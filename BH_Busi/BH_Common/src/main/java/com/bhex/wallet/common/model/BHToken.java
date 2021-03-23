package com.bhex.wallet.common.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.List;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/4/7
 * Time: 18:02
 */
@Entity(tableName = "tab_token",indices = {@Index(value = {"symbol"},unique = true)})
public class BHToken {


    /**
     * symbol : btc
     * issuer :
     * chain : btc
     * type : 1
     * is_send_enabled : true
     * is_deposit_enabled : true
     * is_withdrawal_enabled : true
     * decimals : 8
     * total_supply : 21000000
     * collect_threshold : 0.0002
     * deposit_threshold : 0.0002
     * open_fee : 28
     * sys_open_fee : 28
     * withdrawal_fee : 0.00003
     * max_op_cu_number : 6
     * systransfer_amount : 0.00003
     * op_cu_systransfer_amount : 0.0001
     * is_native : false
     * custodian_amount : 0.02
     * logo :
     * opcus : ["HBCZAhTS5fknRkD69SgxARzTy5FViBHkt1qh","HBCM2s43tEC77x2yfycYUxPwKSjTxHYsMkSy","HBChzkC1FpVhJV24Kaw26wPcxJntPPCjQVx2","HBCjHfVzoupu8FAGVkg1NKqrPHt3brxMbUu7"]
     */
    @NonNull
    @PrimaryKey(autoGenerate = true)
    public int p_id;

    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "symbol")
    public String symbol;
    @ColumnInfo(name = "issuer")
    public String issuer;
    @ColumnInfo(name = "chain")
    public String chain;
    @ColumnInfo(name = "type")
    public int type;
    @ColumnInfo(name = "is_send_enabled")
    public boolean is_send_enabled;
    @ColumnInfo(name = "is_deposit_enabled")
    public boolean is_deposit_enabled;
    @ColumnInfo(name = "is_withdrawal_enabled")
    public boolean is_withdrawal_enabled;
    @ColumnInfo(name = "decimals")
    public int decimals;
    @ColumnInfo(name = "total_supply")
    public String total_supply;
    @ColumnInfo(name = "collect_threshold")
    public String collect_threshold;
    @ColumnInfo(name = "deposit_threshold")
    public String deposit_threshold;
    @ColumnInfo(name = "open_fee")
    public String open_fee;
    @ColumnInfo(name = "sys_open_fee")
    public String sys_open_fee;
    @ColumnInfo(name = "withdrawal_fee")
    public String withdrawal_fee;
    @ColumnInfo(name = "collect_fee")
    public String collect_fee;
    //public String withdrawal_fee_rate;
    @ColumnInfo(name = "max_op_cu_number",defaultValue = "0")
    public Integer max_op_cu_number=0;
    @ColumnInfo(name = "systransfer_amount")
    public String systransfer_amount;
    @ColumnInfo(name = "op_cu_systransfer_amount")
    public String op_cu_systransfer_amount;
    @ColumnInfo(name = "is_native")
    public boolean is_native;
    @ColumnInfo(name = "is_verified")
    public boolean is_verified;
    @ColumnInfo(name = "custodian_amount")
    public String custodian_amount;
    @ColumnInfo(name = "logo")
    public String logo;

    @Ignore
    public List<String> opcus;

    public String getName() {
        return name;
    }

/*@ColumnInfo(name = "default_token") // 1 默认币
    public boolean defaultToken;

    @ColumnInfo(name = "verified_token") //1 官方认证币
    public boolean verifiedToken;*/
}
