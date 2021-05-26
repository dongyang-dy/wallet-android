package com.bhex.wallet.balance.ui.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.text.marqueen.MarqueeFactory;
import com.bhex.lib.uikit.widget.text.marqueen.MarqueeView;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.network.cache.stategy.CacheStrategy;
import com.bhex.tools.constants.BHConstants;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.adapter.AnnouncementMF;
import com.bhex.wallet.balance.adapter.HBalanceAdapter;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.balance.helper.BHTokenHelper;
import com.bhex.wallet.balance.model.AnnouncementItem;
import com.bhex.wallet.balance.model.BHTokenItem;
import com.bhex.wallet.balance.presenter.BalancePresenter;
import com.bhex.wallet.balance.ui.viewhodler.BalanceViewHolder;
import com.bhex.wallet.balance.viewmodel.AnnouncementViewModel;
import com.bhex.wallet.balance.viewmodel.ChainTokenViewModel;
import com.bhex.wallet.common.base.BaseFragment;
import com.bhex.wallet.common.cache.ConfigMapCache;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.MainActivityManager;
import com.bhex.wallet.common.manager.SecuritySettingManager;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.utils.LiveDataBus;
import com.bhex.wallet.common.viewmodel.BalanceViewModel;
import com.bhex.wallet.common.viewmodel.WalletViewModel;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 资产首页
 * @author gdy
 * 2020-3-12
 */
public class BalanceFragment extends BaseFragment<BalancePresenter> {
    private SmartRefreshLayout refreshLayout;
    //顶部卡片
    private BalanceViewHolder balanceViewHolder;
    //公告
    private MarqueeView marquee_announce;
    private MarqueeFactory<RelativeLayout, AnnouncementItem> marqueeFactory;
    //公告
    private AnnouncementViewModel announcementViewModel;
    //钱包
    private WalletViewModel walletViewModel;
    //资产
    private BalanceViewModel balanceViewModel;
    //币种
    private ChainTokenViewModel mChainTokenViewModel;

    //资产列表
    private RecyclerView rec_balance;
    private HBalanceAdapter balanceAdapter;

    //
    private List<BHToken> bhTokens;

    private boolean isOpenEye = true;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_balance;
    }

    @Override
    protected void initView() {
        refreshLayout = mRootView.findViewById(R.id.refreshLayout);
        balanceViewHolder = new BalanceViewHolder(getYActivity(),mRootView.findViewById(R.id.layout_balance_top));

        //通知公告
        marquee_announce = mRootView.findViewById(R.id.marquee_announce);
        marqueeFactory = new AnnouncementMF(getContext(),null);

        //公告
        announcementViewModel = ViewModelProviders.of(this).get(AnnouncementViewModel.class);
        announcementViewModel.mutableLiveData.observe(this,ldm->{
            updateAnnouncement(ldm);
        });

        //资产
        balanceViewModel = ViewModelProviders.of(this).get(BalanceViewModel.class);
        //资产订阅
        LiveDataBus.getInstance().with(BHConstants.Label_Account, LoadDataModel.class).observe(this, ldm->{
            refreshfinish();
            if(ldm.loadingStatus==LoadingStatus.SUCCESS){
                updateAssets();
            }
        });

        //钱包
        walletViewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
        walletViewModel.mutableLiveData.observe(this,ldm->{
            updateWalletStatus(ldm);
        });

        //币种
        mChainTokenViewModel =  ViewModelProviders.of(getYActivity()).get(ChainTokenViewModel.class);
        mChainTokenViewModel.mutableLiveData.observe(this,ldm->{
            //defRefreshCount2++;
            //updateAssetList((List<BHToken>)ldm.getData());
            //finishRefresh();
        });

        //bhTokens = BHTokenHelper.loadTokenByChain(BHConstants.BHT_TOKEN);
        rec_balance = mRootView.findViewById(R.id.rcv_balance);
        balanceAdapter = new HBalanceAdapter(bhTokens);
        rec_balance.setAdapter(balanceAdapter);
    }

    @Override
    protected void addEvent() {

        //自动刷新
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            //获取币种列表
            //mChainTokenViewModel.loadBalanceByChain(this,BHConstants.BHT_TOKEN);
            balanceViewModel.getAccountInfo(getYActivity(), CacheStrategy.cacheAndRemote());
            announcementViewModel.loadAnnouncement(getYActivity());
            ConfigMapCache.getInstance().loadChain();
        });
        refreshLayout.autoRefresh();

        balanceViewHolder.iv_open_eye.setOnClickListener(this::isOpenEyeAction);

        //选择钱包
        balanceViewHolder.tv_wallet_name.setOnClickListener(v -> {
            AccountListFragment.getInstance(BalanceFragment.this::chooseAccountAction).show(getChildFragmentManager(),
                    AccountListFragment.class.getName());
        });

        //钱包二维码展示
        balanceViewHolder.iv_wallet_qr.setOnClickListener(v->{
            HbcAddressQrFragment.getInstance().show(getChildFragmentManager(),
                    HbcAddressQrFragment.class.getName());
        });

        //币种搜索
        AppCompatImageView iv_token_search = mRootView.findViewById(R.id.iv_token_search);
        iv_token_search.setOnClickListener(v -> {
            ARouter.getInstance().build(ARouterConfig.Balance.Balance_Search).navigation();
        });

        //关闭通知
        mRootView.findViewById(R.id.iv_announce_close).setOnClickListener(v->{

            mRootView.findViewById(R.id.layout_announce).setVisibility(View.GONE);
            //
            MarqueeView marqueeView = mRootView.findViewById(R.id.marquee_announce);
            marqueeView.stopFlipping();
        });

        //交易列表
        balanceAdapter.setOnItemClickListener((adapter, view, position) -> {
            BHToken bhTokenItem = balanceAdapter.getData().get(position);
            Postcard postcard = ARouter.getInstance().build(ARouterConfig.Balance.Balance_Token_Detail)
                    .withString(BHConstants.SYMBOL,bhTokenItem.symbol);
            LogisticsCenter.completion(postcard);
            Intent intent = new Intent(getYActivity(), postcard.getDestination());
            intent.putExtras(postcard.getExtras());
            startActivity(intent);
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        List<BHToken>  res = BHTokenHelper.loadTokenByChain(BHConstants.BHT_TOKEN);
        /*Collections.sort(res, new Comparator<BHToken>() {
            @Override
            public int compare(BHToken o1, BHToken o2) {
                return o2.symbol.compareToIgnoreCase(o1.symbol);
            }
        });*/
        balanceAdapter.setNewData(res);
    }

    @Override
    protected void initPresenter() {
        mPresenter = new BalancePresenter(getYActivity());
    }

    //切换账户--事件
    private void chooseAccountAction(BHWallet chooseWallet){
        chooseWallet.isDefault = BH_BUSI_TYPE.默认托管单元.getIntValue();
        BHUserManager.getInstance().setCurrentBhWallet(chooseWallet);
        //请显示的资产
        BHUserManager.getInstance().setAccountInfo(null);
        balanceAdapter.notifyDataSetChanged();
        getActivity().recreate();
        walletViewModel.updateWallet(MainActivityManager.getInstance().mainActivity,chooseWallet,chooseWallet.id,
                BH_BUSI_TYPE.默认托管单元.getIntValue(),false);
        balanceViewModel.getAccountInfo(MainActivityManager.getInstance().mainActivity,CacheStrategy.cacheAndRemote());
        //取消定时
        SecuritySettingManager.getInstance().request_thirty_in_time(false,"");
    }

    //更新公告
    private void updateAnnouncement(LoadDataModel ldm){
        refreshfinish();
        if(ldm.loadingStatus != LoadingStatus.SUCCESS){
            return;
        }
        List<AnnouncementItem> list = (List<AnnouncementItem>)ldm.getData();
        marqueeFactory.setData(list);
        marquee_announce.setMarqueeFactory(marqueeFactory);
        marquee_announce.startFlipping();
        mRootView.findViewById(R.id.layout_announce).setVisibility(View.VISIBLE);
    }

    //更新资产
    private void updateAssets() {
        if(BHUserManager.getInstance().getAccountInfo()==null){
            return;
        }
        balanceViewHolder.updateAsset(isOpenEye);
        //更新列表资产
        balanceAdapter.setOpenEye(isOpenEye);
    }

    //是否显示资产
    private void isOpenEyeAction(View view) {
        isOpenEye = !isOpenEye;
        updateAssets();
    }

    private void updateWalletStatus(LoadDataModel ldm) {
        if(ldm.getLoadingStatus()==LoadDataModel.SUCCESS){

        }
    }



    int refreshCount = 0;
    public void refreshfinish(){
        if(++refreshCount==2){
            refreshLayout.finishRefresh();
            refreshCount=0;
        }
    }
}
