package com.bhex.wallet.balance.helper;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.cache.ConfigMapCache;
import com.bhex.wallet.common.model.BHChain;
import com.bhex.wallet.common.model.BHToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author gongdongyang
 * 2020年10月31日17:28:53
 */
public class BHTokenHelper {

    public static List<BHToken> loadVerifiedToken(String chain) {
        //官方认证
        LinkedHashMap<String,BHToken> map_tokens = CacheCenter.getInstance().getSymbolCache().getVerifiedToken();
        //本地存储
        LinkedHashMap<String, BHToken> local_tokens = CacheCenter.getInstance().getSymbolCache().getLocalToken();
        //合并
        ArrayMap<String, BHToken> merge_tokens = new ArrayMap<>();

        if (local_tokens != null && local_tokens.size() > 0) {
            merge_tokens.putAll(local_tokens);
        }

        if(map_tokens!=null && map_tokens.size()>0){
            merge_tokens.putAll(map_tokens);
        }

        List<BHToken> res = new ArrayList<>();
        for (ArrayMap.Entry<String, BHToken> item : merge_tokens.entrySet()) {
            if (!item.getValue().chain.equalsIgnoreCase(chain)) {
                continue;
            }
            res.add(item.getValue());
        }
        return res;
    }


    public static boolean isExistDefaultToken(String symbol) {
        LinkedHashMap<String, BHToken> map_tokens = CacheCenter.getInstance().getSymbolCache().getLocalToken();
        if (map_tokens.get(symbol) != null) {
            return true;
        }
        return false;
    }


    public static BHToken getCrossDefaultToken(String symbol) {
        BHToken bhToken = CacheCenter.getInstance().getSymbolCache().getBHToken(symbol);
        if (bhToken == null) {
            return null;
        }

        if (ToolUtils.checkListIsEmpty(bhToken.uni_tokens)) {
            return null;
        }


        return bhToken.uni_tokens.get(0);
    }

    public static BHToken getCrossBHToken(String symbol, String chain) {
        BHToken bhToken = CacheCenter.getInstance().getSymbolCache().getBHToken(symbol);
        if (bhToken == null) {
            return null;
        }

        if (ToolUtils.checkListIsEmpty(bhToken.uni_tokens)) {
            return null;
        }

        for (BHToken item : bhToken.uni_tokens) {
            if (!item.chain.equalsIgnoreCase(chain)) {
                continue;
            }
            return item;
        }
        return null;
    }


    public static BHChain getBHChain(String chain) {
        return ConfigMapCache.getInstance().getAllChains().get(chain);
    }

    public static List<BHChain> getBHChainList(String symbol) {
        BHToken bhToken = CacheCenter.getInstance().getSymbolCache().getBHToken(symbol);
        if (bhToken == null) {
            return null;
        }

        if (ToolUtils.checkListIsEmpty(bhToken.uni_tokens)) {
            return null;
        }
        List<BHChain> res = new ArrayList<>();
        LinkedHashMap<String, BHChain> chainMap = ConfigMapCache.getInstance().getAllChains();
        for (BHToken item : bhToken.uni_tokens) {
            if (chainMap.get(bhToken.chain) == null) {
                continue;
            }
            res.add(chainMap.get(item.chain));
        }
        return res;
    }

    public static List<BHChain> getAllBHChainList() {
        List<BHChain> res = new ArrayList<>(ConfigMapCache.getInstance().getAllChains().values());
        return res;
    }

    //获取默认Token列表
    public static List<BHToken> loadDefaultToken() {
        LinkedHashMap<String,BHToken> default_tokens = CacheCenter.getInstance().getSymbolCache().getLocalToken();
        List<BHToken> res = new ArrayList<>();

        if(default_tokens.isEmpty()){
            return res;
        }

        for(ArrayMap.Entry<String,BHToken> item : default_tokens.entrySet()){
            if(!item.getValue().chain.equalsIgnoreCase(BHConstants.BHT_TOKEN)){
                continue;
            }
            res.add(item.getValue());
        }
        return res;
    }


    public static List<BHToken> loadTokenByChain(String chainName){
        LinkedHashMap<String,BHToken> map_tokens =  CacheCenter.getInstance().getSymbolCache().getLocalToken();
        List<BHToken> res = new ArrayList<>();
        for (ArrayMap.Entry<String,BHToken> entry:map_tokens.entrySet()){
            if(!entry.getValue().chain.equalsIgnoreCase(chainName)){
                continue;
            }
            res.add(entry.getValue());
        }
        return res;
    }


    public static List<BHToken> sortBHToken(Context context,List<BHToken> orginTokens){
        if(ToolUtils.checkListIsEmpty(orginTokens)){
            return  orginTokens;
        }

        for(BHToken bhToken:orginTokens){
            if(bhToken==null || TextUtils.isEmpty(bhToken.symbol)){
                continue;
            }
            bhToken.amount = BHBalanceHelper.getAmountToCurrencyValue(context,bhToken.symbol);
        }


        Collections.sort(orginTokens, (o1,o2)->{
            try{
                if(Double.valueOf(o2.amount)>Double.valueOf(o1.amount)){
                    return 1;
                }else if(Double.valueOf(o2.amount)<Double.valueOf(o1.amount)){
                    return -1;
                }else{
                    return o1.name.compareToIgnoreCase(o2.name);
                }
            }catch (Exception e){
                return 0;
            }
        });
        //HBC放最前
        List<BHToken> result = new ArrayList<>();
        for(BHToken bhToken:orginTokens){
            if(bhToken.name.equalsIgnoreCase(BHConstants.BHT_TOKEN)){
                result.add(bhToken);
            }
        }

        for(BHToken bhToken:orginTokens){
            if(!bhToken.name.equalsIgnoreCase(BHConstants.BHT_TOKEN)){
                result.add(bhToken);
            }
        }

        //LogUtils.d("BHTokenHelper====>", Arrays.deepToString(orginTokens.toArray()));
        return result;
    }

}