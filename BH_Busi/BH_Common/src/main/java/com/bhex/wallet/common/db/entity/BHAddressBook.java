package com.bhex.wallet.common.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * @author gdy
 * 2021-3-20 11:53:01
 */
@Entity(tableName = "tab_address_book")
public class BHAddressBook {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    public Long id;

    @ColumnInfo(name = "wallet_address")
    public String wallet_address;

    @ColumnInfo(name = "address")
    public String address;

    @ColumnInfo(name = "chain")
    public String chain;

    @ColumnInfo(name = "symbol")
    public String symbol;

    @ColumnInfo(name = "address_name")
    public String address_name;

    @ColumnInfo(name = "address_remark")
    public String address_remark;
}
