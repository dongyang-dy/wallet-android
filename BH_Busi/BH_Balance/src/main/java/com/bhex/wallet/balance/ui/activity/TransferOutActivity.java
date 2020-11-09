package com.bhex.wallet.balance.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib_qr.XQRCode;
import com.bhex.lib_qr.util.QRCodeAnalyzeUtils;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.network.cache.stategy.CacheStrategy;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.indicator.OnSampleSeekChangeListener;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.tools.utils.PathUtils;
import com.bhex.tools.utils.RegexUtil;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.R2;
import com.bhex.wallet.balance.event.TransctionEvent;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.balance.presenter.TransferOutPresenter;
import com.bhex.wallet.balance.viewmodel.TokenViewModel;
import com.bhex.wallet.balance.viewmodel.TransactionViewModel;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.MainActivityManager;
import com.bhex.wallet.common.model.AccountInfo;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.tx.BHRawTransaction;
import com.bhex.wallet.common.tx.TxReq;
import com.bhex.wallet.common.ui.activity.BHQrScanActivity;
import com.bhex.wallet.common.ui.fragment.Password30Fragment;
import com.bhex.wallet.common.ui.fragment.PasswordFragment;
import com.bhex.wallet.common.utils.LiveDataBus;
import com.bhex.wallet.common.viewmodel.BalanceViewModel;
import com.warkiz.widget.SeekParams;

import org.greenrobot.eventbus.EventBus;

import java.math.BigInteger;
import java.util.List;

import butterknife.OnClick;

/**
 * @author gongdongyang
 * 2020-4-7 12:04:20
 * 提币
 */

@Route(path = ARouterConfig.Balance_transfer_out)
public class TransferOutActivity extends BaseTransferOutActivity<TransferOutPresenter>
        implements Password30Fragment.PasswordClickListener{

    @Autowired(name = "balance")
    BHBalance balance;

    @Autowired(name = "bhtBalance")
    BHBalance bhtBalance;

    @Autowired(name="way")
    int way;



    //跨链提币可用余额
    BHBalance feeBalance;
    int def_dailog_count = 0;
    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        initTokenView();
        bhToken = SymbolCache.getInstance().getBHToken(balance.symbol.toLowerCase());
        feeBalance = BHBalanceHelper.getBHBalanceFromAccount(getBalance().chain);

        tv_reach_amount.btn_right_text.setText( balance.name.toUpperCase());

        ed_transfer_amount.btn_right_text.setOnClickListener(allWithDrawListener);
        tv_reach_amount.getEditText().setEnabled(false);


        transactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);
        transactionViewModel.mutableLiveData.observe(this,ldm -> {
            updateTransferStatus(ldm);
        });
        tv_to_address.btn_right_text.setVisibility(View.GONE);
        tv_to_address.iv_right.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)tv_to_address.getEditText().getLayoutParams();
        lp.addRule(RelativeLayout.LEFT_OF,R.id.iv_right);
        tv_to_address.getEditText().setLayoutParams(lp);

        refreshLayout.setOnRefreshListener(refreshLayout -> {
            def_dailog_count = 0;
            balanceViewModel.getAccountInfo(this, CacheStrategy.onlyRemote());
            tokenViewModel.queryToken(this,getBalance().symbol);
        });
        refreshLayout.autoRefresh();
        updateAvailableView();
    }

    @Override
    protected void initPresenter() {
        mPresenter = new TransferOutPresenter(this);
    }

    /**
     * 手续费
     * @param ldm
     */
    private void updateTransferStatus(LoadDataModel ldm) {
        if(ldm.loadingStatus== LoadingStatus.SUCCESS){
            ToastUtils.showToast(getResources().getString(R.string.transfer_in_success));
            EventBus.getDefault().post(new TransctionEvent());
            finish();
        }else{
            ToastUtils.showToast(getResources().getString(R.string.transfer_in_fail));
        }
    }

    @Override
    protected void addEvent() {
        mCurrentBhWallet = BHUserManager.getInstance().getCurrentBhWallet();
        //二维码扫描
        tv_to_address.iv_right.setOnClickListener(v -> {
            ARouter.getInstance().build(ARouterConfig.Commom_scan_qr).navigation(this, BHQrScanActivity.REQUEST_CODE);
        });

        sb_tx_fee.setOnSeekChangeListener(new OnSampleSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                super.onSeeking(seekParams);
                et_tx_fee.setInputString(seekParams.progressFloat+"");
            }
        });

        balanceViewModel = ViewModelProviders.of(MainActivityManager._instance.mainActivity).get(BalanceViewModel.class).build(MainActivityManager._instance.mainActivity);
        //资产订阅
        LiveDataBus.getInstance().with(BHConstants.Label_Account, LoadDataModel.class).observe(this, ldm->{
            if(ldm.loadingStatus==LoadingStatus.SUCCESS){
                updateAssets((AccountInfo) ldm.getData());
            }
            refreshFinish();
        });

        tokenViewModel = ViewModelProviders.of(TransferOutActivity.this).get(TokenViewModel.class);
        tokenViewModel.queryLiveData.observe(this,ldm->{
            if(ldm.loadingStatus==LoadingStatus.SUCCESS){
                bhToken = (BHToken) ldm.getData();
            }
            refreshFinish();
        });
    }

    public View.OnClickListener allWithDrawListener = v -> {
        if(way==BH_BUSI_TYPE.跨链转账.getIntValue()){
            if(balance.symbol.equalsIgnoreCase(balance.chain) ){
                if(RegexUtil.checkNumeric(et_withdraw_fee.getInputString()) && Double.valueOf(et_withdraw_fee.getInputString())<Double.valueOf(bhToken.withdrawal_fee)){
                    et_withdraw_fee.getEditText().setText(bhToken.withdrawal_fee);
                }
                String all_count = NumberUtil.sub(String.valueOf(available_amount),et_withdraw_fee.getInputString());
                all_count = Double.valueOf(all_count)<0?"0":all_count;
                ed_transfer_amount.getEditText().setText(all_count);
            }else{
                ed_transfer_amount.getEditText().setText(NumberUtil.toPlainString(available_amount));
            }
        }else if(balance.symbol.equalsIgnoreCase(BHConstants.BHT_TOKEN)){
            //交易手续费
            String all_count = NumberUtil.sub(String.valueOf(available_amount),et_tx_fee.getInputString());
            all_count = Double.valueOf(all_count)<0?"0":all_count;
            ed_transfer_amount.getEditText().setText(all_count);
        }else {
            ed_transfer_amount.getEditText().setText(NumberUtil.toPlainString(available_amount));
        }

    };

    @OnClick({R2.id.btn_drawwith_coin})
    public void onViewClicked(View view) {
        if(BHConstants.BHT_TOKEN.equalsIgnoreCase(balance.chain)){
            sendTransfer();
        }else if(way==BH_BUSI_TYPE.跨链转账.getIntValue()){
            crossLinkWithDraw();
        }else if(way==BH_BUSI_TYPE.链内转账.getIntValue()){
            sendTransfer();
        }
    }

    private void updateAssets(AccountInfo accountInfo) {
        BHUserManager.getInstance().setAccountInfo(accountInfo);
        balance = BHBalanceHelper.getBHBalanceFromAccount(balance.symbol);
        feeBalance = BHBalanceHelper.getBHBalanceFromAccount(getBalance().chain);
        bhtBalance = BHBalanceHelper.getBHBalanceFromAccount(BHConstants.BHT_TOKEN);
        updateAvailableView();
    }

    private void updateAvailableView(){
        String available_amount_str =  BHBalanceHelper.getAmountForUser(this,balance.amount,"0",balance.symbol);
        available_amount = Double.valueOf(available_amount_str);
        tv_available_amount.setText(getString(R.string.available)+" "+available_amount_str);

        String available_fee_amount_str =  BHBalanceHelper.getAmountForUser(this,feeBalance.amount,"0",feeBalance.symbol);
        tv_withdraw_fee_amount.setText(getString(R.string.available)+" "+available_fee_amount_str);

        if(bhtBalance!=null){
            String available_bht_amount_str =  BHBalanceHelper.getAmountForUser(this,bhtBalance.amount,"0",bhtBalance.symbol);
            tv_available_bht_amount.setText(getString(R.string.available)+" "+available_bht_amount_str);
        }
    }

    /**
     * 发送交易
     */
    private void sendTransfer(){
        boolean flag = mPresenter.checklinkInnerTransfer(tv_to_address.getInputString(),
                ed_transfer_amount.getInputStringTrim(),
                String.valueOf(available_amount),et_tx_fee.getInputStringTrim());

        if(flag){
            Password30Fragment.showPasswordDialog(getSupportFragmentManager(),
                    PasswordFragment.class.getName(),
                    this,0);
        }
    }

    /**
     * 跨链提币
     */
    private void crossLinkWithDraw(){
        BHToken bhToken = SymbolCache.getInstance().getBHToken(balance.symbol);

        boolean flag = mPresenter.checklinkOutterTransfer(tv_to_address.getEditText().getText().toString(),
                ed_transfer_amount.getInputStringTrim(),
                String.valueOf(available_amount),
                et_tx_fee.getInputStringTrim(),
                et_withdraw_fee.getInputStringTrim(),
                bhToken.withdrawal_fee,feeBalance
        );

        if(flag){
            Password30Fragment.showPasswordDialog(getSupportFragmentManager(),
                    PasswordFragment.class.getName(),
                    this,0);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //处理二维码扫描结果
        if (requestCode == BHQrScanActivity.REQUEST_CODE ) {
            if(resultCode == RESULT_OK){
                //处理扫描结果（在界面上显示）
                String qrCode  = data.getExtras().getString(XQRCode.RESULT_DATA);
                tv_to_address.getEditText().setText(qrCode);
            }else if(resultCode == BHQrScanActivity.REQUEST_IMAGE){
                getAnalyzeQRCodeResult(data.getData());
            }
        }
    }


    private void getAnalyzeQRCodeResult(Uri uri) {
        XQRCode.analyzeQRCode(PathUtils.getFilePathByUri(this, uri), new QRCodeAnalyzeUtils.AnalyzeCallback() {
            @Override
            public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                tv_to_address.getEditText().setText(result);
            }

            @Override
            public void onAnalyzeFailed() {
                ToastUtils.showToast(getResources().getString(R.string.encode_qr_fail));
            }
        });
    }

    /**
     * 密码对话框回调
     * @param password
     * @param position
     */
    @Override
    public void confirmAction(String password, int position,int verifyPwdWay) {
        //String from_address = mCurrentBhWallet.getAddress();
        String to_address = tv_to_address.getInputString();
        BigInteger gasPrice = BigInteger.valueOf ((long)(BHConstants.BHT_GAS_PRICE));
        //链内
        if(way==BH_BUSI_TYPE.链内转账.getIntValue()){
            String withDrawAmount = ed_transfer_amount.getInputStringTrim();
            String feeAmount = et_tx_fee.getInputString();
            //创建转账信息
            List<TxReq.TxMsg> tx_msg_list = BHRawTransaction.createTransferMsg(to_address,withDrawAmount,balance.symbol);
            transactionViewModel.transferInnerExt(this,password,feeAmount,tx_msg_list);

        }else if(way== BH_BUSI_TYPE.跨链转账.getIntValue()){//跨链
            //提币数量
            String withDrawAmount = ed_transfer_amount.getInputStringTrim();
            //交易手续费
            String feeAmount = et_tx_fee.getInputString();
            //提币手续费
            String withDrawFeeAmount = et_withdraw_fee.getInputString();
            //创建提币信息
            List<TxReq.TxMsg> tx_msg_list = BHRawTransaction.createwithDrawWMsg(to_address,withDrawAmount,withDrawFeeAmount,balance.symbol);
            transactionViewModel.transferInnerExt(this,password,feeAmount,tx_msg_list);
        }
    }

    @Override
    public BHBalance getBalance() {
        return balance;
    }

    @Override
    public int getWay() {
        return way;
    }

    public void refreshFinish(){
        def_dailog_count++;
        if(def_dailog_count==2){
            refreshLayout.finishRefresh();
        }
    }
}
