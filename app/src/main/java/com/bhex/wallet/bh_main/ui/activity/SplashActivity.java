package com.bhex.wallet.bh_main.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import com.bhex.network.base.LoadingStatus;
import com.bhex.tools.constants.Constants;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.NavitateUtil;
import com.bhex.tools.utils.StatusBarUtil;
import com.bhex.wallet.R;
import com.bhex.wallet.common.db.entity.BHWalletExt;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.MMKVManager;
import com.bhex.wallet.mnemonic.MnemonicIndexActivity;
import com.bhex.wallet.mnemonic.ui.activity.BackupMnemonicActivity;
import com.bhex.wallet.mnemonic.ui.activity.LoginActivity;
import com.bhex.wallet.mnemonic.ui.activity.TrusteeshipSuccessActivity;
import com.bhex.wallet.mnemonic.ui.activity.VerifyMnemonicActivity;
import com.bhex.wallet.mnemonic.viewmodel.WalletViewModel;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 *  启动页
 * @author
 */
public class SplashActivity extends AppCompatActivity {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private WalletViewModel walletViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        walletViewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
        walletViewModel.loadWallet(this);

       /*walletViewModel.mutableWallentLiveData.observe(this, listLoadDataModel -> {
            if(listLoadDataModel.loadingStatus== LoadingStatus.SUCCESS){
                //List<BHWalletExt> list = listLoadDataModel.getData();
                //BHUserManager.getInstance().setAllWallet(list);
            }
        });*/

        boolean flag = MMKVManager.getInstance().mmkv().decodeBool(Constants.FRIST_BOOT);

        //
        Disposable disposable = Observable.just(0).timer(2000, TimeUnit.MILLISECONDS).subscribe(aLong -> {
            //首次启动
            if(!BHUserManager.getInstance().isHasWallet()){
                NavitateUtil.startActivity(SplashActivity.this, MnemonicIndexActivity.class);
            }else {
                NavitateUtil.startActivity(SplashActivity.this, LoginActivity.class);
            }
            finish();
        });

        mCompositeDisposable.add(disposable);

        StatusBarUtil.setStatusColor(this,false,true,R.color.white);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        dispose();
    }

    private void dispose(){
        if(mCompositeDisposable!=null &&!mCompositeDisposable.isDisposed()){
            mCompositeDisposable.dispose();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dispose();
    }

}
