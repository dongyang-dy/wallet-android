package com.bhex.wallet.base;

import android.app.Application;
import android.content.pm.PackageInfo;

import com.bhex.network.BuildConfig;
import com.bhex.network.base.INetworkRequiredInfo;

/**
 * created by gongdongyang
 * on 2020/2/24
 */
public class BHNetwork implements INetworkRequiredInfo {

    private Application mApplication;

    public BHNetwork(Application application) {
        this.mApplication = application;
    }

    @Override
    public String getAppVersionName() {
        try{
            PackageInfo pInfo = mApplication.getPackageManager().getPackageInfo(mApplication.getPackageName(), 0);
            return  pInfo.versionName;
        }catch (Exception e){
            e.printStackTrace();
        }
        return "1.0.0";
    }

    @Override
    public String getAppVersionCode() {
        try{
            PackageInfo pInfo = mApplication.getPackageManager().getPackageInfo(mApplication.getPackageName(), 0);
            return String.valueOf( pInfo.versionCode);
        }catch (Exception e){
            e.printStackTrace();
        }
        return "100";
    }

    @Override
    public boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    @Override
    public Application getApplicationContext() {
        return mApplication;
    }
}
