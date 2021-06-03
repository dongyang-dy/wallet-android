package com.bhex.wallet.common.ui.activity;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bhex.lib_qr.XQRCode;
import com.bhex.lib_qr.ui.CaptureFragment;
import com.bhex.lib_qr.util.QRCodeAnalyzeUtils;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.tools.utils.IntentUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.common.R;
import com.bhex.wallet.common.R2;
import com.bhex.wallet.common.config.ARouterConfig;
import com.gyf.immersionbar.ImmersionBar;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;

import static com.bhex.lib_qr.ui.CaptureActivity.KEY_CAPTURE_THEME;
import static com.bhex.lib_qr.ui.CaptureActivity.REQUEST_CODE_REQUEST_PERMISSIONS;

/**
 * @author gongdongyang
 * 2020-4-17 00:32:19
 * 扫描二维码
 */
@Route(path = ARouterConfig.Common.commom_scan_qr)
public class BHQrScanActivity extends BaseActivity {

    public static final int REQUEST_CODE = 111;

    public static final int REQUEST_IMAGE = 112;


    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;

    @BindView(R2.id.tv_album)
    AppCompatTextView tv_album;

    @BindView(R2.id.iv_back)
    AppCompatImageView iv_back;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_bhqr_scan;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(getIntent().getIntExtra(KEY_CAPTURE_THEME, R.style.XQRCodeTheme));
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView() {
        tv_center_title.setText(getResources().getString(R.string.qr_code));
        ImmersionBar.with(this).statusBarColor(R.color.blue_bg)
                .statusBarDarkFont(false)
                .barColor(R.color.blue_bg)
                .fitsSystemWindows(true).init();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_REQUEST_PERMISSIONS);
                return;
            }
        }
        initCaptureFragment();
    }

    @Override
    protected void addEvent() {
        tv_album.setOnClickListener(v -> {
            requestPermissions();
            //startActivityForResult(IntentUtils.getDocumentPickerIntent(IntentUtils.DocumentType.IMAGE), REQUEST_IMAGE);
        });

        iv_back.setOnClickListener(v -> {
            finish();
        });
    }
    public void requestPermissions() {
        final RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(permission -> {
                    if (permission.granted) {
                        // 用户已经同意该权限
                        startActivityForResult(IntentUtils.getDocumentPickerIntent(IntentUtils.DocumentType.IMAGE), REQUEST_IMAGE);
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                        LogUtils.d(permission.name + " is denied. More info should be provided.");
                    } else {
                        // 用户拒绝了该权限，并且选中『不再询问』
                        LogUtils.d(permission.name + " is denied.");
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_IMAGE){
            if(data!=null){
                setResult(REQUEST_IMAGE,data);
                finish();
            }
        }
    }

    /**
     * 初始化采集
     */
    protected void initCaptureFragment() {
        CaptureFragment captureFragment = new CaptureFragment();
        captureFragment.setAnalyzeCallback(analyzeCallback);
        captureFragment.setCameraInitCallBack(cameraInitCallBack);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_zxing_container, captureFragment).commit();
    }

    /**
     * 照相机初始化监听
     */
    CaptureFragment.CameraInitCallBack cameraInitCallBack = new CaptureFragment.CameraInitCallBack() {
        @Override
        public void callBack(@Nullable Exception e) {
            if (e != null) {
                BHQrScanActivity.showNoPermissionTip(BHQrScanActivity.this);
                onCameraInitFailed();
            } else {
                //照相机初始化成功
                onCameraInitSuccess();
            }
        }
    };

    /**
     * 相机初始化成功
     */
    protected void onCameraInitSuccess() {

    }

    /**
     * 相机初始化失败
     */
    protected void onCameraInitFailed() {

    }

    /**
     * 显示无照相机权限提示
     *
     * @param activity
     * @param listener 确定点击事件
     * @return
     */
    public static AlertDialog showNoPermissionTip(final Activity activity, DialogInterface.OnClickListener listener) {
        return new AlertDialog.Builder(activity)
                .setTitle(R.string.xqrcode_pay_attention)
                .setMessage(R.string.xqrcode_not_get_permission)
                .setPositiveButton(R.string.xqrcode_submit, listener)
                .show();
    }

    /**
     * 显示无照相机权限提示
     *
     * @param activity
     * @return
     */
    public static AlertDialog showNoPermissionTip(final Activity activity) {
        return showNoPermissionTip(activity, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        });
    }

    /**
     * 二维码解析回调函数
     */
    QRCodeAnalyzeUtils.AnalyzeCallback analyzeCallback = new QRCodeAnalyzeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap bitmap, String result) {
            handleAnalyzeSuccess(bitmap, result);
        }

        @Override
        public void onAnalyzeFailed() {
            handleAnalyzeFailed();
        }
    };

    /**
     * 处理扫描成功
     *
     * @param bitmap
     * @param result
     */
    protected void handleAnalyzeSuccess(Bitmap bitmap, String result) {
        //LogUtils.d("TransferOutActivity==>:","result=="+result);
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt(XQRCode.RESULT_TYPE, XQRCode.RESULT_SUCCESS);
        bundle.putString(XQRCode.RESULT_DATA, result);
        resultIntent.putExtras(bundle);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    /**
     * 处理解析失败
     */
    protected void handleAnalyzeFailed() {
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt(XQRCode.RESULT_TYPE, XQRCode.RESULT_FAILED);
        bundle.putString(XQRCode.RESULT_DATA, "");
        resultIntent.putExtras(bundle);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initCaptureFragment();
            } else {
                handleRequestPermissionDeny();
            }
        }
    }

    /**
     * 处理权限申请拒绝
     */
    protected void handleRequestPermissionDeny() {
        BHQrScanActivity.showNoPermissionTip(BHQrScanActivity.this);
    }

    public static void start(Fragment fragment, int requestCode, int theme) {
        Intent intent = new Intent(fragment.getContext(), BHQrScanActivity.class);
        intent.putExtra(KEY_CAPTURE_THEME, theme);
        fragment.startActivityForResult(intent, requestCode);
    }
}
