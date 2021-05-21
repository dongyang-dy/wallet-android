package com.bhex.wallet.common.cache;

import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.SparseArray;

import com.bhex.network.RetryWithDelay;
import com.bhex.network.RxSchedulersHelper;
import com.bhex.network.app.BaseApplication;
import com.bhex.network.cache.RxCache;
import com.bhex.network.cache.data.CacheResult;
import com.bhex.network.cache.stategy.CacheStrategy;
import com.bhex.network.cache.stategy.IStrategy;
import com.bhex.network.observer.BHBaseObserver;
import com.bhex.network.observer.BaseObserver;
import com.bhex.network.utils.JsonUtils;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.common.api.BHttpApi;
import com.bhex.wallet.common.api.BHttpApiInterface;
import com.bhex.wallet.common.db.AppDataBase;
import com.bhex.wallet.common.db.dao.BHTokenDao;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.manager.MMKVManager;
import com.bhex.wallet.common.model.BHToken;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import java8.util.stream.IntStreams;
import java8.util.stream.StreamSupport;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/4/7
 * Time: 17:57
 */
public class SymbolCache extends BaseCache {

    private static final String TAG = SymbolCache.class.getSimpleName();

    private static volatile SymbolCache _instance = new SymbolCache();

    public static final String CACHE_KEY = "SymbolCache";
    public static final String CACHE_KEY_VERIFIED = "SymbolCache_verified";

    private LinkedHashMap<String, BHToken> symbolMap = new LinkedHashMap();

    private LinkedHashMap<String,BHToken> defaultTokenList = new LinkedHashMap<>();
    private LinkedHashMap<String,BHToken> localTokenList = new LinkedHashMap<>();
    //private ArrayMap<String,BHToken> verifiedTokenList = new ArrayMap<>();

    private BHTokenDao mBhTokenDao = AppDataBase.getInstance(BaseApplication.getInstance()).bhTokenDao();

    private SymbolCache(){
    }

    public static SymbolCache getInstance(){
        return _instance;
    }

    @Override
    public synchronized void beginLoadCache() {
        //加载缓存的数据
        loadTokenFromDb();
        //请求默认币对
        loadDefaultToken();
        //官方认证
        //loadVerifiedToken();
    }


    public void loadTokenFromDb(){
        BaseApplication.getInstance().getExecutor().execute(()->{
            List<BHToken> list = mBhTokenDao.loadAllToken();
            if(ToolUtils.checkListIsEmpty(list)){
               return;
            }
            for(BHToken item:list){
                symbolMap.put(item.symbol,item);
            }
            //LogUtils.d("SymbolCache====>:","size=="+symbolMap.size());
        });

    }

    private synchronized void loadDefaultToken(){
        Type type = (new TypeToken<JsonArray>() {}).getType();
        BHttpApi.getService(BHttpApiInterface.class).loadDefaultToken()
                .compose(RxSchedulersHelper.io_main())
                .compose(RxCache.getDefault().transformObservable(SymbolCache.CACHE_KEY, type,
                        getCacheStrategy(CacheStrategy.cacheAndRemote())))
                .map(new CacheResult.MapFunc<>())
                //.retryWhen(new RetryWithDelay())
                .observeOn(Schedulers.computation())
                .subscribe(new BHBaseObserver<JsonArray>(false) {
                    @Override
                    protected void onSuccess(JsonArray jsonArray) {

                        if(jsonArray==null){
                            return;
                        }

                        if(jsonArray.isJsonNull()){
                            return;
                        }

                        List<BHToken> coinList = JsonUtils.getListFromJson(jsonArray.toString(), BHToken.class);
                        if(ToolUtils.checkListIsEmpty(coinList)){
                            return;
                        }
                        //缓存所有的token
                        LogUtils.d("BalanceAdapter==>","coinList=="+coinList.size());
                        putSymbolToMap(coinList,BH_BUSI_TYPE.默认币种.getIntValue());
                    }

                    @Override
                    protected void onFailure(int code, String errorMsg) {
                        super.onFailure(code, errorMsg);
                        LogUtils.d("BalanceAdapter==>","onFailure==");
                    }
                });
    }

    //官方认证币对
    private synchronized void loadVerifiedToken(){
        Type type = (new TypeToken<JsonArray>() {}).getType();
        BHttpApi.getService(BHttpApiInterface.class).loadVerifiedToken(null)
                .compose(RxSchedulersHelper.io_main())
                .compose(RxCache.getDefault().transformObservable(SymbolCache.CACHE_KEY_VERIFIED, type,
                        getCacheStrategy(CacheStrategy.onlyRemote())))
                .map(new CacheResult.MapFunc<>())
                .retryWhen(new RetryWithDelay())
                .observeOn(Schedulers.computation())
                .subscribe(new BHBaseObserver<JsonArray>(false) {
                    @Override
                    protected void onSuccess(JsonArray jsonArray) {
                        if(jsonArray==null){
                            return;
                        }
                        List<BHToken> coinList = JsonUtils.getListFromJson(jsonArray.toString(), BHToken.class);
                        if(ToolUtils.checkListIsEmpty(coinList)){
                            return;
                        }

                        //缓存所有的token
                        putSymbolToMap(coinList,BH_BUSI_TYPE.官方认证.getIntValue());
                    }

                    @Override
                    protected void onFailure(int code, String errorMsg) {
                        super.onFailure(code, errorMsg);
                    }
                });
    }

    //1 默认 2 官方认证
    public synchronized void putSymbolToMap(List<BHToken> coinList,int way){
        for(BHToken item:coinList){
            symbolMap.put(item.symbol,item);
            if(way==BH_BUSI_TYPE.默认币种.getIntValue()){
                defaultTokenList.put(item.symbol,item);
            }
            /*if(way==BH_BUSI_TYPE.官方认证.getIntValue()){
                verifiedTokenList.put(item.symbol,item);
            }*/
        }
        BaseApplication.getInstance().getExecutor().execute(()->{
            mBhTokenDao.insert(coinList);
        });
    }

    public synchronized BHToken getBHToken(String symbol){
        return symbolMap.get(symbol);
    }

    public synchronized BHToken putBHToken(BHToken token){
        return symbolMap.put(token.symbol,token);
    }

    public synchronized void addBHToken(BHToken bhToken){
        symbolMap.put(bhToken.symbol,bhToken);
    }

    public synchronized LinkedHashMap<String,BHToken> getDefaultTokenList(){
        return defaultTokenList;
    }

    public synchronized LinkedHashMap<String,BHToken> getLocalToken(){
        localTokenList.putAll(defaultTokenList);

        String default_symbol = MMKVManager.getInstance().mmkv().decodeString(BHConstants.SYMBOL_DEFAULT_KEY);
        if(TextUtils.isEmpty(default_symbol)){
            return localTokenList;
        }

        //保存本地
        String []a_default_symbol = default_symbol.split("_");
        if(a_default_symbol.length==0){
            return localTokenList;
        }

        for(int i= 0;i<a_default_symbol.length;i++){
            BHToken bhToken = symbolMap.get(a_default_symbol[i]);
            if(bhToken==null){
                continue;
            }
            localTokenList.put(bhToken.symbol,bhToken);
        }

        //
        String remove_symbol = MMKVManager.getInstance().mmkv().decodeString(BHConstants.SYMBOL_REMOVE_KEY);
        if(TextUtils.isEmpty(remove_symbol)){
            return localTokenList;
        }

        String []a_remove_symbol = remove_symbol.split("_");

        if(a_remove_symbol==null || a_remove_symbol.length==0){
            return localTokenList;
        }

        IntStreams.range(0,a_remove_symbol.length).forEach(value -> {
            BHToken bhToken = localTokenList.get(a_remove_symbol[value]);
            if(bhToken!=null){
                localTokenList.remove(bhToken.symbol);
            }
        });


        return localTokenList;
    }



    public synchronized int getDecimals(String symbol){
        if(symbolMap.get(symbol)!=null){
            return symbolMap.get(symbol).decimals;
        }else{
            return BHConstants.BHT_DEFAULT_DECIMAL;
        }
    }

}
