package com.bhex.wallet.app;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.network.app.BaseApplication;
import com.bhex.network.base.NetworkApi;
import com.bhex.network.cache.RxCache;
import com.bhex.network.cache.diskconverter.GsonDiskConverter;
import com.bhex.network.receiver.NetWorkStatusChangeReceiver;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.BuildConfig;
import com.bhex.wallet.base.BHNetwork;
import com.bhex.wallet.common.cache.CacheCenter;
import com.facebook.stetho.Stetho;
import com.tencent.mmkv.MMKV;

import java.io.File;

/**
 * created by gongdongyang
 * on 2018/9/25
 */
public class SystemConfig  {

    private static SystemConfig _instance;

    private SystemConfig() {

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

        if(BuildConfig.DEBUG){
            //Stetho.initializeWithDefaults(BaseApplication.getInstance());
        }

        /*Stetho.initialize(
                Stetho.newInitializerBuilder(BaseApplication.getInstance())
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(BaseApplication.getInstance()))
                        .build());*/

        NetworkApi.init(new BHNetwork(BaseApplication.getInstance()));

        MMKV.initialize(BHApplication.getInstance());
        File cacheFile = new File(BaseApplication.getInstance().getCacheDir() + File.separator + "data-cache");

        LogUtils.d("SystemConfig===>:","cacheFile===="+cacheFile.getAbsolutePath());

        RxCache.initializeDefault(
                new RxCache.Builder()
                        .appVersion(1)
                        .diskDir(cacheFile)
                        .diskConverter(new GsonDiskConverter())
                        .diskMax((20 * 1024 * 1024))
                        .memoryMax(0)
                        .build());

        //网络状态
        NetWorkStatusChangeReceiver.init(BaseApplication.getInstance());
        //获取币种信息
        CacheCenter.getInstance().loadCache();

    }

    private void asyncInit() {

    }


    /**
     * Arouter初始化
     */

    private void arouterInit(){
        if(true){
            ARouter.openLog();     // 打印日志
            ARouter.openDebug();   // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        }
        ARouter.init(BHApplication.getInstance());
    }
}
