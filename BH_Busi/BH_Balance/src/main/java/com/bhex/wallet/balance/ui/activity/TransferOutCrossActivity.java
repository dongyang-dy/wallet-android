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
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.PathUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.R2;
import com.bhex.wallet.balance.event.TransctionEvent;
import com.bhex.wallet.balance.helper.BHTokenHelper;
import com.bhex.wallet.balance.ui.fragment.ChooseChainFragment;
import com.bhex.wallet.balance.ui.fragment.ChooseTokenFragment;
import com.bhex.wallet.balance.ui.viewhodler.TransferOutCrossVH;
import com.bhex.wallet.balance.viewmodel.TokenViewModel;
import com.bhex.wallet.balance.viewmodel.TransactionViewModel;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.manager.MainActivityManager;
import com.bhex.wallet.common.model.AccountInfo;
import com.bhex.wallet.common.model.BHChain;
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
    String m_select_symbol;

    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;

    private SmartRefreshLayout mRefreshLayout;
    private int def_dailog_count = 0;
    private TransferOutCrossVH transferOutCrossVH;

    private BalanceViewModel mBalanceViewModel;
    private TransactionViewModel mTransactionViewModel;
    private TokenViewModel mTokenViewModel;

    private BHToken withDrawToken;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_transfer_out_cross;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        tv_center_title.setText(getString(R.string.cross_withdraw));
        mRefreshLayout = findViewById(R.id.refreshLayout);

        transferOutCrossVH = new TransferOutCrossVH(this,findViewById(R.id.root_view));

        withDrawToken = BHTokenHelper.getCrossDefaultToken(m_select_symbol);
        if(withDrawToken!=null){
            transferOutCrossVH.updateTokenInfo(m_select_symbol,withDrawToken.chain);
        }
    }

    @Override
    protected void addEvent() {
        //选择币种
        transferOutCrossVH.layout_select_token.setOnClickListener(this::selectTokenAction);
        transferOutCrossVH.btn_drawwith_coin.setOnClickListener(this::withDrawAtion);
        //选择链
        transferOutCrossVH.layout_select_chain.setOnClickListener(this::selectChainAction);
    }

    //选择币种
    public void selectTokenAction(View view){
        ChooseTokenFragment fragment = ChooseTokenFragment.showFragment(m_select_symbol, BH_BUSI_TYPE.跨链转账.value,this::selectTokenListener);
        fragment.show(getSupportFragmentManager(),ChooseTokenFragment.class.getName());
    }

    //选择币种回调
    private void selectTokenListener(String symbol, int position) {
        //更新token
        m_select_symbol = symbol;
        withDrawToken = BHTokenHelper.getCrossDefaultToken(m_select_symbol);
        transferOutCrossVH.updateTokenInfo(symbol,withDrawToken.chain);
    }

    //选择币种
    public void selectChainAction(View view){
        //
        List<BHChain> chainList = BHTokenHelper.getBHChainList(m_select_symbol);
        ChooseChainFragment fragment = ChooseChainFragment.getInstance(chainList,withDrawToken.chain,this::chooseChainCallback);
        fragment.show(getSupportFragmentManager(),ChooseChainFragment.class.getName());
    }

    private void chooseChainCallback(String chain) {
        withDrawToken = BHTokenHelper.getCrossBHToken(m_select_symbol,chain);
        transferOutCrossVH.updateTokenInfo(m_select_symbol,withDrawToken.chain);
    }

    //提币
    private void withDrawAtion(View view) {
        //隐藏键盘
        ToolUtils.hintKeyBoard(this);

        /*boolean flag = transferOutCrossVH.verifyWithDrawAction();
        if(!flag){
            return;
        }

        Password30PFragment.showPasswordDialog(getSupportFragmentManager(),
                Password30PFragment.class.getName(),
                this::callbackWithdraw,0,true);*/
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //处理二维码扫描结果
        try{
            if (requestCode == BHQrScanActivity.REQUEST_CODE ) {
                if(resultCode == RESULT_OK){
                    //处理扫描结果（在界面上显示）
                    String qrCode  = data.getExtras().getString(XQRCode.RESULT_DATA);
                    transferOutCrossVH.inp_withdraw_address.setText(qrCode);
                    transferOutCrossVH.inp_withdraw_address.setSelection(qrCode.length());
                    transferOutCrossVH.inp_withdraw_address.requestFocus();
                }else if(resultCode == BHQrScanActivity.REQUEST_IMAGE){
                    getAnalyzeQRCodeResult(data.getData());
                }
            }else if(requestCode == AddressBookListActivity.REQUEST_ADDRESS){
                if(resultCode == RESULT_OK){
                    String address  = data.getExtras().getString(AddressBookListActivity.RESULT_DATA);
                    transferOutCrossVH.inp_withdraw_address.setText(address);
                    transferOutCrossVH.inp_withdraw_address.setSelection(address.length());
                    transferOutCrossVH.inp_withdraw_address.requestFocus();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void getAnalyzeQRCodeResult(Uri uri) {
        XQRCode.analyzeQRCode(PathUtils.getFilePathByUri(this, uri), new QRCodeAnalyzeUtils.AnalyzeCallback() {
            @Override
            public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                transferOutCrossVH.inp_withdraw_amount.setText(result);
                transferOutCrossVH.inp_withdraw_amount.setSelection(result.length());
                transferOutCrossVH.inp_withdraw_amount.requestFocus();
            }

            @Override
            public void onAnalyzeFailed() {
                ToastUtils.showToast(getResources().getString(R.string.encode_qr_fail));
            }
        });
    }

}