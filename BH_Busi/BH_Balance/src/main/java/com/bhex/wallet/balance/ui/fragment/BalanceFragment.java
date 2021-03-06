package com.bhex.wallet.balance.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.DebugUtils;
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
import com.bhex.lib_qr.XQRCode;
import com.bhex.lib_qr.util.QRCodeAnalyzeUtils;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.network.cache.stategy.CacheStrategy;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.PathUtils;
import com.bhex.tools.utils.ToolUtils;
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
import com.bhex.wallet.common.event.AccountEvent;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.MainActivityManager;
import com.bhex.wallet.common.manager.SecuritySettingManager;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.ui.activity.BHQrScanActivity;
import com.bhex.wallet.common.utils.AddressUtil;
import com.bhex.wallet.common.utils.LiveDataBus;
import com.bhex.wallet.common.viewmodel.BalanceViewModel;
import com.bhex.wallet.common.viewmodel.WalletViewModel;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ????????????
 * @author gdy
 * 2020-3-12
 */
public class BalanceFragment extends BaseFragment<BalancePresenter> {
    private SmartRefreshLayout refreshLayout;
    //????????????
    private BalanceViewHolder balanceViewHolder;
    //??????
    private MarqueeView marquee_announce;
    private MarqueeFactory<RelativeLayout, AnnouncementItem> marqueeFactory;
    //??????
    private AnnouncementViewModel announcementViewModel;
    //??????
    private WalletViewModel walletViewModel;
    //??????
    private BalanceViewModel balanceViewModel;
    //??????
    private ChainTokenViewModel mChainTokenViewModel;

    //????????????
    private RecyclerView rec_balance;
    private HBalanceAdapter balanceAdapter;

    //
    private List<BHToken> bhTokens;

    private boolean isOpenEye = true;

    private View layout_empty_asset;
    @Override
    public int getLayoutId() {
        return R.layout.fragment_balance;
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);

        refreshLayout = mRootView.findViewById(R.id.refreshLayout);
        layout_empty_asset = View.inflate(getYActivity(),R.layout.layout_empty_asset,null);

        balanceViewHolder = new BalanceViewHolder(getYActivity(),mRootView.findViewById(R.id.layout_balance_top));

        //????????????
        marquee_announce = mRootView.findViewById(R.id.marquee_announce);
        marqueeFactory = new AnnouncementMF(getContext(),null);

        //??????
        announcementViewModel = ViewModelProviders.of(this).get(AnnouncementViewModel.class);
        announcementViewModel.mutableLiveData.observe(this,ldm->{
            updateAnnouncement(ldm);
        });

        //??????
        balanceViewModel = ViewModelProviders.of(this).get(BalanceViewModel.class);
        //????????????
        LiveDataBus.getInstance().with(BHConstants.Label_Account, LoadDataModel.class).observe(this, ldm->{
            refreshfinish();
            updateAssets();
            /*if(ldm.loadingStatus==LoadingStatus.SUCCESS){
                updateAssets();
            }else{
                //LogUtils.d("BalanceFragment===","==onFail==");
                //balanceViewHolder.updateAssetFailStatus(isOpenEye);
            }*/
        });

        //??????
        walletViewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
        walletViewModel.mutableLiveData.observe(this,ldm->{
            updateWalletStatus(ldm);
        });

        //??????
        mChainTokenViewModel =  ViewModelProviders.of(getYActivity()).get(ChainTokenViewModel.class);
        /*mChainTokenViewModel.mutableLiveData.observe(this,ldm->{
            //defRefreshCount2++;
            //updateAssetList((List<BHToken>)ldm.getData());
            //finishRefresh();
        });*/

        //bhTokens = BHTokenHelper.loadTokenByChain(BHConstants.BHT_TOKEN);
        rec_balance = mRootView.findViewById(R.id.rcv_balance);
        balanceAdapter = new HBalanceAdapter(bhTokens);
        rec_balance.setAdapter(balanceAdapter);

        balanceAdapter.setEmptyView(layout_empty_asset);
    }

    @Override
    protected void addEvent() {
        LogUtils.d("BalanceFragment====>:","==addEvent===");
        //????????????
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            //??????????????????
            //mChainTokenViewModel.loadBalanceByChain(this,BHConstants.BHT_TOKEN);
            balanceViewModel.getAccountInfo(getYActivity(), CacheStrategy.onlyRemote());
            announcementViewModel.loadAnnouncement(getYActivity());
            ConfigMapCache.getInstance().loadChain();
        });
        refreshLayout.autoRefresh();

        balanceViewHolder.iv_open_eye.setOnClickListener(this::isOpenEyeAction);

        //????????????
        balanceViewHolder.tv_wallet_name.setOnClickListener(v -> {
            AccountListFragment.getInstance(BalanceFragment.this::chooseAccountAction).show(getChildFragmentManager(),
                    AccountListFragment.class.getName());
        });

        //?????????????????????
        balanceViewHolder.iv_wallet_qr.setOnClickListener(v->{
            HbcAddressQrFragment.getInstance().show(getChildFragmentManager(),
                    HbcAddressQrFragment.class.getName());
        });

        //????????????
        AppCompatImageView iv_token_search = mRootView.findViewById(R.id.iv_token_search);
        iv_token_search.setOnClickListener(v -> {
            ARouter.getInstance().build(ARouterConfig.Balance.Balance_Search).navigation();
        });

        //????????????
        mRootView.findViewById(R.id.iv_announce_close).setOnClickListener(v->{

            mRootView.findViewById(R.id.layout_announce).setVisibility(View.GONE);
            //
            MarqueeView marqueeView = mRootView.findViewById(R.id.marquee_announce);
            marqueeView.stopFlipping();
        });

        //????????????
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
        LogUtils.d("BalanceFragment====>","===onResume==="+isVisible());
        bhTokens = BHTokenHelper.loadTokenByChain(BHConstants.BHT_TOKEN);
        List<BHToken> res = BHTokenHelper.sortBHToken(getYActivity(),bhTokens);
        balanceAdapter.setNewData(res);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            LogUtils.d("BalanceFragment====>","===onHiddenChanged==="+hidden);
            balanceViewHolder.updateWalletName();
        }

    }

    @Override
    protected void initPresenter() {
        mPresenter = new BalancePresenter(getYActivity());
    }

    //????????????--??????
    private void chooseAccountAction(BHWallet chooseWallet){
        chooseWallet.isDefault = BH_BUSI_TYPE.??????????????????.getIntValue();
        BHUserManager.getInstance().setCurrentBhWallet(chooseWallet);
        //??????????????????
        BHUserManager.getInstance().setAccountInfo(null);
        balanceAdapter.notifyDataSetChanged();
        getYActivity().getWindow().setWindowAnimations(R.style.WindowAnimationFadeInOut);

        getActivity().recreate();
        walletViewModel.updateWallet(MainActivityManager.getInstance().mainActivity,chooseWallet,chooseWallet.id,
                BH_BUSI_TYPE.??????????????????.getIntValue(),false);
        balanceViewModel.getAccountInfo(MainActivityManager.getInstance().mainActivity,CacheStrategy.onlyRemote());
        //????????????
        SecuritySettingManager.getInstance().request_thirty_in_time(false,"");
    }

    //????????????
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

    //????????????
    private void updateAssets() {
        /*if(BHUserManager.getInstance().getAccountInfo()==null){
            return;
        }*/
        balanceViewHolder.updateAsset(isOpenEye);

        //??????????????????
        if(ToolUtils.checkListIsEmpty(bhTokens)){
            return;
        }
        List<BHToken>  result = new ArrayList<>(bhTokens);
        result = BHTokenHelper.sortBHToken(getYActivity(),result);
        balanceAdapter.setOpenEye(result,isOpenEye);
    }

    //??????????????????
    private void isOpenEyeAction(View view) {
        isOpenEye = !isOpenEye;
        updateAssets();
    }

    private void updateWalletStatus(LoadDataModel ldm) {
        if(ldm.getLoadingStatus()==LoadDataModel.SUCCESS){

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changeAccount(AccountEvent walletEvent){
        LogUtils.d("BalanceFragment====>;","==changeAccount==");
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //LogUtils.d("BalanceFragment====>:","==onActivityResult==");
        if (requestCode == BHQrScanActivity.REQUEST_CODE ) {
            if(resultCode == Activity.RESULT_OK){
                String qrCode  = data.getExtras().getString(XQRCode.RESULT_DATA);
                if(AddressUtil.validHbcAddress(qrCode)){
                    ARouter.getInstance().build(ARouterConfig.Balance.Balance_transfer_out)
                            .withString(BHConstants.SYMBOL, BHConstants.BHT_TOKEN)
                            .withString(BHConstants.ADDRESS, qrCode)
                            .withInt(BHConstants.WAY, BH_BUSI_TYPE.????????????.getIntValue())
                            .navigation();
                }
            }else if(resultCode == BHQrScanActivity.REQUEST_IMAGE){
                getAnalyzeQRCodeResult(data.getData());
            }
        }
    }

    private void getAnalyzeQRCodeResult(Uri uri) {
        XQRCode.analyzeQRCode(PathUtils.getFilePathByUri(getYActivity(), uri), new QRCodeAnalyzeUtils.AnalyzeCallback() {
            @Override
            public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                if(AddressUtil.validHbcAddress(result)){
                    ARouter.getInstance().build(ARouterConfig.Balance.Balance_transfer_out)
                            .withString(BHConstants.SYMBOL, BHConstants.BHT_TOKEN)
                            .withString(BHConstants.ADDRESS, result)
                            .withInt(BHConstants.WAY, BH_BUSI_TYPE.????????????.getIntValue())
                            .navigation();
                }
            }

            @Override
            public void onAnalyzeFailed() {
                ToastUtils.showToast(getResources().getString(R.string.encode_qr_fail));
            }
        });
    }
}
