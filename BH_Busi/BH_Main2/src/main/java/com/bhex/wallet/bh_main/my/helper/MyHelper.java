package com.bhex.wallet.bh_main.my.helper;

import android.content.Context;
import android.text.TextUtils;

import androidx.appcompat.widget.AppCompatTextView;

import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.bh_main.my.enums.BUSI_MY_TYPE;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.network.utils.PackageUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.language.LocalManageUtil;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.my.model.SecuritySettingItem;
import com.bhex.wallet.bh_main.my.ui.item.MyItem;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.enums.CURRENCY_TYPE;
import com.bhex.wallet.common.enums.MAKE_WALLET_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.CurrencyManager;
import com.bhex.wallet.common.manager.MMKVManager;
import com.bhex.wallet.common.model.AccountInfo;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.manager.SecuritySettingManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/12
 * Time: 11:23
 */
public class MyHelper {

    public static List<MyItem> getAllItems(Context context){

        List<MyItem> myItems = new ArrayList<>();

        String [] res = context.getResources().getStringArray(R.array.my_list_item);

        MyItem item_账户与安全 = new MyItem(BUSI_MY_TYPE.账户与安全.index,res[0], true, "");
        myItems.add(item_账户与安全);

        MyItem item_备份导出 = new MyItem(BUSI_MY_TYPE.备份导出.index,res[1], true, "");
        myItems.add(item_备份导出);

        MyItem item_公告 = new MyItem(BUSI_MY_TYPE.公告.index,res[2], true, "");
        myItems.add(item_公告);

        MyItem item_帮助中心 = new MyItem(BUSI_MY_TYPE.帮助中心.index,res[4], true, "");
        myItems.add(item_帮助中心);

        MyItem item_设置 = new MyItem(BUSI_MY_TYPE.设置.index,res[3], true, "");
        myItems.add(item_设置);

        MyItem item_关于我们 = new MyItem(BUSI_MY_TYPE.关于我们.index,res[5], true, "");
        myItems.add(item_关于我们);
        return myItems;
    }

    /**
     * 地址掩码
     */
    public static void proccessAddress(AppCompatTextView tv_address, String address){
        StringBuffer buf = new StringBuffer("");
        if(!TextUtils.isEmpty(address)){
            buf.append(address.substring(0,10))
                    .append("***")
                    .append(address.substring(address.length()-10,address.length()));
            tv_address.setText(buf.toString());
        }

    }


    public static List<MyItem> getSettingItems(Context context){

        List<MyItem> myItems = new ArrayList<>();

        int  selectIndex = LocalManageUtil.getSetLanguageLocaleIndex(context);

        String []langArray = context.getResources().getStringArray(R.array.app_language_type);

        String [] res = context.getResources().getStringArray(R.array.set_list_item);

        for (int i = 0; i < res.length; i++) {
            MyItem item = new MyItem(i,res[i], true, "");
            myItems.add(item);
        }
        myItems.get(0).rightTxt = langArray[selectIndex-1];
        //语言
        CURRENCY_TYPE.initCurrency(context);
        //String currency_name = MMKVManager.getInstance().mmkv().decodeString(BHConstants.CURRENCY_USED,CURRENCY_TYPE.USD.shortName);
        String currency_name = CurrencyManager.getInstance().loadCurrency(context);
        myItems.get(1).rightTxt = CURRENCY_TYPE.getValue(currency_name).name+"("+CURRENCY_TYPE.getValue(currency_name)+")";

        //
        //String []secTipsArray = context.getResources().getStringArray(R.array.security_list);
        //myItems.get(5).rightTxt = SecuritySettingManager.getInstance().thirty_in_time?secTipsArray[1]:secTipsArray[0];
        return myItems;
    }

    public static String getTitle(Context context,int position){
        String [] res = context.getResources().getStringArray(R.array.my_list_item);
        return res[position];
    }


    public static BHBalance getBthBalanceWithAccount(AccountInfo accountInfo){
        if(accountInfo==null){
            return null;
        }
        BHBalance balance = new BHBalance();
        balance.amount="";
        balance.chain= BHConstants.BHT_TOKEN;
        balance.symbol = BHConstants.BHT_TOKEN;

        List<AccountInfo.AssetsBean> assetsBeanList = accountInfo.assets;
        if(assetsBeanList==null || assetsBeanList.size()==0){
            return balance;
        }

        for(AccountInfo.AssetsBean assetsBean:assetsBeanList){
            if(assetsBean.symbol.equalsIgnoreCase(BHConstants.BHT_TOKEN)){
                balance.symbol = assetsBean.symbol;
                BHToken bhToken = SymbolCache.getInstance().getBHToken(balance.symbol);
                balance.chain = bhToken.chain;
                balance.amount = assetsBean.amount;
                balance.frozen_amount = assetsBean.frozen_amount;
                balance.address = assetsBean.external_address;
            }
        }


        return balance;
    }

    public static String getAmountForUser(BaseActivity context, String amount, String frozen_amount, String symbol) {
        SymbolCache symbolCache = CacheCenter.getInstance().getSymbolCache();
        BHToken bhToken = symbolCache.getBHToken(symbol);
        int decimals = bhToken!=null?bhToken.decimals:2;
        decimals = 0;
        String tmp = NumberUtil.sub(TextUtils.isEmpty(amount)?"0":amount,TextUtils.isEmpty(frozen_amount)?"0":frozen_amount);

        double displayAmount = NumberUtil.divide(tmp, Math.pow(10,decimals)+"");

        return NumberUtil.dispalyForUsertokenAmount4Level(String.valueOf(displayAmount));
    }

    //安全设置
    public static List<SecuritySettingItem> getSecSettingItems(Context context){
        List<SecuritySettingItem> myItems = new ArrayList<>();
        String [] res = context.getResources().getStringArray(R.array.security_list);
        for (int i = 0; i < res.length; i++) {
            if(i==0){
                SecuritySettingItem item = new SecuritySettingItem(i,res[i],!SecuritySettingManager.getInstance().thirty_in_time);
                myItems.add(item);
            }else if(i==1){
                SecuritySettingItem item = new SecuritySettingItem(i,res[i], SecuritySettingManager.getInstance().thirty_in_time);
                myItems.add(item);
            }
        }
        return myItems;
    }

    //关于我们
    public static List<MyItem> getAboutUs(Context context){
        List<MyItem> myItems = new ArrayList<>();
        String []res = context.getResources().getStringArray(R.array.about_us_list);

        MyItem item0 = new MyItem(0,res[0], true, "");
        myItems.add(item0);

        MyItem item1 = new MyItem(1,res[1], false, BHConstants.EMAIL);
        myItems.add(item1);

        return myItems;
    }

    //账户详情
    public static List<MyItem> getAcountDetailFunction(Context context, BHWallet chooseWallet){
        List<MyItem> myItems = new ArrayList<>();
        String []res = context.getResources().getStringArray(R.array.account_detail_list);
        for (int i = 0; i < res.length; i++) {
            MyItem item = new MyItem(i,res[i], true, "");
            myItems.add(item);
        }
        //创建和导入助记词 能备份
        if(chooseWallet.getWay()!= MAKE_WALLET_TYPE.创建助记词.getWay() &&
                chooseWallet.getWay()!= MAKE_WALLET_TYPE.导入助记词.getWay()){
            myItems.remove(2);
        }
        return myItems;
    }

    public static List<MyItem> getAccountSecurityList(Context context){
        List<MyItem> myItems = new ArrayList<>();
        String []res = context.getResources().getStringArray(R.array.account_securtiy_list);
        for (int i = 0; i < res.length; i++) {
            MyItem item = new MyItem(i,res[i], true, "");
            myItems.add(item);
        }
        return myItems;
    }


    public static List<MyItem> getBackupList(Context context){
        List<MyItem> myItems = new ArrayList<>();
        String []res = context.getResources().getStringArray(R.array.backup_list);
        for (int i = 0; i < res.length; i++) {
            MyItem item = new MyItem(i,res[i], true, "");
            myItems.add(item);
        }

        BHWallet currentWallet = BHUserManager.getInstance().getCurrentBhWallet();
        //创建和导入助记词 能备份
        if(currentWallet.getWay()!= MAKE_WALLET_TYPE.创建助记词.getWay() && currentWallet.getWay()!= MAKE_WALLET_TYPE.导入助记词.getWay()){
            myItems.remove(0);
        }

        return myItems;
    }
}
