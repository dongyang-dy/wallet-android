package com.bhex.wallet.bh_main.validator.ui.activity;

import android.text.InputType;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.editor.WithDrawInput;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.indicator.OnSampleSeekChangeListener;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.viewmodel.TransactionViewModel;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.R2;
import com.bhex.wallet.bh_main.validator.enums.ENTRUST_BUSI_TYPE;
import com.bhex.wallet.bh_main.validator.model.ValidatorDelegationInfo;
import com.bhex.wallet.bh_main.validator.model.ValidatorInfo;
import com.bhex.wallet.bh_main.validator.presenter.DoEntrustPresenter;
import com.bhex.wallet.bh_main.validator.viewmodel.EnstrustViewModel;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.AccountInfo;
import com.bhex.wallet.common.tx.BHRawTransaction;
import com.bhex.wallet.common.tx.TxReq;
import com.bhex.wallet.common.ui.fragment.Password30PFragment;
import com.bhex.wallet.common.utils.LiveDataBus;
import com.google.android.material.button.MaterialButton;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.SeekParams;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author zc
 * 2020-4-16
 * 委托
 */
@Route(path = ARouterConfig.Validator.Do_Entrust)
public class DoEntrustActivity extends BaseActivity<DoEntrustPresenter> implements Password30PFragment.PasswordClickListener {

    @Autowired(name = "validatorInfo")
    ValidatorInfo mValidatorInfo;
    @Autowired(name = "bussiType")
    int mBussiType;
    @BindView(R2.id.tv_tips)
    AppCompatTextView tv_tips;
    @BindView(R2.id.tv_to_address)
    WithDrawInput tv_to_address;
    @BindView(R2.id.tv_available_amount)
    AppCompatTextView tv_available_amount;
    @BindView(R2.id.ed_entrust_amount)
    WithDrawInput ed_entrust_amount;
    @BindView(R2.id.ed_entrust_fee)
    WithDrawInput ed_entrust_fee;
    @BindView(R2.id.ed_real_entrust_amount)
    WithDrawInput ed_real_entrust_amount;
    @BindView(R2.id.tv_tips_1)
    AppCompatTextView tv_tips_1;
    @BindView(R2.id.tv_tips_2)
    AppCompatTextView tv_tips_2;
    @BindView(R2.id.tv_tips_3)
    AppCompatTextView tv_tips_3;
    @BindView(R2.id.btn_do_entrust)
    MaterialButton btn_do_entrust;
    @BindView(R2.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R2.id.tv_entrust_to_title)
    AppCompatTextView tv_entrust_to_title;
    @BindView(R2.id.tv_entrust_amount_title)
    AppCompatTextView tv_entrust_amount_title;
    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;
    @BindView(R2.id.tv_fee_available_amount)
    AppCompatTextView tv_fee_available_amount;
    @BindView(R2.id.sb_tx_fee)
    IndicatorSeekBar sb_tx_fee;

    private String token = BHConstants.BHT_TOKEN;

    EnstrustViewModel mEnstrustViewModel;
    TransactionViewModel transactionViewModel;
    //BalanceViewModel mBalanceViewModel;
    String mAvailabelTitle = "";

    String validatorAddress = "";
    private String available_amount;
    private String wallet_available;

    @Override
    protected void initPresenter() {
        mPresenter = new DoEntrustPresenter(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_do_entrust;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        sb_tx_fee.setDecimalScale(4);
        initValidatorView();
    }


    private void initValidatorView() {
        tv_to_address.getEditText().setEnabled(false);
        refreshLayout.setEnableLoadMore(false);

        if (mBussiType == ENTRUST_BUSI_TYPE.TRANFER_ENTRUS.getTypeId()) {
            if (mValidatorInfo != null) {
                String address = mValidatorInfo.getOperator_address();
                if (mValidatorInfo.getDescription() != null) {
                    address = mValidatorInfo.getDescription().getMoniker() + "-" + addressReplace(address);
                }
                validatorAddress = mValidatorInfo.getOperator_address();
                tv_to_address.getEditText().setText(address);

                tv_to_address.btn_right_text.setVisibility(View.GONE);
                tv_to_address.iv_right.setVisibility(View.VISIBLE);
                tv_to_address.iv_right.setImageResource(R.drawable.ic_arrowdown);
                tv_to_address.setOnClickListener(v -> {
                    // TODO 转委托暂时不做

                });
            }
            mAvailabelTitle = getString(R.string.avilable_transfer);
            tv_entrust_to_title.setText(getString(R.string.source_address));
            tv_entrust_amount_title.setText(getString(R.string.transfer_entrust_amount));
            btn_do_entrust.setText(getString(R.string.re_entrust));
            tv_center_title.setText(getString(R.string.re_entrust));
        } else if (mBussiType == ENTRUST_BUSI_TYPE.DO_ENTRUS.getTypeId()) {
            tv_fee_available_amount.setVisibility(View.GONE);
            if (mValidatorInfo != null) {
                String address = mValidatorInfo.getOperator_address();
                validatorAddress = mValidatorInfo.getOperator_address();
                if (mValidatorInfo.getDescription() != null) {
                    address = mValidatorInfo.getDescription().getMoniker() + "-" + addressReplace(address);
                }
                tv_to_address.getEditText().setText(address);
            }

            mAvailabelTitle = getString(R.string.available)+" ";
            tv_entrust_to_title.setText(getString(R.string.entrust_to));
            tv_entrust_amount_title.setText(getString(R.string.entrust_amount));
            btn_do_entrust.setText(getString(R.string.do_entrust));
            tv_center_title.setText(getString(R.string.do_entrust));
        } else {
            if (mValidatorInfo != null) {
                validatorAddress = mValidatorInfo.getOperator_address();
            }
            String address = getString(R.string.my_trusteeship) + "-" + addressReplace(BHUserManager.getInstance().getCurrentBhWallet().address);
            tv_to_address.getEditText().setText(address);
            mAvailabelTitle = getString(R.string.avilable_relieve)+" ";
            tv_entrust_to_title.setText(getString(R.string.relieve_entrust_to));
            tv_entrust_amount_title.setText(getString(R.string.relieve_entrust_amount));
            btn_do_entrust.setText(getString(R.string.relieve_entrust));
            tv_center_title.setText(getString(R.string.relieve_entrust));
            tv_fee_available_amount.setVisibility(View.VISIBLE);
            tv_fee_available_amount.setText(" " + getString(R.string.string_placeholder) + token.toUpperCase());
            ed_entrust_amount.getEditText().setHint(getString(R.string.hint_enter_relieve_delegate));
        }
        ed_entrust_amount.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        ed_real_entrust_amount.btn_right_text.setText(token.toUpperCase());
        ed_entrust_fee.btn_right_text.setText(token.toUpperCase());
        ed_entrust_fee.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //ed_entrust_fee.getEditText().setText(BHConstants.BHT_DEFAULT_FEE);
        ed_entrust_fee.setInputString(BHUserManager.getInstance().getDefaultGasFee().displayFee);
        tv_available_amount.setText(" " + getString(R.string.string_placeholder) + token.toUpperCase());
    }

    @Override
    protected void addEvent() {
        ed_entrust_amount.btn_right_text.setOnClickListener(allListener);

        mEnstrustViewModel = ViewModelProviders.of(this).get(EnstrustViewModel.class);
        mEnstrustViewModel.mutableLiveData.observe(this, ldm -> {
            updateDoEntrustStatus(ldm);
        });

        transactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);
        transactionViewModel.mutableLiveData.observe(this, ldm -> {
            updateDoEntrustStatus(ldm);
        });


        LiveDataBus.getInstance().with(BHConstants.Label_Account,LoadDataModel.class).observe(this,ldm->{
            if (ldm.loadingStatus == LoadingStatus.SUCCESS) {
                updateAssets((AccountInfo) ldm.getData());
            }
            refreshLayout.finishRefresh();

        });
        mEnstrustViewModel.delegationLiveData.observe(this, ldm -> {
            if (ldm.loadingStatus == LoadingStatus.SUCCESS) {
                updateValidatorAssets(ldm.getData());
            }
            refreshLayout.finishRefresh();
        });
        queryAssetInfo(true);

        refreshLayout.setOnRefreshListener(refreshLayout1 -> {
            queryAssetInfo(false);
        });

        sb_tx_fee.setOnSeekChangeListener(new OnSampleSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                super.onSeeking(seekParams);
                ed_entrust_fee.setInputString(seekParams.progressFloat+"");
            }
        });
    }

    private String addressReplace(String originAddress) {
        if (originAddress == null || TextUtils.isEmpty(originAddress)) {
            return "";
        }
        if (originAddress.length() < 21) {
            return originAddress;
        }
        return originAddress.replace(originAddress.substring(10, originAddress.length() - 10), "...");
    }

    private void queryAssetInfo(boolean isShowProgressDialog) {
        if (mBussiType == ENTRUST_BUSI_TYPE.DO_ENTRUS.getTypeId()) {
            mEnstrustViewModel.getAccountInfo(this,isShowProgressDialog);
        } else  if (mBussiType == ENTRUST_BUSI_TYPE.RELIEVE_ENTRUS.getTypeId()) {
            mEnstrustViewModel.getCustDelegations(this, isShowProgressDialog);
            mEnstrustViewModel.getAccountInfo(this,isShowProgressDialog);
        }
    }


    private void updateDoEntrustStatus(LoadDataModel ldm) {
        if (ldm.loadingStatus == LoadingStatus.SUCCESS) {
            if (mBussiType == ENTRUST_BUSI_TYPE.DO_ENTRUS.getTypeId()) {
                ToastUtils.showToast(getString(R.string.do_entrust_succeed));
            } else if (mBussiType == ENTRUST_BUSI_TYPE.RELIEVE_ENTRUS.getTypeId()) {
                ToastUtils.showToast(getString(R.string.relieve_entrust_succeed));
            } else if (mBussiType == ENTRUST_BUSI_TYPE.TRANFER_ENTRUS.getTypeId()) {
                ToastUtils.showToast(getString(R.string.transfer_entrust_succeed));
            }
            finish();
        } else {
            if (mBussiType == ENTRUST_BUSI_TYPE.DO_ENTRUS.getTypeId()) {
                ToastUtils.showToast(getString(R.string.do_entrust_failed));
            } else if (mBussiType == ENTRUST_BUSI_TYPE.RELIEVE_ENTRUS.getTypeId()) {
                ToastUtils.showToast(getString(R.string.relieve_entrust_failed));
            } else if (mBussiType == ENTRUST_BUSI_TYPE.TRANFER_ENTRUS.getTypeId()) {
                ToastUtils.showToast(getString(R.string.transfer_entrust_failed));
            }
        }
    }

    @OnClick({R2.id.btn_do_entrust})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.btn_do_entrust) {
            if (mBussiType == ENTRUST_BUSI_TYPE.DO_ENTRUS.getTypeId()) {
                sendDoEntrust();
            } else if (mBussiType == ENTRUST_BUSI_TYPE.RELIEVE_ENTRUS.getTypeId()) {
                sendRelieveEntrust();
            } else if (mBussiType == ENTRUST_BUSI_TYPE.TRANFER_ENTRUS.getTypeId()) {
                sendTransferEntrust();
            }
        }
    }

    private void sendTransferEntrust() {
        // TODO 转委托暂时不做
    }

    private void sendRelieveEntrust() {
        boolean flag = mPresenter.checkReliveEntrust( ed_entrust_amount.getInputString(),wallet_available,
                available_amount,
                ed_entrust_fee.getInputString());

        if (!flag) {
            return;
        }

        Password30PFragment.showPasswordDialog(getSupportFragmentManager(),
                Password30PFragment.class.getName(),
                this,0,true);
    }

    /**
     * 发送交易
     */
    private void sendDoEntrust() {
        boolean flag = mPresenter.checkDoEntrust(
                ed_entrust_amount.getInputString(),String.valueOf(available_amount),
                ed_entrust_fee.getInputString()
        );
        if (!flag) {
            return;
        }

        Password30PFragment.showPasswordDialog(getSupportFragmentManager(),
                Password30PFragment.class.getName(),
                this,0,true);
    }


    public View.OnClickListener allListener = v -> {
        if (available_amount == null || TextUtils.isEmpty(available_amount)) {
            ed_entrust_amount.getEditText().setText("");
            return;
        }
        String fee = ed_entrust_fee.getInputString();
        if(mBussiType == ENTRUST_BUSI_TYPE.DO_ENTRUS.getTypeId()){
            String all_count = NumberUtil.sub(String.valueOf(available_amount), fee);
            ed_entrust_amount.setInputString(all_count);
        }else if(mBussiType == ENTRUST_BUSI_TYPE.RELIEVE_ENTRUS.getTypeId()){
            String all_count = String.valueOf(available_amount);
            ed_entrust_amount.setInputString(all_count);
        }
        //ed_entrust_amount.getEditText().setSelection(ed_entrust_amount.getInputString().length());
    };

    private void updateAssets(AccountInfo data) {
        if (data == null) {
            return;
        }
        List<AccountInfo.AssetsBean> list = data.assets;
        if (ToolUtils.checkListIsEmpty(list)) {
            return;
        }
        for (AccountInfo.AssetsBean item : list) {
            if (!item.symbol.equalsIgnoreCase(token)) {
                continue;
            }

            if (mBussiType == ENTRUST_BUSI_TYPE.DO_ENTRUS.getTypeId()) {
                available_amount = mPresenter.getAmountForUser(item.amount, item.frozen_amount, token);
                tv_available_amount.setText(mAvailabelTitle + available_amount + token.toUpperCase());
            } else if (mBussiType == ENTRUST_BUSI_TYPE.RELIEVE_ENTRUS.getTypeId()) {
                wallet_available = mPresenter.getAmountForUser(item.amount, item.frozen_amount, token);
                tv_fee_available_amount.setText(getString(R.string.available) + wallet_available + token.toUpperCase());
            }
        }
    }

    private void updateValidatorAssets(List<ValidatorDelegationInfo> data) {
        if (data == null || data.size() < 1) {
            return;
        }

        for (ValidatorDelegationInfo item : data) {
            if (!item.getValidator().equalsIgnoreCase(validatorAddress)) {
                continue;
            }
            String asset = item.getBonded();
            available_amount = mPresenter.getAmountForUser(asset, "0", token);
            tv_available_amount.setText(mAvailabelTitle + available_amount + token.toUpperCase());
        }
    }

    /*public String getAmountForUser(String amount, String frozen_amount, String symbol) {
        SymbolCache symbolCache = CacheCenter.getInstance().getSymbolCache();
        BHToken bhToken = symbolCache.getBHToken(symbol.toLowerCase());
        int decimals = bhToken!=null?bhToken.decimals:2;
        decimals = 0;
        double tmp = NumberUtil.sub(amount,frozen_amount);
        double displayAmount = NumberUtil.divide(String.valueOf(tmp), Math.pow(10,decimals)+"");

        //LogUtils.d("BHBalanceHelper==>:","displayAmount==="+displayAmount);
        //DecimalFormat format = new DecimalFormat();
        return NumberUtil.dispalyForUsertokenAmount(String.valueOf(displayAmount));
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void confirmAction(String password, int position,int way) {

        if (mBussiType == ENTRUST_BUSI_TYPE.DO_ENTRUS.getTypeId())  {

            String validator_address = validatorAddress;
            String entrustDrawAmount = ed_entrust_amount.getInputString();
            String feeAmount = ed_entrust_fee.getInputString();

            List<TxReq.TxMsg> tx_msg_list = BHRawTransaction.createDoEntrustMsg(validator_address,entrustDrawAmount,token);
            transactionViewModel.transferInnerExt(this,password,feeAmount,tx_msg_list);
        } else if (mBussiType == ENTRUST_BUSI_TYPE.RELIEVE_ENTRUS.getTypeId())  {

            String validator_address = validatorAddress;
            String unEntrustDrawAmount = ed_entrust_amount.getInputString();
            String feeAmount = ed_entrust_fee.getInputString();

            /*BHTransactionManager.loadSuquece(suquece -> {
                BHSendTranscation bhSendTranscation = BHTransactionManager.relieveEntrust( validator_address, entrustDrawAmount, feeAmount,
                         password, suquece, token);
                mEnstrustViewModel.sendDoEntrust(this, bhSendTranscation);
                return 0;
            });*/

            List<TxReq.TxMsg> tx_msg_list = BHRawTransaction.createUnEntrustMsg(validator_address,unEntrustDrawAmount,token);
            transactionViewModel.transferInnerExt(this,password,feeAmount,tx_msg_list);


        }

    }
}
