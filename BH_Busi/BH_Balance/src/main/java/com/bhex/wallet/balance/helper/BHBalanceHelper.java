package com.bhex.wallet.balance.helper;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.ArrayMap;
import android.util.Log;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.bhex.network.app.BaseApplication;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.ImageLoaderUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.CurrencyManager;
import com.bhex.wallet.common.menu.MenuItem;
import com.bhex.wallet.common.model.AccountInfo;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHChain;
import com.bhex.wallet.common.model.BHToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/4/5
 * Time: 16:51
 */
public class BHBalanceHelper {

    /*public static BHBalance getBHBalanceBySymbol(String symbol){
        BHBalance item = new BHBalance();
        if(symbol.equalsIgnoreCase(BHConstants.BHT_TOKEN)){
            item.resId = R.mipmap.ic_token;
        }else if(symbol.equalsIgnoreCase("btc")){
            item.resId = R.mipmap.ic_btc;
        }else if(symbol.equalsIgnoreCase("eth")){
            item.resId = R.mipmap.ic_eth;
        }else if(symbol.equalsIgnoreCase("eos")){
            item.resId = R.mipmap.ic_eos;
        }else if(symbol.equalsIgnoreCase("usdt")){
            item.resId = R.mipmap.ic_usdt;
        }else{
            item.resId = 0;
        }
        item.symbol = symbol;
        return item;
    }*/


    /**
     * 资产对应法币价值
     * @param amount
     * @param symbol
     * @return
     */
    public static String[]  getAmountToCurrencyValue(Context context,String amount, String symbol,boolean flag){
        String []result = new String[2];
        BHToken bhToken = CacheCenter.getInstance().getSymbolCache().getBHToken(symbol);
        int decimals = bhToken!=null?bhToken.decimals:2;
        if(!flag){
            decimals = 0;
        }

        double displayAmount = NumberUtil.divide(amount, Math.pow(10,decimals)+"");
        result[0] = NumberUtil.dispalyForUsertokenAmount4Level(String.valueOf(displayAmount));
        //法币价值
        double symbolPrice = CurrencyManager.getInstance().getCurrencyRate(context,symbol);
        double asset = NumberUtil.mul(String.valueOf(displayAmount),String.valueOf(symbolPrice));
        result[1] = CurrencyManager.getInstance().getCurrencyDecription(context,asset);

        return result;
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

    public static void loadTokenIcon(Context context, AppCompatImageView iv,String symbol){
        BHToken item = CacheCenter.getInstance().getSymbolCache().getBHToken(symbol.toLowerCase());
        ImageLoaderUtil.loadImageView(context,item!=null?item.logo:"", iv,R.mipmap.ic_default_coin);
    }

    /**
     * H
     * @param symbol
     * @return
     */
    public static BHBalance getBHBalanceFromAccount(String symbol){
        AccountInfo accountInfo = BHUserManager.getInstance().getAccountInfo();
        BHBalance balance = new BHBalance();
        BHToken bhToken = CacheCenter.getInstance().getSymbolCache().getBHToken(symbol);
        balance.amount="0";
        balance.symbol = symbol;
        if(bhToken!=null){
            balance.name = bhToken.name;
            balance.chain = bhToken.chain;
        }
        if(accountInfo==null){
            return balance;
        }
        List<AccountInfo.AssetsBean> list = accountInfo.assets;
        if(list==null || list.size()==0){
            return balance;
        }

        for (int i = 0; i < list.size(); i++) {
            AccountInfo.AssetsBean assetsBean = list.get(i);
            if(assetsBean.symbol==null || !assetsBean.symbol.equalsIgnoreCase(symbol)){
                continue;
            }
            balance.amount = assetsBean.amount;
            balance.frozen_amount = assetsBean.frozen_amount;
            balance.address = assetsBean.external_address;
            balance.external_address = assetsBean.external_address;
            balance.is_native = assetsBean.is_native;
            return balance;
        }
        return balance;
    }

    public static ArrayList<MenuItem> loadCrossActionList(Context context){
        String []operator_list = context.getResources().getStringArray(R.array.Cross_operator_list);
        ArrayList<MenuItem> list = new ArrayList<>();
        for (String item:operator_list) {
            MenuItem menuItem = new MenuItem(item,false);
            list.add(menuItem);
        }
        return list;
    }



    //
    public static List<BHToken> loadTokenList(String symbol,String flag){
        BHToken chainToken =  CacheCenter.getInstance().getSymbolCache().getBHToken(symbol);
        //flag 1 非hbc链功能
        LinkedHashMap<String,BHToken> map_tokens =  CacheCenter.getInstance().getSymbolCache().getLocalToken();
        List<BHToken> res = new ArrayList<>();
        for (ArrayMap.Entry<String,BHToken> entry:map_tokens.entrySet()){
            if(flag.equals(BH_BUSI_TYPE.跨链转账.value)){
                if(entry.getValue().chain.toLowerCase().equals(chainToken.chain)){
                    res.add(entry.getValue());
                }
            }else{
                res.add(entry.getValue());
            }

        }

        Collections.sort(res,((o1, o2) -> {
            String n1 =  o1.name;
            String n2 =  o2.name;
            return n1.compareTo(n2);
        }));
        return res;
    }


    //获取链下的资产
    public static double getAssetByChain(Context context,String chain){
        double res = 0;
        AccountInfo accountInfo = BHUserManager.getInstance().getAccountInfo();
        if(accountInfo==null){
            return res;
        }
        List<AccountInfo.AssetsBean> list = accountInfo.assets;
        if(ToolUtils.checkListIsEmpty(list)){
            return res;
        }
        for (int i = 0; i < list.size(); i++) {
            AccountInfo.AssetsBean assetsBean = list.get(i);
            BHToken bhToken = CacheCenter.getInstance().getSymbolCache().getBHToken(assetsBean.symbol);
            if(bhToken==null){
                continue;
            }
            if(!bhToken.chain.equalsIgnoreCase(chain)){
                continue;
            }
            //
            double displayAmount = TextUtils.isEmpty(assetsBean.amount)? 0:Double.valueOf(assetsBean.amount);
            //法币价值
            double symbolPrice = CurrencyManager.getInstance().getCurrencyRate(context,assetsBean.symbol);
            double asset = NumberUtil.mul(String.valueOf(displayAmount),String.valueOf(symbolPrice));
            res +=asset;
        }
        return res;
    }

    /**
     * 计算所有Token价值
     * @return
     */
    public static double calculateAllTokenPrice(Context context,AccountInfo accountInfo){
        double allTokenPrice = 0;
        List<AccountInfo.AssetsBean> list = accountInfo.assets;
        if(list==null || list.size()==0){
            return allTokenPrice;
        }
        Map<String,AccountInfo.AssetsBean> map = new HashMap<>();
        for(AccountInfo.AssetsBean bean:list){
            map.put(bean.symbol,bean);
            //计算每一个币种的资产价值
            double amount = TextUtils.isEmpty(bean.amount)?0:Double.valueOf(bean.amount);
            //法币价值
            double symbolPrice = CurrencyManager.getInstance().getCurrencyRate(context,bean.symbol);
            double asset = NumberUtil.mul(String.valueOf(amount),String.valueOf(symbolPrice));
            allTokenPrice = NumberUtil.add(asset,allTokenPrice);

        }
        return allTokenPrice;
    }


    public static void setTextFristSamll(Context context,AppCompatTextView tv_asset, String allTokenAssetsText) {

        SpannableString spanStr = new SpannableString(allTokenAssetsText);
        spanStr.setSpan(new AbsoluteSizeSpan(PixelUtils.dp2px(context,16)), 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spanStr.setSpan(new ForegroundColorSpan(ColorUtil.getColor(BaseApplication.getInstance(),R.color.white_60)), 0 ,1,Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tv_asset.setText(spanStr);
        tv_asset.setTag(R.id.tag_first,allTokenAssetsText);
    }


    //获取链地址
    public static String queryAddressByChain(String chain){
        AccountInfo accountInfo = BHUserManager.getInstance().getAccountInfo();
        if(accountInfo==null){
            return "";
        }
        if(ToolUtils.checkListIsEmpty(accountInfo.assets)){
            return "";
        }

        List<AccountInfo.AssetsBean> assetList = accountInfo.assets;

        for(AccountInfo.AssetsBean assetsBean:assetList){
            if(!assetsBean.chain.equals(chain)){
                continue;
            }
            return  assetsBean.external_address;
        }
        return "";
    }
}
