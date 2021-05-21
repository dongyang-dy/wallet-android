package com.bhex.wallet.common.model;

import androidx.room.TypeConverter;

import com.bhex.network.utils.JsonUtils;

import java.util.List;

public class BHTokenConverter {

    @TypeConverter
    public String objectToString(List<BHToken> list) {
        return JsonUtils.toJson(list);
    }

    @TypeConverter
    public List<BHToken> stringToObject(String json) {
        return JsonUtils.getListFromJson(json,BHToken.class);

    }

}
