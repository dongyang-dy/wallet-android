package com.bhex.wallet.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.bhex.network.app.BaseApplication;
import com.bhex.tools.language.LocalManageUtil;
import com.bhex.wallet.common.manager.MMKVManager;
import com.bhex.wallet.common.manager.MainActivityManager;
import com.bhex.wallet.common.work.RateSyncWork;

import java.util.concurrent.TimeUnit;

import xcrash.XCrash;

/**
 * created by gongdongyang
 * on 2020/2/24
 */
public class BHApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        SystemConfig.getInstance().init();
        //TypefaceUtils.replaceSystemDefaultFont(BHApplication.getInstance());
        //夜间模式
        AppCompatDelegate.setDefaultNightMode(MMKVManager.getInstance().getSelectNightMode());
        registerActivityLifecycleCallbacks(new ActivityLifecycleListener());
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocalManageUtil.attachBaseContext(base,""));
        XCrash.init(this,new XCrash.InitParameters().setLogDir(getExternalFilesDir("tombstone").getAbsolutePath()));
    }

    private void rateSync(){
        PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest.Builder(RateSyncWork.class,15L,TimeUnit.SECONDS);
        WorkManager.getInstance().enqueue(builder.build());
    }

    class ActivityLifecycleListener implements ActivityLifecycleCallbacks{
        private int refCount = 0;
        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            if(refCount==0){
                MainActivityManager._instance.startAssetRequest();
                //SequenceManager.getInstance().timerTranscation(MainActivityManager.getInstance().mainActivity);
            }
            refCount++;
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {

        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {

        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            refCount--;
            if(refCount == 0){
                MainActivityManager._instance.stopAssetRequest();
                //SequenceManager.getInstance().stopTranscation(MainActivityManager.getInstance().mainActivity);
            }
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {

        }
    }
}
