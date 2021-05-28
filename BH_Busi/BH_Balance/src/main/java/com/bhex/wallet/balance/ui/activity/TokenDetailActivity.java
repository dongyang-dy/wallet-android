package com.bhex.wallet.balance.ui.activity;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.EmptyLayout;
import com.bhex.lib.uikit.widget.RecycleViewExtDivider;
import com.bhex.lib.uikit.widget.layout.XUIFrameLayout;
import com.bhex.lib.uikit.widget.layout.XUIRelativeLayout;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.network.cache.stategy.CacheStrategy;
import com.bhex.network.cache.stategy.IStrategy;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.ImageLoaderUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.R2;
import com.bhex.wallet.balance.adapter.TxOrderAdapter;
import com.bhex.wallet.balance.event.TransctionEvent;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.balance.helper.TransactionHelper;
import com.bhex.wallet.balance.model.DelegateValidator;
import com.bhex.wallet.balance.model.TxOrderItem;
import com.bhex.wallet.balance.presenter.AssetPresenter;
import com.bhex.wallet.balance.ui.fragment.ReInvestShareFragment;
import com.bhex.wallet.balance.ui.fragment.WithDrawShareFragment;
import com.bhex.wallet.balance.ui.viewhodler.TokenDetailVH;
import com.bhex.wallet.balance.viewmodel.TransactionViewModel;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.MainActivityManager;
import com.bhex.wallet.common.model.AccountInfo;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.model.BHTokenMapping;
import com.bhex.wallet.common.tx.BHRawTransaction;
import com.bhex.wallet.common.tx.TransactionMsg;
import com.bhex.wallet.common.tx.TransactionOrder;
import com.bhex.wallet.common.tx.TxReq;
import com.bhex.wallet.common.ui.fragment.Password30PFragment;
import com.bhex.wallet.common.utils.LiveDataBus;
import com.bhex.wallet.common.viewmodel.BalanceViewModel;
import com.google.android.material.button.MaterialButton;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;

/**
 * @author gongdongyang
 * 2020-4-4 23:34:33
 * 资产详情
 */
@Route(path = ARouterConfig.Balance.Balance_Token_Detail)
public  class TokenDetailActivity extends BaseActivity<AssetPresenter> {
    @Autowired(name = BHConstants.SYMBOL)
    String symbol;

    public BHBalance bthBalance;
    public BHBalance symbolBalance;
    public BHToken symbolToken;
    public BHBalance chainSymbolBalance;

    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;
    @BindView(R2.id.recycler_order)
    RecyclerView recycler_order;
    @BindView(R2.id.empty_layout)
    EmptyLayout empty_layout;
    @BindView(R2.id.refreshLayout)
    SmartRefreshLayout refreshLayout;

    private TokenDetailVH tokenDetailVH;

    TxOrderAdapter mTxOrderAdapter;
    List<TransactionOrder> mOrderList;
    BalanceViewModel balanceViewModel;
    int mCurrentPage = 1;
    TransactionViewModel transactionViewModel;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_token_detail;
    }

    @Override
    protected void initPresenter() {
        mPresenter = new AssetPresenter(this);
    }

    @Override
    protected void initView() {

        ARouter.getInstance().inject(this);
        tokenDetailVH = new TokenDetailVH(this,symbol);
        symbolBalance = BHBalanceHelper.getBHBalanceFromAccount(symbol);
        symbolToken  = CacheCenter.getInstance().getSymbolCache().getBHToken(symbol);
        bthBalance = BHBalanceHelper.getBHBalanceFromAccount(BHConstants.BHT_TOKEN);
        chainSymbolBalance = BHBalanceHelper.getBHBalanceFromAccount(symbolToken.chain);

        tv_center_title.setText(symbolBalance.name.toUpperCase());
        mTxOrderAdapter = new TxOrderAdapter(mOrderList,symbolBalance.symbol);
        recycler_order.setAdapter(mTxOrderAdapter);

        recycler_order.setNestedScrollingEnabled(false);
        RecycleViewExtDivider ItemDecoration = new RecycleViewExtDivider(
                this,LinearLayoutManager.VERTICAL,
                PixelUtils.dp2px(this,24),0,
                ColorUtil.getColor(this,R.color.global_divider_color));

        recycler_order.addItemDecoration(ItemDecoration);

        refreshLayout.setEnableLoadMore(false);

        transactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);
        balanceViewModel = ViewModelProviders.of(MainActivityManager._instance.mainActivity).get(BalanceViewModel.class);

        transactionViewModel.initData(this,symbolBalance.symbol);
        //根据token 显示View
        tokenDetailVH.updateLayout(symbol);
        tokenDetailVH.updateTokenAsset(symbol);
    }

    @Override
    protected void addEvent() {
        empty_layout.showProgess();

        transactionViewModel.queryTransctionByAddress(this,
                BHUserManager.getInstance().getCurrentBhWallet().address, mCurrentPage, symbolToken.symbol, null);

        getLifecycle().addObserver(transactionViewModel);

        transactionViewModel.transLiveData.observe(this, ldm -> {
            //更新交易记录
            if (ldm.loadingStatus == LoadingStatus.SUCCESS ) {
                if((ldm.getData() == null || ldm.getData().size()==0)&& mTxOrderAdapter.getData().size()==0){
                    empty_layout.showNoData();
                }else {
                    empty_layout.loadSuccess();
                    updateTxOrder(ldm.getData());
                }
            } else if(ldm.loadingStatus == LoadingStatus.ERROR){
                if(mTxOrderAdapter.getData()==null || mTxOrderAdapter.getData().size()==0){
                    empty_layout.showNeterror(view -> {
                    });
                }
            }
        });

        mTxOrderAdapter.setOnItemClickListener((adapter, view,
                                                position) -> {
            TransactionOrder txo = mOrderList.get(position);
            TxOrderItem txOrderItem = TransactionHelper.getTxOrderItem(txo);
            TransactionHelper.gotoTranscationDetail(txOrderItem,symbolToken.symbol);
        });

        LiveDataBus.getInstance().with(BHConstants.Label_Account,LoadDataModel.class).observe(this,ldm->{
            updateAssest(ldm);
        });

        refreshLayout.setOnRefreshListener(refreshLayout -> {
            balanceViewModel.getAccountInfo(TokenDetailActivity.this,CacheStrategy.onlyRemote());
            transactionViewModel.queryTransctionByAddress(this,
                    BHUserManager.getInstance().getCurrentBhWallet().address, mCurrentPage, symbolToken.symbol, null);
        });

        /*transactionViewModel.validatorLiveData.observe(this,ldm->{
            updateValidatorAddress(ldm);
        });*/

        balanceViewModel.getAccountInfo(TokenDetailActivity.this, CacheStrategy.onlyRemote());
        EventBus.getDefault().register(this);

        //币种详情
        //tv_right_title.setOnClickListener(this::gotoTokenDetail);
    }

    /**
     * 更新资产
     * @param ldm
     */
    private void updateAssest(LoadDataModel<AccountInfo> ldm) {
        //更新
        refreshLayout.finishRefresh();
        if(ldm.loadingStatus==LoadingStatus.SUCCESS){
            bthBalance = BHBalanceHelper.getBHBalanceFromAccount(BHConstants.BHT_TOKEN);
            symbolBalance = BHBalanceHelper.getBHBalanceFromAccount(symbolToken.symbol);
            chainSymbolBalance = BHBalanceHelper.getBHBalanceFromAccount(symbolToken.chain);
            tokenDetailVH.updateTokenAsset(symbol);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 更新交易记录
     *
     * @param data
     */
    private void updateTxOrder(List<TransactionOrder> data) {
        if (ToolUtils.checkListIsEmpty(data)) {
            return;
        }
        mTxOrderAdapter.getData().clear();
        mOrderList = data;
        mTxOrderAdapter.addData(data);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateTransction(TransctionEvent txEvent){
        transactionViewModel.queryTransctionByAddress(this,
                BHUserManager.getInstance().getCurrentBhWallet().address, mCurrentPage, symbolToken.symbol, null);
    }


}
