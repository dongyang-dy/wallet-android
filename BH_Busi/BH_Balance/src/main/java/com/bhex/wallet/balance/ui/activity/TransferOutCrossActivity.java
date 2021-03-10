package com.bhex.wallet.balance.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib_qr.XQRCode;
import com.bhex.lib_qr.util.QRCodeAnalyzeUtils;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.AndroidBug5497Workaround;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.PathUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.R2;
import com.bhex.wallet.balance.event.TransctionEvent;
import com.bhex.wallet.balance.ui.fragment.ChooseTokenFragment;
import com.bhex.wallet.balance.ui.viewhodler.TransferInVH;
import com.bhex.wallet.balance.ui.viewhodler.TransferOutCrossVH;
import com.bhex.wallet.balance.viewmodel.TokenViewModel;
import com.bhex.wallet.balance.viewmodel.TransactionViewModel;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.manager.MainActivityManager;
import com.bhex.wallet.common.model.AccountInfo;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.tx.BHRawTransaction;
import com.bhex.wallet.common.tx.TxReq;
import com.bhex.wallet.common.ui.activity.BHQrScanActivity;
import com.bhex.wallet.common.ui.fragment.Password30PFragment;
import com.bhex.wallet.common.utils.LiveDataBus;
import com.bhex.wallet.common.viewmodel.BalanceViewModel;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;

/**
 * @author gongdongyang
 * 提币
 * 2021-3-4 16:54:38
 */
@Route(path = ARouterConfig.Balance.Balance_transfer_out_cross,name = "提币")
public class TransferOutCrossActivity extends BaseActivity {

    @Autowired(name=BHConstants.SYMBOL)
    String m_symbol;

    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;

    private SmartRefreshLayout mRefreshLayout;
    int def_dailog_count = 0;

    private BalanceViewModel mBalanceViewModel;
    private TransactionViewModel mTransactionViewModel;
    private TokenViewModel mTokenViewModel;

    //
    TransferOutCrossVH transferOutCrossVH;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_transfer_out_cross;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        tv_center_title.setText(getString(R.string.cross_withdraw));
        mRefreshLayout = findViewById(R.id.refreshLayout);
        //AndroidBug5497Workaround.assistActivity(this);
        transferOutCrossVH = new TransferOutCrossVH(this,findViewById(R.id.root_view));

        transferOutCrossVH.layout_select_token.setOnClickListener(this::selectTokenAction);
        transferOutCrossVH.btn_drawwith_coin.setOnClickListener(this::withDrawAtion);
    }

    @Override
    protected void addEvent() {
        transferOutCrossVH.updateTokenInfo(m_symbol);
        mBalanceViewModel = ViewModelProviders.of(MainActivityManager._instance.mainActivity).get(BalanceViewModel.class).build(MainActivityManager._instance.mainActivity);
        //资产订阅
        LiveDataBus.getInstance().with(BHConstants.Label_Account, LoadDataModel.class).observe(this, ldm->{
            if(ldm.loadingStatus== LoadingStatus.SUCCESS){
                updateAssets((AccountInfo) ldm.getData());
            }
            refreshFinish();
        });

        mTransactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);
        mTransactionViewModel.mutableLiveData.observe(this,ldm -> {
            updateTransferStatus(ldm);
        });


        mTokenViewModel = ViewModelProviders.of(this).get(TokenViewModel.class);
        mTokenViewModel.queryLiveData.observe(this,ldm->{
            if(ldm.loadingStatus==LoadingStatus.SUCCESS){
                transferOutCrossVH.tranferToken = (BHToken) ldm.getData();
                //更新跨链提币手续费
                transferOutCrossVH.updateWithDrawFee();
            }
            refreshFinish();
        });
    }


    public void refreshFinish(){
        def_dailog_count++;
        if(def_dailog_count==2){
            mRefreshLayout.finishRefresh();
        }
    }

    //更新资产
    private void updateAssets(AccountInfo accountInfo) {
        transferOutCrossVH.updateAssets();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //处理二维码扫描结果
        if (requestCode == BHQrScanActivity.REQUEST_CODE ) {
            if(resultCode == RESULT_OK){
                //处理扫描结果（在界面上显示）
                String qrCode  = data.getExtras().getString(XQRCode.RESULT_DATA);
                transferOutCrossVH.inp_drawwith_address.setText(qrCode);
            }else if(resultCode == BHQrScanActivity.REQUEST_IMAGE){
                getAnalyzeQRCodeResult(data.getData());
            }
        }
    }

    private void getAnalyzeQRCodeResult(Uri uri) {
        XQRCode.analyzeQRCode(PathUtils.getFilePathByUri(this, uri), new QRCodeAnalyzeUtils.AnalyzeCallback() {
            @Override
            public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                transferOutCrossVH.inp_drawwith_address.setText(result);
            }

            @Override
            public void onAnalyzeFailed() {
                ToastUtils.showToast(getResources().getString(R.string.encode_qr_fail));
            }
        });
    }

    //选择币种
    public void selectTokenAction(View view){
        ChooseTokenFragment fragment = ChooseTokenFragment.showFragment(m_symbol, BH_BUSI_TYPE.跨链转账.value,this::selectTokenListener);
        fragment.show(getSupportFragmentManager(),ChooseTokenFragment.class.getName());
    }

    //选择币种回调
    private void selectTokenListener(String symbol, int position) {
        //更新token
        m_symbol = symbol;
        transferOutCrossVH.updateTokenInfo(symbol);
    }

    //提币
    private void withDrawAtion(View view) {
        //隐藏键盘
        ToolUtils.hintKeyBoard(this);

        boolean flag = transferOutCrossVH.verifyWithDrawAction();
        if(!flag){
            return;
        }

        Password30PFragment.showPasswordDialog(getSupportFragmentManager(),
                Password30PFragment.class.getName(),
                this::callbackWithdraw,0,true);
    }


    public void callbackWithdraw(String password, int position,int verifyPwdWay) {
        String to_address = transferOutCrossVH.inp_drawwith_address.getText().toString().trim();
        //提币数量
        String withDrawAmount = transferOutCrossVH.inp_withdraw_amount.getText().toString().trim();
        //交易手续费
        String feeAmount = transferOutCrossVH.tv_fee.getText().toString();
        //提币手续费
        String withDrawFeeAmount = transferOutCrossVH.tv_withdraw_fee.getText().toString();
        //创建提币信息
        List<TxReq.TxMsg> tx_msg_list = BHRawTransaction.createwithDrawWMsg(to_address,withDrawAmount,withDrawFeeAmount,
                transferOutCrossVH.tranferToken.symbol);
        mTransactionViewModel.transferInnerExt(this,password,feeAmount,tx_msg_list);
    }

    //更新状态
    private void updateTransferStatus(LoadDataModel ldm) {
        if(ldm.loadingStatus== LoadingStatus.SUCCESS){
            ToastUtils.showToast(getResources().getString(R.string.transfer_in_success));
            EventBus.getDefault().post(new TransctionEvent());
            finish();
        }else{
            ToastUtils.showToast(getResources().getString(R.string.transfer_in_fail));
        }
    }

}