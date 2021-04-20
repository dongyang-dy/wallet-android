package com.bhex.wallet.mnemonic.ui.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.editor.SimpleTextWatcher;
import com.bhex.lib_qr.XQRCode;
import com.bhex.lib_qr.util.QRCodeAnalyzeUtils;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.NavigateUtil;
import com.bhex.tools.utils.PathUtils;
import com.bhex.tools.utils.RegexUtil;
import com.bhex.wallet.common.base.BaseCacheActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.enums.MAKE_WALLET_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.ui.activity.BHQrScanActivity;
import com.bhex.wallet.mnemonic.R;
import com.bhex.wallet.mnemonic.R2;

import butterknife.BindView;
import butterknife.OnClick;

/**
 *
 * @author gongdongyang
 * 私钥导入
 * 2020-3-18 18:26:18
 */
@Route(path = ARouterConfig.TRUSTEESHIP_IMPORT_PRIVATEKEY)
public class ImportPrivateKeyActivity extends BaseCacheActivity {


    @BindView(R2.id.et_private_key)
    AppCompatEditText et_private_key;

    @BindView(R2.id.btn_scan_qr)
    AppCompatImageView btn_scan_qr;

    @BindView(R2.id.btn_next)
    AppCompatTextView btn_next;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_import_privatekey;
    }

    @Override
    protected void initView() {
        et_private_key.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        et_private_key.setGravity(Gravity.TOP);
        et_private_key.setSingleLine(false);
        et_private_key.setHorizontallyScrolling(false);
    }

    @Override
    protected void addEvent() {
        et_private_key.addTextChangedListener(new SimpleTextWatcher(){
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                String origin = et_private_key.getText().toString().trim();
                boolean flag = RegexUtil.checkIsHex(origin);
                LogUtils.d("");
                if(!TextUtils.isEmpty(origin)){
                    btn_next.setBackgroundResource(R.drawable.btn_bg_blue_6_corner);
                    btn_next.setEnabled(true);
                }else{
                    btn_next.setBackgroundResource(R.drawable.btn_disabled_gray);
                    btn_next.setEnabled(false);
                }
            }
        });

        btn_scan_qr.setOnClickListener(this::scanPKAction);
    }

    //扫描私钥
    private void scanPKAction(View view) {
        ARouter.getInstance().build(ARouterConfig.Common.commom_scan_qr).navigation(this, BHQrScanActivity.REQUEST_CODE);
    }

    @OnClick({R2.id.btn_next})
    public void onViewClicked(View view) {
        if(view.getId()==R.id.btn_next){
            importPrivateKey();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.d("key=======>:","requestCode=="+requestCode);
        if (requestCode == BHQrScanActivity.REQUEST_CODE) {
            if(resultCode==RESULT_OK){
                String qrCode  = data.getExtras().getString(XQRCode.RESULT_DATA);
                et_private_key.setText(qrCode);
                et_private_key.setSelection(qrCode.length());
            }else if(resultCode == BHQrScanActivity.REQUEST_IMAGE){
                getAnalyzeQRCodeResult(data.getData());
            }
        }
    }

    private void getAnalyzeQRCodeResult(Uri uri) {
        XQRCode.analyzeQRCode(PathUtils.getFilePathByUri(this, uri), new QRCodeAnalyzeUtils.AnalyzeCallback() {
            @Override
            public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                et_private_key.setText(result);
            }
            @Override
            public void onAnalyzeFailed() {
                ToastUtils.showToast(getResources().getString(R.string.encode_qr_fail));
            }
        });
    }

    /**
     * 导入privateKey
     */
    private void importPrivateKey() {
        String privateKey = et_private_key.getText().toString();
        if(!RegexUtil.checkIsHex(privateKey) || privateKey.length()!=64){
            ToastUtils.showToast(getResources().getString(R.string.error_private_key_rule));
            return;
        }

        //BHUserManager.getInstance().getTmpBhWallet().setWay(MAKE_WALLET_TYPE.PK.getWay());
        BHUserManager.getInstance().getCreateWalletParams().privateKey = privateKey.trim();
        //NavigateUtil.startActivity(this,TrusteeshipActivity.class);
        ARouter.getInstance()
                .build(ARouterConfig.TRUSTEESHIP_MNEMONIC_FRIST)
                .withInt("way",MAKE_WALLET_TYPE.PK.getWay())
                .navigation();
    }

}
