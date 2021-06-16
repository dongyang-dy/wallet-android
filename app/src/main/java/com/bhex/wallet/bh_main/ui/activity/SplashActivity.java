package com.bhex.wallet.bh_main.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.language.LocalManageUtil;
import com.bhex.tools.utils.CheckSysUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.NavigateUtil;
import com.bhex.wallet.R;
import com.bhex.wallet.app.BHApplication;
import com.bhex.wallet.bh_main.ui.fragment.RistFragment;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.config.BHFilePath;
import com.bhex.wallet.common.manager.AppStatusManager;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.MMKVManager;
import com.bhex.wallet.common.viewmodel.WalletViewModel;
import com.bhex.wallet.mnemonic.MnemonicIndexActivity;
import com.gyf.immersionbar.ImmersionBar;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import org.web3j.protocol.core.methods.response.Log;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 *  启动页
 * @author
 */
@Route(path= ARouterConfig.Main.main_splash)
public class SplashActivity extends AppCompatActivity {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private WalletViewModel walletViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        AppStatusManager.getInstance().setAppStatus(AppStatusManager.STATUS_NORMAL); //进入应用初始化正常状态-设置成正常(未回收)状态
        setContentView(R.layout.activity_splash);
        walletViewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
        walletViewModel.mutableWallentLiveData.observe(this,ldm->{
            goto_Index();
        });

        if(!new CheckSysUtils(this).isDeviceRooted()){
            walletViewModel.loadWallet(this);
        }else{
            RistFragment.showRistFragment(itemClickListener).show(getSupportFragmentManager(),RistFragment.class.getName());
        }

        ImmersionBar.with(this)
                .statusBarColor(android.R.color.white)
                .statusBarDarkFont(true)
                .navigationBarDarkIcon(true)
                .navigationBarColor(R.color.white)
                .fullScreen(true)
                .init();
    }

    //去首页
    public void goto_Index(){
        Disposable disposable = Observable.just(0).timer(1000, TimeUnit.MILLISECONDS)
                //.as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(aLong -> {
                    LogUtils.d("SplashActivity===>:","==aLong=="+aLong);
                    //首次启动
                    if (!BHUserManager.getInstance().isHasWallet()) {
                        ARouter.getInstance().build(ARouterConfig.MNEMONIC_INDEX_PAGE).navigation();
                    } else {
                        boolean isFinger = MMKVManager.getInstance().mmkv().decodeBool(BHConstants.FINGER_PWD_KEY);
                        if (!isFinger) {
                            ARouter.getInstance().build(ARouterConfig.Main.main_mainindex).navigation();
                        } else {
                            ARouter.getInstance().build(ARouterConfig.Account.Account_Login_Finger).navigation();
                        }
                    }
                    finish();
                });
        mCompositeDisposable.add(disposable);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocalManageUtil.attachBaseContext(newBase, ""));
    }

    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            int uiMode = overrideConfiguration.uiMode;
            overrideConfiguration.setTo(getBaseContext().getResources().getConfiguration());
            overrideConfiguration.uiMode = uiMode;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dispose();
    }

    private void dispose() {
        if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.dispose();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dispose();
    }

    @Override
    protected void onStop() {
        super.onStop();
        BHUserManager.getInstance();
        BHFilePath.initPath(BHApplication.getInstance());
    }

    RistFragment.ItemClickListener itemClickListener = (position -> {
        if(position==0){
            finish();
        }else{
            walletViewModel.loadWallet(this);
        }
    });
}