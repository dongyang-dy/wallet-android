package com.bhex.wallet.common.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/4/7
 * Time: 17:51
 */
public class CacheCenter {

    private static Map<String, CacheLisenter> cacheLisenterMap = new HashMap();

    private static volatile CacheCenter instance;

    public static CacheCenter getInstance() {
        if(instance==null){
            synchronized (CacheCenter.class){
                if(instance==null){
                    instance = new CacheCenter();
                }
            }
        }
        return instance;
    }

    private  CacheCenter(){
        cacheLisenterMap.put(SymbolCache.CACHE_KEY, SymbolCache.getInstance());
        cacheLisenterMap.put(ConfigMapCache.CACHE_CHAIN_KEY, ConfigMapCache.getInstance());
        cacheLisenterMap.put(RatesCache.CACHE_KEY, RatesCache.getInstance());
    }

    /**
     * Symbol缓存
     * @return
     */
    public synchronized SymbolCache getSymbolCache(){
        if (cacheLisenterMap.containsKey(SymbolCache.CACHE_KEY)){
            return (SymbolCache)cacheLisenterMap.get(SymbolCache.CACHE_KEY);
        }
        cacheLisenterMap.put(SymbolCache.CACHE_KEY, SymbolCache.getInstance());
        return (SymbolCache)cacheLisenterMap.get(SymbolCache.CACHE_KEY);
    }

    public synchronized RatesCache getRatesCache(){
        if (cacheLisenterMap.containsKey(RatesCache.CACHE_KEY)){
            return (RatesCache)cacheLisenterMap.get(RatesCache.CACHE_KEY);
        }
        cacheLisenterMap.put(RatesCache.CACHE_KEY, RatesCache.getInstance());
        return (RatesCache)cacheLisenterMap.get(RatesCache.CACHE_KEY);
    }

    public synchronized ConfigMapCache getTokenMapCache(){
        if (cacheLisenterMap.containsKey(ConfigMapCache.CACHE_CHAIN_KEY)){
            return (ConfigMapCache)cacheLisenterMap.get(ConfigMapCache.CACHE_CHAIN_KEY);
        }
        cacheLisenterMap.put(ConfigMapCache.CACHE_CHAIN_KEY, ConfigMapCache.getInstance());
        return (ConfigMapCache)cacheLisenterMap.get(ConfigMapCache.CACHE_CHAIN_KEY);
    }

    /**
     * 加载缓存信息
     */
    public synchronized void loadCache(){
        for (String key : cacheLisenterMap.keySet()) {
            if (cacheLisenterMap.get(key) instanceof CacheLisenter){
                cacheLisenterMap.get(key).beginLoadCache();
            }
        }
    }
}
