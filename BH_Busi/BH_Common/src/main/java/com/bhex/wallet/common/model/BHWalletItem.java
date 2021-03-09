package com.bhex.wallet.common.model;

import com.bhex.wallet.common.db.entity.BHWallet;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/14
 * Time: 22:50
 */
public class BHWalletItem {

    public static int SELECTED = 1;

    public static int UNSELECTED = 0;

    public Integer id;

    public String address;

    public String name;

    public int isBackup; //1备份 2未备份

    public int isDefault;//1默认 0默认

    public String password;

    public String asset;//资产

    public boolean isSelect;

    public String headUrl;


    public static  BHWalletItem makeBHWalletItem(BHWallet bhWallet){
        BHWalletItem item = new BHWalletItem();
        item.id = bhWallet.id;
        item.address = bhWallet.getAddress();
        item.name = bhWallet.getName();
        item.isBackup = bhWallet.getIsBackup();
        item.isDefault = bhWallet.getIsDefault();
        item.password = bhWallet.getPassword();
        return item;

    }
}
