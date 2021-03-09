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

import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.EmptyLayout;
import com.bhex.lib.uikit.widget.RecycleViewExtDivider;
import com.bhex.lib.uikit.widget.layout.XUIFrameLayout;
import com.bhex.lib.uikit.widget.layout.XUIRelativeLayout;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
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
public abstract class TokenDetailActivity extends BaseActivity<AssetPresenter> {
    public BHBalance bthBalance;
    public BHBalance symbolBalance;
    public BHToken symbolToken;
    public BHBalance chainSymbolBalance;

    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;
    @BindView(R2.id.btn_item1)
    MaterialButton btn_item1;
    @BindView(R2.id.btn_item2)
    MaterialButton btn_item2;
    @BindView(R2.id.btn_item3)
    MaterialButton btn_item3;
    @BindView(R2.id.btn_item4)
    MaterialButton btn_item4;
    @BindView(R2.id.recycler_order)
    RecyclerView recycler_order;
    @BindView(R2.id.empty_layout)
    EmptyLayout empty_layout;

    @BindView(R2.id.tv_coin_amount)
    AppCompatTextView tv_coin_amount;
    @BindView(R2.id.tv_coin_currency)
    AppCompatTextView tv_coin_currency;

    @BindView(R2.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R2.id.iv_coin_ic)
    AppCompatImageView iv_coin_ic;

    @BindView(R2.id.layout_hbc_view)
    ConstraintLayout layout_hbc_view;

    @BindView(R2.id.tv_available_value)
    AppCompatTextView tv_available_value;
    @BindView(R2.id.tv_entrust_value)
    AppCompatTextView tv_entrust_value;
    @BindView(R2.id.tv_redemption_value)
    AppCompatTextView tv_redemption_value;
    @BindView(R2.id.tv_income_value)
    AppCompatTextView tv_income_value;

    AppCompatTextView tv_right_title;

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
        symbolBalance = BHBalanceHelper.getBHBalanceFromAccount(getSymbol());
        symbolToken  = CacheCenter.getInstance().getSymbolCache().getBHToken(getSymbol());
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

        tv_right_title = findViewById(R.id.tv_right_title);

        //根据token 显示View
        initTokenView();
        initTokenAsset();
    }

    private void initTokenView() {
        //XUIRelativeLayout layout_top = findViewById(R.id.layout_top);
        //layout_top.setShadowColor(Color.parseColor("#1F3375E0"));
        if(BHConstants.BHT_TOKEN.equalsIgnoreCase(symbolToken.name)){
            layout_hbc_view.setVisibility(View.VISIBLE);
        }else{
            layout_hbc_view.setVisibility(View.GONE);
        }

        if(!TextUtils.isEmpty(symbolToken.logo)){
            iv_coin_ic.setAlpha(0.1f);
            ImageLoaderUtil.loadImageView(this,symbolToken.logo,iv_coin_ic,R.mipmap.ic_default_coin);
        }

        if(BHConstants.BHT_TOKEN.equalsIgnoreCase(symbolBalance.symbol)){
            //转账
            btn_item1.setText(getResources().getString(R.string.transfer_in));
            btn_item2.setText(getResources().getString(R.string.transfer));

        } else if (BHConstants.BHT_TOKEN.equalsIgnoreCase(symbolBalance.chain)) {
            //原生代币
            btn_item1.setText(getResources().getString(R.string.transfer_in));
            btn_item2.setText(getResources().getString(R.string.transfer));

        } else {
            //跨链代币
            btn_item1.setText(getResources().getString(R.string.deposit));
            btn_item2.setText(getResources().getString(R.string.draw_coin));
            //转账
        }

        //兑币功能
        BHTokenMapping tokenMapping = CacheCenter.getInstance().getTokenMapCache().getTokenMappingOne(symbolToken.symbol);
        if(tokenMapping!=null){
            btn_item3.setVisibility(View.VISIBLE);
            btn_item3.setText(getResources().getString(R.string.swap));
        }else{
            btn_item3.setVisibility(View.GONE);
        }

    }

    private void initTokenAsset(){
        if(BHUserManager.getInstance().getAccountInfo()==null){
            return;
        }
        //计算持有资产
        String []res = BHBalanceHelper.getAmountToCurrencyValue(this,symbolBalance.amount,symbolToken.symbol,false);
        tv_coin_amount.setText(res[0]);
        //对应法币实际值
        tv_coin_currency.setText(res[1]);
        //链上资产

        //初始化话HBC资产
        if(BHUserManager.getInstance().getAccountInfo()==null){
            return;
        }
        String available_value = NumberUtil.dispalyForUsertokenAmount4Level(BHUserManager.getInstance().getAccountInfo() .available );
        tv_available_value.setText(available_value);

        //委托中
        String bonded_value = NumberUtil.dispalyForUsertokenAmount4Level(BHUserManager.getInstance().getAccountInfo().bonded);
        tv_entrust_value.setText(bonded_value);
        //赎回中
        String unbonding_value = NumberUtil.dispalyForUsertokenAmount4Level(BHUserManager.getInstance().getAccountInfo().unbonding);
        tv_redemption_value.setText(unbonding_value);

        //已收益
        String claimed_reward_value = NumberUtil.dispalyForUsertokenAmount4Level(BHUserManager.getInstance().getAccountInfo().claimed_reward);
        tv_income_value.setText(claimed_reward_value);
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
            balanceViewModel.getAccountInfo(TokenDetailActivity.this,null);
            transactionViewModel.queryTransctionByAddress(this,
                    BHUserManager.getInstance().getCurrentBhWallet().address, mCurrentPage, symbolToken.symbol, null);
        });

        transactionViewModel.validatorLiveData.observe(this,ldm->{
            updateValidatorAddress(ldm);
        });

        balanceViewModel.getAccountInfo(TokenDetailActivity.this,null);
        EventBus.getDefault().register(this);

        //币种详情
        tv_right_title.setOnClickListener(this::gotoTokenDetail);
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
            initTokenAsset();
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
        if (data == null || data.size() == 0) {
            return;
        }
        mTxOrderAdapter.getData().clear();
        mOrderList = data;
        mTxOrderAdapter.addData(data);
    }

    /**
     * 更新验证人列表
     */
    private List<DelegateValidator> mRewardList;
    private void updateValidatorAddress(LoadDataModel ldm) {
        if(ldm.loadingStatus==LoadDataModel.SUCCESS){
            if(ldm.code==1){
                toDoWithdrawShare(ldm);
            }else{
                toDoReDelegate(ldm);
            }
        }else if(ldm.loadingStatus==LoadDataModel.ERROR){
            ToastUtils.showToast(getResources().getString(R.string.no_profit));
        }
    }

    //提取收益--回调
    public void toDoWithdrawShare(LoadDataModel ldm){
        List<DelegateValidator> dvList =  (List<DelegateValidator>)ldm.getData();
        //计算所有收益
        double all_reward = mPresenter.calAllReward(dvList);
        String def_all_reward = NumberUtil.dispalyForUsertokenAmount4Level(all_reward+"");
        if(Double.valueOf(def_all_reward)<=0){
            ToastUtils.showToast(getResources().getString(R.string.no_profit));
        }else {
            mRewardList = dvList;
            WithDrawShareFragment.showWithDrawShareFragment(getSupportFragmentManager(),
                    WithDrawShareFragment.class.getSimpleName(), itemListener,def_all_reward);
        }
    }

    //复投分红回调
    public void toDoReDelegate(LoadDataModel ldm){
        List<DelegateValidator> dvList =  (List<DelegateValidator>)ldm.getData();
        //计算所有收益
        double all_reward = mPresenter.calAllReward(dvList);
        String def_all_reward = NumberUtil.dispalyForUsertokenAmount4Level(all_reward+"");
        if(Double.valueOf(def_all_reward)<=0){
            ToastUtils.showToast(getResources().getString(R.string.no_profit));
        }else {
            mRewardList = dvList;
            ReInvestShareFragment.showWithDrawShareFragment(getSupportFragmentManager(),
                    ReInvestShareFragment.class.getSimpleName(),fragmentItemListener,def_all_reward);
        }
    }

    Password30PFragment.PasswordClickListener withDrawPwdListener = (password, position,way) -> {
        if(position==1){

            List<TransactionMsg.ValidatorMsg> validatorMsgs = mPresenter.getAllValidator(mRewardList);
            List<TxReq.TxMsg> tx_msg_list = BHRawTransaction.createRewardMsg(validatorMsgs);
            transactionViewModel.transferInnerExt(this,password,BHUserManager.getInstance().getDefaultGasFee().displayFee,tx_msg_list);
        }else if(position==2){

            List<TransactionMsg.ValidatorMsg> validatorMsgs = mPresenter.getAllValidator(mRewardList);
            List<TransactionMsg.DoEntrustMsg> doEntrustMsgs = mPresenter.getAllEntrust(mRewardList);
            List<TxReq.TxMsg> tx_msg_list = BHRawTransaction.createReDoEntrustMsg(validatorMsgs,doEntrustMsgs);
            transactionViewModel.transferInnerExt(this,password,BHUserManager.getInstance().getDefaultGasFee().displayFee,tx_msg_list);
        }
    };

    //发送提取分红交易
    private WithDrawShareFragment.FragmentItemListener itemListener = (position -> {
        Password30PFragment.showPasswordDialog(getSupportFragmentManager(),Password30PFragment.class.getSimpleName(),withDrawPwdListener,1,true);
    });

    //发送复投分红交易
    private ReInvestShareFragment.FragmentItemListener fragmentItemListener = (position -> {
        Password30PFragment.showPasswordDialog(getSupportFragmentManager(),Password30PFragment.class.getSimpleName(),withDrawPwdListener,2,true);
    });

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateTransction(TransctionEvent txEvent){
        transactionViewModel.queryTransctionByAddress(this,
                BHUserManager.getInstance().getCurrentBhWallet().address, mCurrentPage, symbolToken.symbol, null);
    }

    //跳转币种详情
    private void gotoTokenDetail(View view) {
        ARouter.getInstance().build(ARouterConfig.Market.market_webview).withString("url",getTokenUrl()).navigation();
    }


    private String getTokenUrl(){
        String v_local_display = ToolUtils.getLocalString(this);
        String url = BHConstants.API_BASE_URL
                .concat("tokens/")
                .concat(getSymbol())
                .concat("?lang=").concat(v_local_display);
        LogUtils.d("url==>"+url);

        return url;
    }

    public abstract String getSymbol();
}
