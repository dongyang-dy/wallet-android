package com.bhex.wallet.balance.ui.fragment;

import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.lib.uikit.widget.RecycleViewExtDivider;
import com.bhex.lib.uikit.widget.text.marqueen.MarqueeFactory;
import com.bhex.lib.uikit.widget.text.marqueen.MarqueeView;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.network.cache.stategy.CacheStrategy;
import com.bhex.network.cache.stategy.IStrategy;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.adapter.AnnouncementMF;
import com.bhex.wallet.balance.adapter.ChainAdapter;
import com.bhex.wallet.balance.model.AnnouncementItem;
import com.bhex.wallet.balance.model.BHTokenItem;
import com.bhex.wallet.balance.presenter.BalancePresenter;
import com.bhex.wallet.balance.ui.viewhodler.BalanceViewHolder;
import com.bhex.wallet.balance.viewmodel.AnnouncementViewModel;
import com.bhex.wallet.balance.viewmodel.ChainTokenViewModel;
import com.bhex.wallet.common.base.BaseFragment;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.cache.TokenMapCache;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.event.AccountEvent;
import com.bhex.wallet.common.event.CurrencyEvent;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.CurrencyManager;
import com.bhex.wallet.common.manager.MainActivityManager;
import com.bhex.wallet.common.manager.SequenceManager;
import com.bhex.wallet.common.model.BHChain;
import com.bhex.wallet.common.utils.LiveDataBus;
import com.bhex.wallet.common.viewmodel.BalanceViewModel;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * 资产首页
 * @author gdy
 * 2020-3-12
 */
public class BalanceFragment extends BaseFragment<BalancePresenter> {
    private RecyclerView recycler_balance;
    private SmartRefreshLayout refreshLayout;
    private MarqueeView layout_balance_tip;
    private ChainAdapter mChainAdapter;
    private List<BHChain> mChainList;
    private BHWallet bhWallet;
    private AnnouncementViewModel announcementViewModel;
    private BalanceViewHolder balanceViewHolder;

    private BalanceViewModel balanceViewModel;
    private ChainTokenViewModel mChainTokenViewModel;

    //总资产
    private double allTokenAssets;

    public BalanceFragment() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_balance;
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        bhWallet = BHUserManager.getInstance().getCurrentBhWallet();
        //所有链
        recycler_balance = mRootView.findViewById(R.id.recycler_balance);
        refreshLayout = mRootView.findViewById(R.id.refreshLayout);
        layout_balance_tip = mRootView.findViewById(R.id.layout_balance_tip);


        mChainList = CacheCenter.getInstance().getTokenMapCache().getLoadChains();
        recycler_balance.getLayoutManager().setAutoMeasureEnabled(true);
        recycler_balance.setNestedScrollingEnabled(false);
        RecycleViewExtDivider ItemDecoration = new RecycleViewExtDivider(
                getContext(),LinearLayoutManager.VERTICAL,
                PixelUtils.dp2px(getYActivity(),68),0,
                ColorUtil.getColor(getContext(),R.color.global_divider_color));

        recycler_balance.addItemDecoration(ItemDecoration);
        recycler_balance.setAdapter(mChainAdapter = new ChainAdapter(mChainList));
        refreshLayout.setEnableLoadMore(false);
        //
        balanceViewHolder = new BalanceViewHolder(mRootView,getYActivity());
        balanceViewHolder.initContent();

        //
        mRootView.findViewById(R.id.iv_eye).setOnClickListener(this::onViewClick);
        mRootView.findViewById(R.id.btn_coin_apply).setOnClickListener(this::onViewClick);
        //
        mChainTokenViewModel =  ViewModelProviders.of(this).get(ChainTokenViewModel.class);
        mChainTokenViewModel.mutableLiveData.observe(this,ldm->{
            updateAssetList((List<BHTokenItem>)ldm.getData());
        });

        //
        GradientDrawable drawable = ShapeUtils.getRoundRectDrawable(PixelUtils.dp2px(getYActivity(),6),ColorUtil.getColor(getYActivity(),R.color.card_bg));
        mRootView.findViewById(R.id.layout_announce).setBackground(drawable);
    }

    @Override
    protected void initPresenter() {
        mPresenter = new BalancePresenter(getYActivity());
    }

    @Override
    protected void addEvent() {
        mRootView.findViewById(R.id.iv_announce_close).setOnClickListener(this::onViewClick);
        //资产列表点击事件
        mChainAdapter.setOnItemClickListener((adapter, view, position) -> {
            BHChain bhChain =  mChainAdapter.getData().get(position);
            ARouter.getInstance().build(ARouterConfig.Balance.Balance_chain_tokens)
                    .withObject("bhChain",bhChain)
                    .withString("title",BHConstants.BHT_TOKEN)
                    .navigation();
        });

        balanceViewModel = ViewModelProviders.of(MainActivityManager._instance.mainActivity).get(BalanceViewModel.class).build(getYActivity());
        //资产订阅
        LiveDataBus.getInstance().with(BHConstants.Label_Account, LoadDataModel.class).observe(this, ldm->{
            refreshfinish();
            if(ldm.loadingStatus==LoadingStatus.SUCCESS){
                updateAssets();
            }
        });

        //公告
        announcementViewModel = ViewModelProviders.of(this).get(AnnouncementViewModel.class);
        announcementViewModel.mutableLiveData.observe(this,ldm->{
            updateAnnouncement(ldm);
        });

        //自动刷新
        refreshLayout.setOnRefreshListener(refreshLayout1 -> {
            balanceViewModel.getAccountInfo(getYActivity(), CacheStrategy.firstRemote());
            announcementViewModel.loadAnnouncement(getYActivity());
            TokenMapCache.getInstance().loadChain();
        });

        refreshLayout.autoRefresh();
        //重置Sequece
        //balanceViewModel.resetSequence(getYActivity());
    }

    public void updateAssetList(List<BHTokenItem> list){
        AppCompatTextView btn_coin_apply = mRootView.findViewById(R.id.btn_coin_apply);
        btn_coin_apply.setText(getString(R.string.claim_test_coin));
        updateAssets();
    }

    //关闭眼睛
    private void onViewClick(View view) {
        if(view.getId()==R.id.iv_eye){
            mPresenter.hiddenAssetExt(getYActivity(),balanceViewHolder.tv_asset,balanceViewHolder.iv_eye);
            mChainAdapter.setIsHidden(balanceViewHolder.iv_eye.getTag().toString());
        }else if(view.getId()==R.id.btn_coin_apply){
            applyTestToken();
        }else if(view.getId()==R.id.iv_announce_close){
            //关闭通知
            mRootView.findViewById(R.id.layout_announce).setVisibility(View.GONE);
            //
            layout_balance_tip.stopFlipping();
        }
    }

    public void applyTestToken(){
        //
        AppCompatTextView btn_coin_apply = mRootView.findViewById(R.id.btn_coin_apply);
        btn_coin_apply.setText(getString(R.string.claiming));
        mChainTokenViewModel.send_test_token(getYActivity(),"hbc","kiwi");
        /*String address = BHUserManager.getInstance().getCurrentBhWallet().address;
        String url = BHConstants.API_BASE_URL.concat("receive");
        ARouter.getInstance().build(ARouterConfig.Market.market_webview).withString("url",url).navigation();*/
    }

    //更新资产
    private void updateAssets() {
        if(BHUserManager.getInstance().getAccountInfo()==null){
            return;
        }
        allTokenAssets = mPresenter.calculateAllTokenPrice(getYActivity(),BHUserManager.getInstance().getAccountInfo(),mChainList);
        String allTokenAssetsText = CurrencyManager.getInstance().getCurrencyDecription(getYActivity(),allTokenAssets);
        //LogUtils.d("BalanceFragment===>:","allTokenAssetsText=="+allTokenAssetsText);
        //设置第一字符15sp
        String tag = balanceViewHolder.iv_eye.getTag().toString();
        if(balanceViewHolder.iv_eye.getTag().equals("0")){
            mPresenter.setTextFristSamll(balanceViewHolder.tv_asset,allTokenAssetsText);
        }else{
            balanceViewHolder.tv_asset.setText("***");
        }
        mChainAdapter.notifyDataSetChanged();
    }

    //更新公告
    private void updateAnnouncement(LoadDataModel ldm) {
        refreshfinish();
        if(ldm.loadingStatus != LoadingStatus.SUCCESS){
            return;
        }

        //LogUtils.d("AnnouncementMF==>","==updateAnnouncement==");
        List<AnnouncementItem> list = (List<AnnouncementItem>)ldm.getData();
        MarqueeFactory<RelativeLayout, AnnouncementItem> marqueeFactory = new AnnouncementMF(getContext(),null);
        marqueeFactory.setData(list);
        layout_balance_tip.setMarqueeFactory(marqueeFactory);
        layout_balance_tip.startFlipping();
        mRootView.findViewById(R.id.layout_announce).setVisibility(View.VISIBLE);
        marqueeFactory.setOnItemClickListener((view, holder) -> {
            //holder.getData().
            if(!TextUtils.isEmpty(holder.getData().jump_url)){
                ARouter.getInstance()
                        .build(ARouterConfig.Market.market_webview)
                        .withString("url",holder.getData().jump_url)
                        .navigation();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changeAccount(AccountEvent walletEvent){
        //当前钱包用户
        bhWallet = BHUserManager.getInstance().getCurrentBhWallet();
        //AssetHelper.proccessAddress(tv_address,bhWallet.getAddress());
        //清空原始用户资产
        balanceViewHolder.tv_asset.setText("");
        balanceViewHolder.tv_wallet_name.setText(bhWallet.name);
        //balanceViewHolder.tv_wallet_name.setText(bhWallet.name+"-" +SequenceManager.getInstance().getSequence());
        mChainAdapter.notifyDataSetChanged();
        //更新资产
        balanceViewModel.getAccountInfo(getYActivity(),null);
    }

    //切换法币单位
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changeCurrency(CurrencyEvent currencyEvent){
        updateAssets();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            bhWallet = BHUserManager.getInstance().getCurrentBhWallet();
            balanceViewHolder.tv_wallet_name.setText(bhWallet.name);
            //balanceViewHolder.tv_wallet_name.setText(bhWallet.name+"-" +SequenceManager.getInstance().getSequence());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    int refreshCount = 0;
    public void refreshfinish(){
        if(++refreshCount==2){
            refreshLayout.finishRefresh();
            refreshCount=0;
        }
    }

}
