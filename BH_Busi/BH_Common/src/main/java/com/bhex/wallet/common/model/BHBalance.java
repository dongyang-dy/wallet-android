package com.bhex.wallet.common.model;

import androidx.room.Ignore;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/18
 * Time: 0:19
 */
public class BHBalance {
    public int resId;

    public String name;
    //币种
    public String symbol;
    //当前价格
    public double price;
    //数量
    public String amount;
    //账户余额
    public double asset;

    //币的地址
    public String address;

    public String chain = "";

    public String external_address;

    public boolean is_native;

    public String logo="";

    //是否能发送交易
    public boolean enable_sendtx;

    public String frozen_amount;

    public int index;

    @Ignore
    public String fullName;

    public BHBalance() {
    }

    public BHBalance(int resId, String symbol) {
        this.resId = resId;
        this.symbol = symbol;
    }

}
