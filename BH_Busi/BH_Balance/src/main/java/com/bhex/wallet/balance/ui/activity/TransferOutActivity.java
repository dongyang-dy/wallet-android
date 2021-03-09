package com.bhex.wallet.balance.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;
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
import com.bhex.tools.utils.PathUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.R2;
import com.bhex.wallet.balance.event.TransctionEvent;
import com.bhex.wallet.balance.ui.fragment.ChooseTokenFragment;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.ui.viewhodler.TransferOutVH;
import com.bhex.wallet.balance.viewmodel.TokenViewModel;
import com.bhex.wallet.balance.viewmodel.TransactionViewModel;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.manager.MainActivityManager;
import com.bhex.wallet.common.model.AccountInfo;
import com.bhex.wallet.common.tx.BHRawTransaction;
import com.bhex.wallet.common.tx.TxReq;
import com.bhex.wallet.common.ui.activity.BHQrScanActivity;
import com.bhex.wallet.common.ui.fragment.Password30PFragment;
import com.bhex.wallet.common.utils.LiveDataBus;
import com.bhex.wallet.common.viewmodel.BalanceViewModel;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import org.greenrobot.eventbus.EventBus;

import java.math.BigInteger;
import java.util.List;

import butterknife.BindView;

/**
 * @author gongdongyang
 * 转账、提币代码重构
 */
@Route(path = ARouterConfig.Balance.Balance_transfer_out)
public class TransferOutActivity extends BaseActivity {

    @Autowired(name="symbol")
    String m_symbol;

    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;

    @BindView(R2.id.refreshLayout)
    SmartRefreshLayout refreshLayout;

    int def_dailog_count = 0;

    TransferOutVH transferOutVH;

    BalanceViewModel mBalanceViewModel;

    TransactionViewModel mTransactionViewModel;

    TokenViewModel mTokenViewModel;

    private SmartRefreshLayout mRefreshLayout;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_transfer_out;
    }

    @Override
    protected void initView() {
        tv_center_title.setText(getString(R.string.transfer));
        ARouter.getInstance().inject(this);
        mRefreshLayout = findViewById(R.id.refreshLayout);
        transferOutVH = new TransferOutVH(this,findViewById(R.id.root_view));
        /*//
        findViewById(R.id.btn_drawwith_coin).setOnClickListener(this::onSubmitAction);
        findViewById(R.id.btn_transfer_out_token).setOnClickListener(this::chooseTokenAction);*/

        transferOutVH.layout_select_token.setOnClickListener(this::selectTokenAction);
        transferOutVH.btn_transfer.setOnClickListener(this::transferAction);
    }

    @Override
    protected void addEvent() {
        transferOutVH.updateTokenInfo(m_symbol);
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

        mRefreshLayout.setOnRefreshListener(refreshLayout -> {
            def_dailog_count = 0;
            mBalanceViewModel.getAccountInfo(this, CacheStrategy.onlyRemote());
            //mTokenViewModel.queryToken(this,transferOutVH.tranferToken.symbol);
        });
        mRefreshLayout.autoRefresh();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //处理二维码扫描结果
        if (requestCode == BHQrScanActivity.REQUEST_CODE ) {
            if(resultCode == RESULT_OK){
                //处理扫描结果（在界面上显示）
                String qrCode  = data.getExtras().getString(XQRCode.RESULT_DATA);
                transferOutVH.inp_transfer_in_address.setText(qrCode);
            }else if(resultCode == BHQrScanActivity.REQUEST_IMAGE){
                getAnalyzeQRCodeResult(data.getData());
            }
        }
    }

    private void getAnalyzeQRCodeResult(Uri uri) {
        XQRCode.analyzeQRCode(PathUtils.getFilePathByUri(this, uri), new QRCodeAnalyzeUtils.AnalyzeCallback() {
            @Override
            public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                transferOutVH.inp_transfer_in_address.setText(result);
            }

            @Override
            public void onAnalyzeFailed() {
                ToastUtils.showToast(getResources().getString(R.string.encode_qr_fail));
            }
        });
    }

    //选择币种
    public void selectTokenAction(View view){
        ChooseTokenFragment fragment = ChooseTokenFragment.showFragment(m_symbol, BH_BUSI_TYPE.链内转账.value,this::selectTokenListener);
        fragment.show(getSupportFragmentManager(),ChooseTokenFragment.class.getName());
    }

    //选择币种回调
    private void selectTokenListener(String symbol, int position) {
        //更新token
        m_symbol = symbol;
        transferOutVH.updateTokenInfo(symbol);
    }

    //转账
    private void transferAction(View view) {
        //隐藏键盘
        ToolUtils.hintKeyBoard(this);

       boolean flag= transferOutVH.verifyTransferAction();
       if(!flag){
           return;
       }

       Password30PFragment.showPasswordDialog(getSupportFragmentManager(),
                Password30PFragment.class.getName(),
                this::transferConfirmAction,0,true);
    }

    //密码对话框回调
    public void transferConfirmAction(String password, int position,int verifyPwdWay) {

        String to_address = transferOutVH.inp_transfer_in_address.getText().toString().trim();
        BigInteger gasPrice = BigInteger.valueOf ((long)(BHConstants.BHT_GAS_PRICE));
        //转账金额
        String v_transfer_amount = transferOutVH.inp_transfer_amount.getText().toString().trim();
        //交易手续费
        String v_fee_amount = transferOutVH.tv_fee.getText().toString().trim();
        //创建转账信息
        List<TxReq.TxMsg> tx_msg_list = BHRawTransaction.createTransferMsg(to_address,v_transfer_amount,m_symbol);
        mTransactionViewModel.transferInnerExt(this,password,v_fee_amount,tx_msg_list);
    }


    //更新可用资产
    private void updateAssets(AccountInfo accountInfo) {
        transferOutVH.updateAssets();
    }

    //更新转账状态
    private void updateTransferStatus(LoadDataModel ldm) {
        if(ldm.loadingStatus== LoadingStatus.SUCCESS){
            ToastUtils.showToast(getResources().getString(R.string.transfer_in_success));
            EventBus.getDefault().post(new TransctionEvent());
            finish();
        }else{
            //ToastUtils.showToast(getResources().getString(R.string.transfer_in_fail));
        }
    }



    public void refreshFinish(){
        def_dailog_count++;
        if(def_dailog_count==1){
            mRefreshLayout.finishRefresh();
        }
    }
}
