package com.bhex.wallet.balance.ui.fragment;

import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.text.marqueen.MarqueeFactory;
import com.bhex.lib.uikit.widget.text.marqueen.MarqueeView;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.tools.constants.BHConstants;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.adapter.AnnouncementMF;
import com.bhex.wallet.balance.adapter.ChainAdapter;
import com.bhex.wallet.balance.model.AnnouncementItem;
import com.bhex.wallet.balance.presenter.BalancePresenter;
import com.bhex.wallet.balance.ui.viewhodler.BalanceViewHolder;
import com.bhex.wallet.balance.viewmodel.AnnouncementViewModel;
import com.bhex.wallet.common.base.BaseFragment;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.CurrencyManager;
import com.bhex.wallet.common.model.BHChain;
import com.bhex.wallet.common.utils.LiveDataBus;
import com.bhex.wallet.common.viewmodel.BalanceViewModel;

import java.util.List;

/**
 * 资产首页
 * @author gdy
 * 2020-3-12
 */
public class BalanceFragment extends BaseFragment<BalancePresenter> {

    BalanceViewHolder balanceViewHolder;
    //公告
    private MarqueeView marquee_announce;
    private MarqueeFactory<RelativeLayout, AnnouncementItem> marqueeFactory;
    private AnnouncementViewModel announcementViewModel;
    //资产
    private BalanceViewModel balanceViewModel;
    //所有链
    private List<BHChain> mChainList;
    private RecyclerView rcv_chain;
    private ChainAdapter mChainAdapter;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_balance;
    }

    @Override
    protected void initView() {
        balanceViewHolder = new BalanceViewHolder(getYActivity(),mRootView.findViewById(R.id.layout_balance_top));
        rcv_chain = mRootView.findViewById(R.id.rcv_chain);
        //获取所有链
        mChainList = CacheCenter.getInstance().getTokenMapCache().getLoadChains();
        rcv_chain.setAdapter(mChainAdapter = new ChainAdapter(mChainList));

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
            //refreshfinish();
            if(ldm.loadingStatus==LoadingStatus.SUCCESS){
                updateAssets();
            }
        });
    }

    @Override
    protected void initPresenter() {
        mPresenter = new BalancePresenter(getYActivity());
    }

    @Override
    protected void addEvent() {
        //
        announcementViewModel.loadAnnouncement(getYActivity());

        marqueeFactory.setOnItemClickListener((view, holder) -> {
            //holder.getData().
            if(!TextUtils.isEmpty(holder.getData().jump_url)){
                ARouter.getInstance()
                        .build(ARouterConfig.Market.market_webview)
                        .withString("url",holder.getData().jump_url)
                        .navigation();
            }
        });

        //选择钱包
        balanceViewHolder.tv_wallet_name.setOnClickListener(v -> {

            AccountListFragment.getInstance().show(getChildFragmentManager(),
                    AccountListFragment.class.getName());
        });

        //钱包二维展示
        balanceViewHolder.iv_wallet_qr.setOnClickListener(v->{
            HbcAddressQrFragment.getInstance().show(getChildFragmentManager(),
                    HbcAddressQrFragment.class.getName());
        });

        //链点击事件
        mChainAdapter.setOnItemClickListener((adapter, view, position) -> {
            BHChain bhChain =  mChainAdapter.getData().get(position);
            ARouter.getInstance().build(ARouterConfig.Balance.Balance_chain_tokens)
                    .withObject("bhChain",bhChain)
                    .withString("title",BHConstants.BHT_TOKEN)
                    .navigation();
        });
    }

    //更新公告
    private void updateAnnouncement(LoadDataModel ldm){
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
        double allTokenAssets = mPresenter.calculateAllTokenPrice(getYActivity(),BHUserManager.getInstance().getAccountInfo(),mChainList);
        String allTokenAssetsText = CurrencyManager.getInstance().getCurrencyDecription(getYActivity(),allTokenAssets);

        mPresenter.setTextFristSamll(balanceViewHolder.tv_asset,allTokenAssetsText);
    }
}
