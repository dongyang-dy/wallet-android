package com.bhex.wallet.balance.helper;

import android.util.ArrayMap;

import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.model.BHToken;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author gongdongyang
 * 2020年10月31日17:28:53
 */
public class TokenHelper {

    public static List<BHToken> loadVerifiedToken(String chain) {
        //官方认证
        //ArrayMap<String,BHToken> map_tokens = CacheCenter.getInstance().getSymbolCache().getVerifiedToken();
        //本地存储
        LinkedHashMap<String,BHToken> local_tokens = CacheCenter.getInstance().getSymbolCache().getLocalToken();
        //合并
        ArrayMap<String,BHToken> merge_tokens = new ArrayMap<>();

        if(local_tokens!=null && local_tokens.size()>0){
            merge_tokens.putAll(local_tokens);
        }

        /*if(map_tokens!=null && map_tokens.size()>0){
            merge_tokens.putAll(map_tokens);
        }*/

        List<BHToken> res = new ArrayList<>();
        for (ArrayMap.Entry<String,BHToken> item : merge_tokens.entrySet()) {
            if (!item.getValue().chain.equalsIgnoreCase(chain)) {
                continue;
            }
            res.add(item.getValue());
        }
        return res;
    }



    public static boolean isExistDefaultToken(String symbol) {
        LinkedHashMap<String,BHToken> map_tokens = CacheCenter.getInstance().getSymbolCache().getLocalToken();
        if(map_tokens.get(symbol)!=null){
            return true;
        }
        return false;
    }

}
