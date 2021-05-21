package com.bhex.wallet.balance.ui.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.lib.uikit.widget.EmptyLayout;
import com.bhex.lib.uikit.widget.RecycleViewExtDivider;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.wallet.balance.event.TransctionEvent;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.R2;
import com.bhex.wallet.balance.adapter.BalanceAdapter;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.balance.model.BHTokenItem;
import com.bhex.wallet.balance.presenter.BalancePresenter;
import com.bhex.wallet.balance.ui.viewhodler.BTCViewHolder;
import com.bhex.wallet.balance.ui.viewhodler.ChainBottomLayoutVH;
import com.bhex.wallet.balance.ui.viewhodler.ETHViewHolder;
import com.bhex.wallet.balance.ui.viewhodler.HBCViewHolder;
import com.bhex.wallet.balance.viewmodel.ChainTokenViewModel;
import com.bhex.wallet.balance.viewmodel.TransactionViewModel;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.event.AccountEvent;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.MainActivityManager;
import com.bhex.wallet.common.model.AccountInfo;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHChain;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.utils.LiveDataBus;
import com.bhex.wallet.common.viewmodel.BalanceViewModel;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;

/**
 * @author gongdongyang
 * 2020-8-31 10:57:14
 */
@Route(path = ARouterConfig.Balance.Balance_chain_tokens, name = "链下Token")
public class ChainTokenActivity extends BaseActivity<BalancePresenter> implements OnRefreshListener {
    @Autowired (name = "bhChain")
    public BHChain bhChain;

    @Autowired(name=BHConstants.TITLE)
    public String title;

    BHBalance mBalance;
    //@BindView(R2.id.layout_index_1)
    @BindView(R2.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R2.id.rcv_token_list)
    RecyclerView rcv_token_list;
    @BindView(R2.id.empty_layout)
    EmptyLayout empty_layout;

    AppCompatTextView tv_center_title;

    private BalanceAdapter mBalanceAdapter;
    private ETHViewHolder mETHViewHolder;

    private BalanceViewModel mBalanceViewModel;
    private ChainTokenViewModel mChainTokenViewModel;

    private List<BHTokenItem> mTokenList;
    private int defRefreshCount1 = 0;
    private int defRefreshCount2 = 0;

    private ChainBottomLayoutVH mBottomLayoutVH;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chain_token;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        tv_center_title = findViewById(R.id.tv_center_title);

        BHToken bhToken = SymbolCache.getInstance().getBHToken(bhChain.chain.toLowerCase());
        mBalance = BHBalanceHelper.getBHBalanceFromAccount(bhToken!=null?bhToken.symbol:bhChain.chain);
        //链的名称
        tv_center_title.setText(bhChain.chain.toUpperCase());
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setOnRefreshListener(this);

        RecycleViewExtDivider itemDecoration = new RecycleViewExtDivider(
                this,LinearLayoutManager.VERTICAL,
                PixelUtils.dp2px(this,68),0,
                ColorUtil.getColor(this,R.color.global_divider_color));
        rcv_token_list.addItemDecoration(itemDecoration);

        mBalanceAdapter = new BalanceAdapter(this,mTokenList);
        rcv_token_list.setAdapter(mBalanceAdapter);
        rcv_token_list.setNestedScrollingEnabled(false);

        mETHViewHolder = new ETHViewHolder(this,findViewById(R.id.layout_index_1),bhChain);
        mBottomLayoutVH = new ChainBottomLayoutVH(this,findViewById(R.id.layout_bottom),bhChain.chain,mBalance!=null?mBalance.symbol:null);
        mBottomLayoutVH.initContentView();
        setTokenAddress();
        //
        if(bhChain.single_coin){
            findViewById(R.id.tv_add_token).setVisibility(View.GONE);
        }

    }

    //设置地址
    private void setTokenAddress() {
        //mETHViewHolder.initViewContent(mBalance);
        mETHViewHolder.initViewContent(bhChain);
    }

    @Override
    protected void addEvent() {
        mBalanceViewModel = ViewModelProviders.of(MainActivityManager._instance.mainActivity).get(BalanceViewModel.class).build(this);
        //资产订阅
        LiveDataBus.getInstance().with(BHConstants.Label_Account, LoadDataModel.class).observe(this, ldm->{
            defRefreshCount1++;
            if(ldm.loadingStatus== LoadingStatus.SUCCESS){
                updateAssets((AccountInfo) ldm.getData());
            }
            finishRefresh();
        });

        mChainTokenViewModel =  ViewModelProviders.of(ChainTokenActivity.this).get(ChainTokenViewModel.class);
        mChainTokenViewModel.mutableLiveData.observe(this,ldm->{
            defRefreshCount2++;
            updateAssetList((List<BHTokenItem>)ldm.getData());
            finishRefresh();
        });

        mBalanceAdapter.setOnItemClickListener((adapter, view, position) -> {
            BHTokenItem bhTokenItem = mBalanceAdapter.getData().get(position);
            BHBalance bhBalance = BHBalanceHelper.getBHBalanceFromAccount(bhTokenItem.symbol);

            Postcard postcard = ARouter.getInstance().build(ARouterConfig.Balance.Balance_Token_Detail)
                    .withString(BHConstants.SYMBOL,bhBalance.symbol);
            LogisticsCenter.completion(postcard);
            Intent intent = new Intent(this, postcard.getDestination());
            intent.putExtras(postcard.getExtras());
            startActivity(intent);
        });

        findViewById(R.id.tv_add_token).setOnClickListener(v -> {
            ARouter.getInstance()
                    .build(ARouterConfig.Balance.Balance_Search)
                    .withString(BHConstants.CHAIN,mBalance.chain)
                    .navigation();
        });
    }

    @Override
    protected void initPresenter() {
        mPresenter = new BalancePresenter(this);
    }

    private void updateAssets(AccountInfo accountInfo) {
        BHUserManager.getInstance().setAccountInfo(accountInfo);
        //重新计算地址
        if(!BHConstants.BHT_TOKEN.equalsIgnoreCase(mBalance.symbol)){
            mBalance = BHBalanceHelper.getBHBalanceFromAccount(mBalance.symbol);
            setTokenAddress();
        }
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        defRefreshCount1 = 0;
        defRefreshCount2 = 0;
        mBalanceViewModel.getAccountInfo(this,null);
        mChainTokenViewModel.loadBalanceByChain(this,mBalance.chain);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100){
            refreshLayout.autoRefresh();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mChainTokenViewModel.loadBalanceByChain(this,mBalance.chain);
    }

    public void applyTestToken(){
        mChainTokenViewModel.send_test_token(this,"hbc","kiwi");
    }

    public void updateAssetList(List<BHTokenItem> list){
        mBalanceAdapter.clear();
        if(ToolUtils.checkListIsEmpty(list)){
            empty_layout.showNoData();
            mBalanceAdapter.notifyDataSetChanged();
            return;
        }
        empty_layout.loadSuccess();
        mTokenList = list;
        mBalanceAdapter.addData(mTokenList);
        mBalanceAdapter.notifyDataSetChanged();
    }

    private void finishRefresh(){
        if(defRefreshCount1>=1 && defRefreshCount2>=1){
            refreshLayout.finishRefresh();
        }
    }

    @Subscribe(threadMode= ThreadMode.MAIN)
    public void changeAccount(TransctionEvent transctionEvent){
        mETHViewHolder.setTokenAddress(mBalance.symbol);
    }

    /**
     * 生成跨链地址
     */
    /*
      public void generateCrossLinkAddress() {
        *//*List<TxReq.TxMsg> tx_msg_list = BHRawTransaction.createGenerateAddressMsg(bhChain.chain);
        mTransactionViewModel.transferInnerExt(this,password,feeAmount,tx_msg_list);*//*
        *//*Password30Fragment.showPasswordDialog(getSupportFragmentManager(),
                Password30Fragment.class.getName(),
                this,0);*//*
    }*/

    //生成跨链地址
    /*public void generateCrossLinkAddress() {
        PasswordFragment fragment = PasswordFragment.showPasswordDialogExt(getSupportFragmentManager(),
                Password30Fragment.class.getName(),
                passwordClickListener,0);
        String subTitle = String.format(getString(R.string.generate_address_fee),
                BHUserManager.getInstance().getDefaultGasFee().displayFee+BHConstants.BHT_TOKEN.toUpperCase());
        fragment.setTv_sub_title(subTitle);
        fragment.show(getSupportFragmentManager(),ChainTokenActivity.class.getName());
    }*/

    /*PasswordFragment.PasswordClickListener passwordClickListener = ((password, position, way) -> {
        BHBalance bhtBalance = BHBalanceHelper.getBHBalanceFromAccount(BHConstants.BHT_TOKEN);
        if(TextUtils.isEmpty(bhtBalance.amount) ||
                Double.valueOf(bhtBalance.amount)<=Double.valueOf(BHUserManager.getInstance().getDefaultGasFee().displayFee)){
            ToastUtils.showToast(getString(R.string.not_have_amount)+BHConstants.BHT_TOKEN.toUpperCase());
            return;
        }

        List<TxReq.TxMsg> tx_msg_list = BHRawTransaction.createGenerateAddressMsg(mBalance.symbol);
        mTransactionViewModel.transferInnerExt(this,password,BHUserManager.getInstance().getDefaultGasFee().displayFee,tx_msg_list);
    });*/

}