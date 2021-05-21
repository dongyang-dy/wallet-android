package com.bhex.wallet.balance.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.NavigateUtil;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.adapter.AnnouncementMF;
import com.bhex.wallet.balance.adapter.BalanceAdapter;
import com.bhex.wallet.balance.adapter.ChainAdapter;
import com.bhex.wallet.balance.adapter.HBalanceAdapter;
import com.bhex.wallet.balance.enums.BUSI_ANNOUNCE_TYPE;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.balance.helper.TokenHelper;
import com.bhex.wallet.balance.model.AnnouncementItem;
import com.bhex.wallet.balance.model.BHTokenItem;
import com.bhex.wallet.balance.presenter.BalancePresenter;
import com.bhex.wallet.balance.ui.viewhodler.BalanceViewHolder;
import com.bhex.wallet.balance.viewmodel.AnnouncementViewModel;
import com.bhex.wallet.common.ActivityCache;
import com.bhex.wallet.common.base.BaseFragment;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.cache.TokenMapCache;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.event.AccountEvent;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.CurrencyManager;
import com.bhex.wallet.common.manager.MainActivityManager;
import com.bhex.wallet.common.manager.SecuritySettingManager;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHChain;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.utils.LiveDataBus;
import com.bhex.wallet.common.viewmodel.BalanceViewModel;
import com.bhex.wallet.common.viewmodel.WalletViewModel;
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

        //
        bhTokens = BHBalanceHelper.loadDefaultToken();
        rec_balance = mRootView.findViewById(R.id.rcv_balance);
        balanceAdapter = new HBalanceAdapter(bhTokens);
        rec_balance.setAdapter(balanceAdapter);
    }

    @Override
    protected void addEvent() {

        //自动刷新
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            balanceViewModel.getAccountInfo(getYActivity(), CacheStrategy.cacheAndRemote());
            announcementViewModel.loadAnnouncement(getYActivity());
            TokenMapCache.getInstance().loadChain();
        });
        refreshLayout.autoRefresh();

        balanceViewHolder.iv_open_eye.setOnClickListener(this::isOpenEyeAction);

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
    protected void initPresenter() {
        mPresenter = new BalancePresenter(getYActivity());
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

    int refreshCount = 0;
    public void refreshfinish(){
        if(++refreshCount==2){
            refreshLayout.finishRefresh();
            refreshCount=0;
        }
    }
}
