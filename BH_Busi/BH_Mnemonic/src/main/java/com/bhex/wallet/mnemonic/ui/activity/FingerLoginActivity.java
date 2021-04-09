package com.bhex.wallet.mnemonic.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.core.os.CancellationSignal;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.network.base.LoadingStatus;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.helper.BHWalletHelper;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.BHWalletItem;
import com.bhex.wallet.common.viewmodel.WalletViewModel;
import com.bhex.wallet.mnemonic.R;
import com.bhex.wallet.mnemonic.R2;
import com.bhex.wallet.mnemonic.ui.fragment.AddressFragment;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author gongdongyang
 * 2020-5-20 00:29:39
 * 指纹或面容登录
 */
@Route(path = ARouterConfig.Account.Account_Login_Finger,name = "指纹登录")
public class FingerLoginActivity extends BaseActivity  implements AddressFragment.AddressChangeListener{
    protected final static String TAG = FingerLoginActivity.class.getSimpleName();

    @BindView(R2.id.tv_bh_address)
    AppCompatTextView tv_bh_address;

    @BindView(R2.id.iv_username)
    AppCompatTextView iv_username;

    @BindView(R2.id.tv_password_verify)
    AppCompatTextView tv_password_verify;

    FingerprintManagerCompat manager;
    KeyguardManager mKeyManager;

    //当前钱包
    private BHWallet mCurrentWallet;

    private WalletViewModel walletVM;

    CancellationSignal mCancellationSignal;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_finger_login;
    }

    @Override
    protected void initView() {
        manager = FingerprintManagerCompat.from(this);
        mKeyManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
    }
    @Override
    protected void onResume() {
        super.onResume();

        mCurrentWallet = BHUserManager.getInstance().getCurrentBhWallet();
        tv_bh_address.setText(mCurrentWallet.getAddress());
        iv_username.setText(mCurrentWallet.getName());
        BHWalletHelper.proccessAddress(tv_bh_address, mCurrentWallet.getAddress());
        startAuthFingerPrint();

    }

    @Override
    protected void addEvent() {
        //startAuthFingerPrint();
        mCancellationSignal = new CancellationSignal();

        walletVM = ViewModelProviders.of(this).get(WalletViewModel.class);
        walletVM.mutableLiveData.observe(this, loadDataModel -> {
            if (loadDataModel.loadingStatus == LoadingStatus.SUCCESS) {
                //ToastUtils.showToast("==loadingStatus==" + loadDataModel.loadingStatus);
            }
        });
    }



    private void startAuthFingerPrint() {
        if (isFinger()) {
            LogUtils.d(TAG, getString(R.string.fingerprint_identify_hint));
            startListening(null);
        }
    }

    private void stopAuthFingerPrint(){
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    public void startListening(FingerprintManagerCompat.CryptoObject cryptoObject) {
        //android studio 上，没有这个会报错
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.string_no_fingerprint_permission), Toast.LENGTH_SHORT).show();
            return;
        }
        manager.authenticate(cryptoObject, 0,mCancellationSignal, mSelfCancelled, null);
    }


    FingerprintManagerCompat.AuthenticationCallback mSelfCancelled = new FingerprintManagerCompat.AuthenticationCallback() {
        @Override
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
            super.onAuthenticationError(errMsgId, errString);
            LogUtils.d("FingerLoginActivity==>:","==onAuthenticationError==");
            if (errMsgId==7) {
                showError(getString(R.string.string_fingerprint_errors_retry));
            }
        }

        @Override
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
            LogUtils.d("FingerLoginActivity==>:","==onAuthenticationHelp==");

            super.onAuthenticationHelp(helpMsgId, helpString);
            showError(helpString);

        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
            LogUtils.d("FingerLoginActivity==>:","==onAuthenticationSucceeded==");

            super.onAuthenticationSucceeded(result);
            showAuthenticationSucceeded(result);
        }

        @Override
        public void onAuthenticationFailed() {
            LogUtils.d("FingerLoginActivity==>:","==onAuthenticationFailed==");

            super.onAuthenticationFailed();

            showError(getString(R.string.fingerprint_failed));
        }
    };

    public void showAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        //ToastUtils.showToast(getResources().getString(R.string.verify_pass));
        //NavigateUtil.startMainActivity(this);
        ARouter.getInstance().build(ARouterConfig.Main.main_mainindex).navigation();
        finish();
    }

    @OnClick({R2.id.tv_bh_address,R2.id.tv_password_verify})
    public void onViewClicked(View view) {
         if (view.getId() == R.id.tv_bh_address) {
             AddressFragment fragment = new AddressFragment();
             fragment.setChangeListener(this);
             fragment.show(getSupportFragmentManager(), "");
        }else if(view.getId() == R.id.tv_password_verify){
             ARouter.getInstance().build(ARouterConfig.Account.Account_Login_Password).navigation();
             this.finish();
        }
    }

    @Override
    public void onItemClick(int position) {
        BHWallet bhWallet = BHUserManager.getInstance().getAllWallet().get(position);
        if(bhWallet.id!=mCurrentWallet.id){
            mCurrentWallet = bhWallet;
            BHUserManager.getInstance().setCurrentBhWallet(mCurrentWallet);
            walletVM.updateWallet(this, mCurrentWallet, mCurrentWallet.id, BHWalletItem.SELECTED,false);
            BHWalletHelper.proccessAddress(tv_bh_address, mCurrentWallet.getAddress());
            iv_username.setText(mCurrentWallet.getName());
        }
    }

    @SuppressLint("MissingPermission")
    public boolean isFinger() {

        //android studio 上，没有这个会报错
        if (ActivityCompat.checkSelfPermission(FingerLoginActivity.this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.string_no_fingerprint_permission), Toast.LENGTH_SHORT).show();
            return false;
        }
        LogUtils.d(TAG, "有指纹权限");
        //判断硬件是否支持指纹识别
        if (!manager.isHardwareDetected()) {
            Toast.makeText(this, getString(R.string.fingerprint_no_hardware), Toast.LENGTH_SHORT).show();
            return false;
        }
        LogUtils.d(TAG, "有指纹模块");
        //判断 是否开启锁屏密码
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (!mKeyManager.isKeyguardSecure()) {
                Toast.makeText(this, getString(R.string.fingerprint_no_lock_screen_pwd), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        LogUtils.d(TAG, "已开启锁屏密码");
        //判断是否有指纹录入
        if (!manager.hasEnrolledFingerprints()) {
            Toast.makeText(this, getString(R.string.fingerprint_create_finger_first_hint), Toast.LENGTH_SHORT).show();
            return false;
        }
        LogUtils.d(TAG, "已录入指纹");

        return true;
    }

    @Override
    protected boolean isShowBacking() {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAuthFingerPrint();
    }

    private void showError(CharSequence error) {
        ToastUtils.showToast(error+"");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.exit(0);
    }
}
