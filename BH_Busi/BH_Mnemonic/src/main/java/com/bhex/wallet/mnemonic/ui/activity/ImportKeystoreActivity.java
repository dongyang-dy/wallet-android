package com.bhex.wallet.mnemonic.ui.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.InputView;
import com.bhex.lib.uikit.widget.keyborad.PasswordKeyBoardView;
import com.bhex.lib_qr.XQRCode;
import com.bhex.lib_qr.util.QRCodeAnalyzeUtils;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.utils.NavigateUtil;
import com.bhex.tools.utils.PathUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.common.base.BaseCacheActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.enums.MAKE_WALLET_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.ui.activity.BHQrScanActivity;
import com.bhex.wallet.common.viewmodel.WalletViewModel;
import com.bhex.wallet.mnemonic.R;
import com.bhex.wallet.mnemonic.R2;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author gongdongyang
 * Keystore导入
 * 2020-5-14 12:16:43
 */
@Route(path = ARouterConfig.TRUSTEESHIP_IMPORT_KEYSTORE)
public class ImportKeystoreActivity extends BaseCacheActivity {

    @BindView(R2.id.et_keystore)
    AppCompatEditText et_keystore;

    @BindView(R2.id.inp_origin_pwd)
    InputView inp_origin_pwd;

    @BindView(R2.id.btn_scan_qr)
    AppCompatImageView btn_scan_qr;

    @BindView(R2.id.btn_next)
    AppCompatTextView btn_next;

    WalletViewModel walletViewModel;
    //PasswordKeyBoardView mPasswordKeyboardView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_import_key_store;
    }

    @Override
    protected void initView() {
        et_keystore.setInputType(InputType.TYPE_CLASS_TEXT);
        et_keystore.setGravity(Gravity.TOP);
        et_keystore.setSingleLine(false);
        et_keystore.setHorizontallyScrolling(false);

        inp_origin_pwd.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        /*mPasswordKeyboardView = findViewById(R.id.my_keyboard);
        mPasswordKeyboardView.setAttachToEditText(this,inp_origin_pwd.getEditText(),findViewById(R.id.root_view),findViewById(R.id.keyboard_root));
        findViewById(R.id.keyboard_root).setVisibility(View.GONE);*/

    }

    @Override
    protected void addEvent() {
        walletViewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
        walletViewModel.pwdVerifyLiveData.observe(this,ldm->{
            verifyKeyStoreStatus(ldm);
        });

        btn_scan_qr.setOnClickListener(this::scanKSAction);

        /*et_keystore.setOnFocusChangeListener((v,hasFcus)->{
            if(hasFcus){
                findViewById(R.id.keyboard_root).setVisibility(View.GONE);
            }
        });*/

        /*mPasswordKeyboardView.setOnKeyListener(new PasswordKeyBoardView.OnKeyListener() {
            @Override
            public void onInput(String text) {
                //inp_old_pwd.getEditText().setText(text);
            }

            @Override
            public void onDelete() {
                //mPasswordInputView.onKeyDelete();
            }
        });*/
    }

    //扫描Keystore
    private void scanKSAction(View view) {
        ARouter.getInstance().build(ARouterConfig.Common.commom_scan_qr).navigation(this, BHQrScanActivity.REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BHQrScanActivity.REQUEST_CODE) {
            if(resultCode==RESULT_OK){
                String qrCode  = data.getExtras().getString(XQRCode.RESULT_DATA);
                et_keystore.setText(qrCode);
                et_keystore.setSelection(qrCode.length());
            }else if(resultCode == BHQrScanActivity.REQUEST_IMAGE){
                getAnalyzeQRCodeResult(data.getData());
            }
        }
    }

    private void getAnalyzeQRCodeResult(Uri uri) {
        XQRCode.analyzeQRCode(PathUtils.getFilePathByUri(this, uri), new QRCodeAnalyzeUtils.AnalyzeCallback() {
            @Override
            public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                et_keystore.setText(result);
            }
            @Override
            public void onAnalyzeFailed() {
                ToastUtils.showToast(getResources().getString(R.string.encode_qr_fail));
            }
        });
    }

    @OnClick({R2.id.btn_next})
    public void onViewClicked(View view) {
        if(view.getId()==R.id.btn_next){
            verifyKeystore();
        }

    }

    /**
     * 导入KeyStore
     */
    private void verifyKeystore() {
        String keyStoreStr = et_keystore.getText().toString().trim();
        String password = inp_origin_pwd.getInputString();

        ToolUtils.hintKeyBoard(this);
        if(TextUtils.isEmpty(keyStoreStr)){
            ToastUtils.showToast(getResources().getString(R.string.please_input_keystore));
            return ;
        }

        if(TextUtils.isEmpty(password)){
            ToastUtils.showToast(getResources().getString(R.string.please_input_password));
            return ;
        }

        //验证Keystore和密码是否匹配
        walletViewModel.verifyKeystore(this,keyStoreStr,password,true,true);
    }

    /**
     * 导入KeyStore后的回调
     */
    private void verifyKeyStoreStatus(LoadDataModel<String> ldm){
        if(ldm.getLoadingStatus()== LoadingStatus.SUCCESS){
            //跳转下一页
            String keyStoreStr = et_keystore.getText().toString().trim();
            String password = inp_origin_pwd.getInputString();

            ARouter.getInstance()
                    .build(ARouterConfig.TRUSTEESHIP_IMPORT_PRIVATEKEY_NEXT)
                    .withInt("way", MAKE_WALLET_TYPE.导入KS.getWay())
                    .withString("keyStore",keyStoreStr)
                    .withString("password",password)
                    .navigation();
        }else if(ldm.getLoadingStatus()== LoadingStatus.ERROR){
            ToastUtils.showToast(ldm.msg);
        }
    }

}
