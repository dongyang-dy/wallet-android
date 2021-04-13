package com.bhex.wallet.app;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.network.app.BaseApplication;
import com.bhex.network.base.NetworkApi;
import com.bhex.network.cache.RxCache;
import com.bhex.network.cache.diskconverter.GsonDiskConverter;
import com.bhex.network.exception.ApiException;
import com.bhex.network.exception.ExceptionEngin;
import com.bhex.network.receiver.NetWorkStatusChangeReceiver;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.BuildConfig;
import com.bhex.wallet.base.BHNetwork;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.model.BHPhoneInfo;
import com.facebook.stetho.Stetho;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.mmkv.MMKV;

import java.io.File;

import io.reactivex.plugins.RxJavaPlugins;

/**
 * created by gongdongyang
 * on 2018/9/25
 */
public class SystemConfig  {

    private static SystemConfig _instance;

    private SystemConfig() {
        MMKV.initialize(BHApplication.getInstance());
    }

    public static SystemConfig getInstance() {
        if (_instance == null) {
            synchronized (SystemConfig.class) {
                if (_instance == null) {
                    _instance = new SystemConfig();
                }
            }
        }
        return _instance;
    }

    /**
     * 初始化 同步 异步
     */
    public void init() {
        syncInit();
        BHApplication.getInstance().getExecutor().execute(()->{
            asyncInit();
        });

    }

    private void syncInit(){
        arouterInit();
        RxJavaPlugins.setErrorHandler(throwable -> {
            ApiException apiException = ExceptionEngin.handleException(throwable);
            ToastUtils.showToast(apiException.getDisplayMessage());
        });
        //bugly
        CrashReport.initCrashReport(BaseApplication.getInstance(), "7f44a89de4", false);
        //异常处理
        if(BuildConfig.DEBUG){
             Stetho.initialize(
                    Stetho.newInitializerBuilder(BaseApplication.getInstance())
                            .enableDumpapp(Stetho.defaultDumperPluginsProvider(BaseApplication.getInstance()))
                            .build());
            //Stetho.initializeWithDefaults(BaseApplication.getInstance());
        }

        NetworkApi.init(new BHNetwork(BaseApplication.getInstance()));

        File cacheFile = new File(BaseApplication.getInstance().getCacheDir() + File.separator + "data-cache");

        RxCache rxCache = new RxCache.Builder()
                .appVersion(1)
                .diskDir(cacheFile)
                .diskConverter(new GsonDiskConverter())
                .diskMax((20 * 1024 * 1024))
                .memoryMax(0)
                .build();
        RxCache.initializeDefault(rxCache);


        //网络状态
        NetWorkStatusChangeReceiver.init(BaseApplication.getInstance());
        //获取币种信息
        CacheCenter.getInstance().loadCache();

    }

    private void asyncInit() {
        //初始化设备信息
        BHPhoneInfo.initPhoneInfo(BaseApplication.getInstance());
    }


    /**
     * Arouter初始化
     */

    private void arouterInit(){
        if(BuildConfig.DEBUG){
            ARouter.openLog();     // 打印日志
            ARouter.openDebug();   // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        }
        ARouter.init(BHApplication.getInstance());
    }


}
