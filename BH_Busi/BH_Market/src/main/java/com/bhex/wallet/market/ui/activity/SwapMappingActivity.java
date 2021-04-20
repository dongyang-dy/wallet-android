package com.bhex.wallet.market.ui.activity;

import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.lib.uikit.widget.editor.WithDrawInput;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
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
import com.bhex.wallet.common.tx.TxReq;
import com.bhex.wallet.common.ui.fragment.Password30PFragment;
import com.bhex.wallet.common.utils.LiveDataBus;
import com.bhex.wallet.common.viewmodel.BalanceViewModel;
import com.bhex.wallet.market.R;
import com.bhex.wallet.market.R2;
import com.bhex.wallet.market.ui.SymbolViewHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 映射
 */
@Route(path = ARouterConfig.Market_swap_mapping, name = "映射")
public class SwapMappingActivity extends BaseActivity
        implements Password30PFragment.PasswordClickListener,
        ChooseTokenFragment.ChooseTokenListener {

    @Autowired(name = BHConstants.SYMBOL)
    String mSymbol;

    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;
    @BindView(R2.id.layout_coin)
    LinearLayout layout_coin;
    @BindView(R2.id.layout_target)
    LinearLayout layout_target;
    @BindView(R2.id.tv_target_token)
    AppCompatTextView tv_target_token;
    @BindView(R2.id.iv_target)
    AppCompatImageView iv_target;
    @BindView(R2.id.tv_coin_token)
    AppCompatTextView tv_coin_token;
    @BindView(R2.id.iv_coin)
    AppCompatImageView iv_coin;
    @BindView(R2.id.ed_transfer_coin_amount)
    WithDrawInput ed_transfer_coin_amount;
    @BindView(R2.id.ed_transfer_target_amount)
    WithDrawInput ed_transfer_target_amount;

    private SymbolViewHolder mSymbolViewHolder;

    private BalanceViewModel mBalanceViewModel;
    protected TransactionViewModel mTransactionViewModel;

    private BHTokenMapping mTokenMapping;
    private List<BHTokenMapping> mMappingToknList;

    private BHBalance mBhtBalance;
    private BHBalance mTokenBalance;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_swap_mapping;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        tv_center_title.setText(getString(R.string.mapping));

        GradientDrawable drawable = ShapeUtils.getRoundRectDrawable(PixelUtils.dp2px(this,4),
                ColorUtil.getColor(this,R.color.secondary_input_background));
        layout_coin.setBackground(drawable);

        layout_target.setBackground(drawable);

        mSymbolViewHolder = new SymbolViewHolder();
        mSymbolViewHolder.bindView(findViewById(R.id.root_view),mSymbol);

        findViewById(R.id.layout_top).bringToFront();
        mTokenMapping = CacheCenter.getInstance().getTokenMapCache().getTokenMappingOne(mSymbol.toUpperCase());
        mMappingToknList = CacheCenter.getInstance().getTokenMapCache().getTokenMapping(mSymbol.toUpperCase());

        if(mTokenMapping==null){
            mTokenMapping = CacheCenter.getInstance().getTokenMapCache().getTokenMappingFrist();
            mSymbol = (mTokenMapping!=null) ? mTokenMapping.coin_symbol:"";
        }
        if(mTokenMapping==null){
            return;
        }
        mSymbolViewHolder.setTokenAsset(this,mSymbol);
    }

    @Override
    protected void addEvent() {
        mTokenBalance = BHBalanceHelper.getBHBalanceFromAccount(mSymbol);
        mBhtBalance = BHBalanceHelper.getBHBalanceFromAccount(BHConstants.BHT_TOKEN);

        mBalanceViewModel = ViewModelProviders.of(MainActivityManager._instance.mainActivity)
                .get(BalanceViewModel.class)
                .build(MainActivityManager._instance.mainActivity);

        //资产订阅
        LiveDataBus.getInstance().with(BHConstants.Label_Account, LoadDataModel.class).observe(this, ldm -> {
            if (ldm.loadingStatus == LoadingStatus.SUCCESS) {
                updateAssets((AccountInfo) ldm.getData());
            }
        });

        mTransactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);
        mTransactionViewModel.mutableLiveData.observe(this, ldm -> {
            updateTransferStatus(ldm);
        });

    }

    @OnClick({R2.id.btn_send_swapmapping,R2.id.layout_coin,R2.id.layout_target,R2.id.iv_exchange_action})
    public void onClickView(View view) {
        if(R.id.layout_coin==view.getId()){
            ChooseTokenFragment frg = ChooseTokenFragment.showDialog(mSymbol,0,this);
            frg.show(getSupportFragmentManager(),ChooseTokenFragment.class.getName());
        }else if(R.id.layout_target==view.getId()){
            if(ToolUtils.checkListIsEmpty(mMappingToknList) || mMappingToknList.size()<=1){
                return;
            }
            ChooseTokenFragment frg = ChooseTokenFragment.showDialog(tv_coin_token.getText().toString(),1,chooseTokenListener);
            frg.mTargetSymbol = tv_target_token.getText().toString();
            frg.show(getSupportFragmentManager(),ChooseTokenFragment.class.getName());
        }else if (view.getId() == R.id.btn_send_swapmapping) {
            sendSwapMapping();
        }else if(view.getId() == R.id.iv_exchange_action){
            exchangeAction();
        }
    }

    private void updateAssets(AccountInfo accountInfo){
        //更新余额
        mSymbolViewHolder.updateBalance(this);

        mBhtBalance = BHBalanceHelper.getBHBalanceFromAccount(BHConstants.BHT_TOKEN);
        mTokenBalance = BHBalanceHelper.getBHBalanceFromAccount(mSymbol);
    }

    private void sendSwapMapping() {
        String map_amount = ed_transfer_coin_amount.getInputString();
        if (TextUtils.isEmpty(map_amount)) {
            ToastUtils.showToast(getString(R.string.please_input_exchange_amount));
            return;
        }

        BHToken symbolToken = CacheCenter.getInstance().getTokenMapCache().getBHToken(mSymbol);
        if(symbolToken==null){
            ToastUtils.showToast(getResources().getString(R.string.choose_token));
            return;
        }
        if (mTokenBalance == null || Double.valueOf(mTokenBalance.amount) <= 0) {
            ToastUtils.showToast(getResources().getString(R.string.not_have_amount) + symbolToken.name.toUpperCase());
            return;
        }

        if (Double.valueOf(mTokenBalance.amount) < Double.valueOf(map_amount)) {
            ToastUtils.showToast(getResources().getString(R.string.not_have_amount) + symbolToken.name.toUpperCase());
            return;
        }

        if (mBhtBalance == null || Double.valueOf(mBhtBalance.amount) <= 0) {
            ToastUtils.showToast(getResources().getString(R.string.not_have_amount) + BHConstants.BHT_TOKEN.toUpperCase());
            return;
        }

        Password30PFragment.showPasswordDialog(getSupportFragmentManager(),
                Password30PFragment.class.getName(),
                this, 0,true);
    }

    private void exchangeAction(){
        if(mTokenMapping==null){
            return;
        }

        mSymbol = mTokenMapping.target_symbol;
        mTokenMapping = CacheCenter.getInstance().getTokenMapCache().getTokenMappingOne(mSymbol.toUpperCase());
        mTokenBalance = BHBalanceHelper.getBHBalanceFromAccount(mSymbol);
        mSymbolViewHolder.setTokenAsset(this,mSymbol);
    }

    @Override
    public void confirmAction(String password, int position, int way) {
        if(mTokenMapping==null){
            return;
        }
        String coin_symbol = mTokenMapping.coin_symbol;
        String issue_symbol = mTokenMapping.issue_symbol;
        String map_amount = ed_transfer_coin_amount.getInputString();
        List<TxReq.TxMsg> tx_msg_list = BHRawTransaction.createSwapMappingMsg(issue_symbol,coin_symbol,map_amount);
        mTransactionViewModel.transferInnerExt(this,password, BHUserManager.getInstance().getDefaultGasFee().displayFee,tx_msg_list);

    }

    @Override
    public void clickTokenPosition(BHTokenMapping tokenMapping) {
        mTokenMapping = tokenMapping;
        mSymbol = mTokenMapping.coin_symbol;
        mTokenBalance = BHBalanceHelper.getBHBalanceFromAccount(mSymbol);
        mMappingToknList = CacheCenter.getInstance().getTokenMapCache().getTokenMapping(mSymbol.toUpperCase());
        mSymbolViewHolder.setTokenAsset(this,mSymbol);
    }

    //更新兑换状态
    private void updateTransferStatus(LoadDataModel ldm) {
        if (ldm.loadingStatus == LoadingStatus.SUCCESS) {
            ed_transfer_coin_amount.setInputString("");
            ed_transfer_target_amount.setInputString("");
            ToastUtils.showToast(getString(R.string.transfer_in_success));
        } else {
            ToastUtils.showToast(getString(R.string.transfer_in_fail));
        }
    }


    private ChooseTokenFragment.ChooseTokenListener chooseTokenListener = item -> {
        tv_target_token.setText(item.target_symbol.toUpperCase());
        mTokenMapping = item;
        BHBalanceHelper.loadTokenIcon(this,iv_target,mTokenMapping.target_symbol.toUpperCase());
        mSymbolViewHolder.setTokenAsset(this,mSymbol);
    };
}
