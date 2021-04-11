package com.bhex.wallet.common.cache;

import androidx.lifecycle.Lifecycle;

import com.bhex.network.RxSchedulersHelper;
import com.bhex.network.app.BaseApplication;
import com.bhex.network.cache.RxCache;
import com.bhex.network.cache.data.CacheResult;
import com.bhex.network.cache.stategy.CacheStrategy;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.network.observer.BHBaseObserver;
import com.bhex.network.observer.SimpleObserver;
import com.bhex.network.utils.HUtils;
import com.bhex.network.utils.JsonUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.common.api.BHttpApi;
import com.bhex.wallet.common.api.BHttpApiInterface;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.BHRates;
import com.google.gson.reflect.TypeToken;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import java8.util.stream.StreamSupport;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/4/10
 * Time: 11:05
 */
public class RatesCache extends BaseCache {

    private static final String TAG = RatesCache.class.getSimpleName();

    public  static final String CACHE_KEY = "RatesCache";

    private Map<String, BHRates.RatesBean> ratesMap = new ConcurrentHashMap();

    private static volatile RatesCache _instance;

    private RatesCache(){

    }

    public static RatesCache getInstance(){
        if(_instance==null){
            synchronized (RatesCache.class){
                if(_instance==null){
                    _instance = new RatesCache();
                }
            }
        }
        return _instance;
    }

    @Override
    public synchronized void beginLoadCache() {
        super.beginLoadCache();
        getRateToken();
    }

    public synchronized void getRateToken(){
        Type type = (new TypeToken<List<BHRates>>() {}).getType();
        String balacne_list = BHUserManager.getInstance().getSymbolList();
        balacne_list = balacne_list.replace("_",",").toUpperCase();

        RequestBody txBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("symbols",balacne_list).build();
        BHttpApi.getService(BHttpApiInterface.class).loadRates(txBody)
                .compose(RxSchedulersHelper.io_main())
                .compose(RxCache.getDefault().transformObservable(RatesCache.CACHE_KEY, type, getCacheStrategy(CacheStrategy.cacheAndRemoteSync())))
                .map(new CacheResult.MapFunc<>())
                .subscribe(new BHBaseObserver<List<BHRates>>(false) {
                    @Override
                    protected void onSuccess(List<BHRates> ratelist) {
                        if(ToolUtils.checkListIsEmpty(ratelist)){
                            return;
                        }

                        RatesCache.getInstance().getRatesMap().clear();
                        StreamSupport.stream(ratelist).forEach(rate->{
                            RatesCache.getInstance().getRatesMap().put(rate.getToken().toLowerCase(),rate.getRates());
                        });
                    }

                    @Override
                    protected void onFailure(int code, String errorMsg) {

                        super.onFailure(code, errorMsg);
                    }
                });
    }


    /**
     * 获取汇率
     * @return
     */
    public synchronized BHRates.RatesBean getBHRate(String symbol){
        return ratesMap.get(symbol);
    }

    public synchronized Map<String, BHRates.RatesBean> getRatesMap() {
        return ratesMap;
    }
}
