package com.bhex.wallet.common.model;

import android.text.TextUtils;

import androidx.room.TypeConverter;

import com.bhex.network.utils.JsonUtils;
import com.bhex.tools.utils.LogUtils;

import java.io.Serializable;
import java.util.List;

public class BHTokenConverter implements Serializable {

    @TypeConverter
    public String objectToString(List<BHToken> list) {
        return JsonUtils.toJson(list);
    }

    @TypeConverter
    public List<BHToken> stringToObject(String json) {
        try{
            if(TextUtils.isEmpty(json)){
                return null;
            }
            return JsonUtils.getListFromJson(json,BHToken.class);
        }catch (Exception e){
            return null;
        }


    }

}
