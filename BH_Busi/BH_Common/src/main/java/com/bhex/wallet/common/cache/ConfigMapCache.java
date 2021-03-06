package com.bhex.wallet.common.cache;

import android.text.TextUtils;

import com.bhex.network.RxSchedulersHelper;
import com.bhex.network.app.BaseApplication;
import com.bhex.network.cache.RxCache;
import com.bhex.network.cache.data.CacheResult;
import com.bhex.network.observer.BHBaseObserver;
import com.bhex.network.utils.JsonUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.common.R;
import com.bhex.wallet.common.api.BHttpApi;
import com.bhex.wallet.common.api.BHttpApiInterface;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.MMKVManager;
import com.bhex.wallet.common.model.BHChain;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.model.BHTokenMapping;
import com.bhex.wallet.common.model.GasFee;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java8.util.stream.IntStreams;
import java8.util.stream.RefStreams;
import java8.util.stream.StreamSupport;

/**
 * @author gongdongyang
 * 2020-10-10 14:15:35
 */
public class ConfigMapCache extends BaseCache {

    private static final String TAG = ConfigMapCache.class.getSimpleName();

    public static final String CACHE_TOKENMAP_KEY = "TokenMappingCache";
    public static final String CACHE_CHAIN_KEY = "TokenChainCache";
    public static final String CACHE_GASFEE_KEY = "GasFeeCache";

    private List<BHTokenMapping> mTokenMappings = new ArrayList<>();
    private LinkedHashMap<String,BHChain> mChainMap = new LinkedHashMap<>();
    public Map<String,BHToken> mTokens = new HashMap<String,BHToken>();

    private static volatile ConfigMapCache _instance;

    private ConfigMapCache(){

    }

    public static ConfigMapCache getInstance(){
        if(_instance==null){
            synchronized (ConfigMapCache.class){
                if(_instance==null){
                    _instance = new ConfigMapCache();
                }
            }
        }
        return _instance;
    }

    @Override
    public synchronized void beginLoadCache() {
        super.beginLoadCache();
        loadTokenMapping();
        loadChain();
        loadDefaultGasFee();
    }

    private synchronized void loadTokenMapping() {
        Type type = (new TypeToken<JsonObject>() {}).getType();
        BHttpApi.getService(BHttpApiInterface.class).loadTokenMappings()
                .compose(RxSchedulersHelper.io_main())
                .compose(RxCache.getDefault().transformObservable(CACHE_TOKENMAP_KEY, type,getCacheStrategy()))
                .map(new CacheResult.MapFunc())
                .subscribe(new BHBaseObserver<JsonObject>(false) {
                    @Override
                    protected void onSuccess(JsonObject jsonObject) {
                        if(!JsonUtils.isHasMember(jsonObject,"items")){
                            return;
                        }

                        List<BHTokenMapping> tokens = JsonUtils.getListFromJson(jsonObject.toString(),"items", BHTokenMapping.class);
                        //???????????????token
                        if(ToolUtils.checkListIsEmpty(tokens)){
                            return;
                        }

                        mTokenMappings.clear();

                        for (BHTokenMapping item:tokens) {
                            if(!item.enabled){
                                continue;
                            }
                            if(mTokens.get(item.issue_symbol)==null){
                                mTokens.put(item.issue_symbol,item.issue_token);
                            }

                            if(mTokens.get(item.target_symbol)==null){
                                mTokens.put(item.target_symbol,item.target_token);
                            }

                            item.coin_symbol = item.issue_symbol;
                            mTokenMappings.add(item);
                            BHTokenMapping reverseItem = new BHTokenMapping(
                                    item.target_symbol,
                                    item.issue_symbol,
                                    item.issue_symbol,
                                    item.total_supply,
                                    item.issue_pool,
                                    item.enabled);
                            reverseItem.issue_token = item.issue_token;
                            reverseItem.coin_symbol = item.target_symbol;

                            mTokenMappings.add(reverseItem);
                        }

                        //??????token??????
                    }


                    @Override
                    protected void onFailure(int code, String errorMsg) {
                        super.onFailure(code, errorMsg);
                        //LogUtils.d("TokenMapCache====>:","==errorMsg=="+errorMsg);
                    }
                });
    }

    public synchronized void loadChain() {
        BHBaseObserver observer = new BHBaseObserver<JsonArray>() {
            @Override
            protected void onSuccess(JsonArray jsonArray) {
                if(TextUtils.isEmpty(jsonArray.toString()) || jsonArray.isJsonNull()){
                    return;
                }
                List<BHChain> chains = JsonUtils.getListFromJson(jsonArray.toString(),BHChain.class);
                if(ToolUtils.checkListIsEmpty(chains)){
                    return;
                }

                MMKVManager.getInstance().mmkv().encode(CACHE_TOKENMAP_KEY,jsonArray.toString());
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
            }

        };

        Type type = (new TypeToken<JsonArray>() {}).getType();
        BHttpApi.getService(BHttpApiInterface.class).loadChain()
                .compose(RxSchedulersHelper.io_main())
                .compose(RxCache.getDefault().transformObservable(CACHE_CHAIN_KEY, type,getCacheStrategy()))
                .map(new CacheResult.MapFunc())
                //.retryWhen(new RetryWithDelay())
                .subscribe(observer);
    }

    private synchronized void loadDefaultGasFee() {
        BHBaseObserver observer = new BHBaseObserver<GasFee>() {
            @Override
            protected void onSuccess(GasFee gasFee) {
                BHUserManager.getInstance().gasFee = new GasFee(gasFee.fee,gasFee.gas);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);

            }
        };

        Type type = (new TypeToken<GasFee>() {}).getType();
        BHttpApi.getService(BHttpApiInterface.class).queryGasfee()
                .compose(RxSchedulersHelper.io_main())
                .compose(RxCache.getDefault().transformObservable(CACHE_GASFEE_KEY, type,getCacheStrategy()))
                .map(new CacheResult.MapFunc<GasFee>())
                .subscribe(observer);
    }

    public synchronized List<BHTokenMapping> getTokenMapping(String symbol){
        List<BHTokenMapping> res = new ArrayList<>();
        for(BHTokenMapping item:mTokenMappings){
            if(item.coin_symbol.equalsIgnoreCase(symbol)){
                res.add(item);
            }
        }
        return res;
    }

    public synchronized LinkedHashMap<String,BHChain> getAllChains(){
        if(!ToolUtils.checkMapEmpty(mChainMap)){
            return mChainMap;
        }


        String jsonArray = MMKVManager.getInstance().mmkv().decodeString(CACHE_TOKENMAP_KEY);
        if(!TextUtils.isEmpty(jsonArray) ){
            List<BHChain> chains = JsonUtils.getListFromJson(jsonArray,BHChain.class);
            StreamSupport.stream(chains).forEach(chain -> {
                mChainMap.put(chain.chain,chain);
            });
            return mChainMap;
        }

        String[] chain_list = BHUserManager.getInstance().getUserBalanceList().split("_");
        //String[] default_chain_name = BaseApplication.getInstance().getResources().getStringArray(R.array.default_chain_name);
        String[] default_chain_name = BHConstants.default_chain;

        IntStreams.range(0,chain_list.length).forEach(value -> {
            BHChain bhChain = new BHChain(chain_list[value],default_chain_name[value]);
            mChainMap.put(bhChain.chain,bhChain);
        });
        return mChainMap;
    }

    public synchronized BHTokenMapping getTokenMappingOne(String symbol){
        for(BHTokenMapping item:mTokenMappings){
            if(item.coin_symbol.equalsIgnoreCase(symbol)){
                return item;
            }
        }
        return null;
    }


    public synchronized BHTokenMapping getTokenMappingFrist(){
        if(!ToolUtils.checkListIsEmpty(mTokenMappings)){
            return mTokenMappings.get(0);
        }
        return null;
    }

    public synchronized List<BHTokenMapping> getTokenMappings() {
        LinkedHashMap<String,BHTokenMapping> maps = new LinkedHashMap<>();
        for (BHTokenMapping item:mTokenMappings) {
            if(maps.get(item.coin_symbol)!=null){
                continue;
            }
            maps.put(item.coin_symbol,item);
        }
        return new ArrayList<>(maps.values());
    }

    public  synchronized BHToken getBHToken(String symbol){
        return mTokens.get(symbol);
    }


}
