package com.bhex.wallet.balance.presenter;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.widget.CheckedTextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.bhex.network.app.BaseApplication;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.base.BasePresenter;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.adapter.BalanceAdapter;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.balance.model.BHTokenItem;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.CurrencyManager;
import com.bhex.wallet.common.model.AccountInfo;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHChain;
import com.bhex.wallet.common.model.BHToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/18
 * Time: 0:23
 */
public class BalancePresenter extends BasePresenter {

    public BalancePresenter(BaseActivity activity) {
        super(activity);
    }

    /*public List<BHBalance> makeBalanceList(){
        List<BHBalance> list = new ArrayList<>();
        BHWallet wallet = BHUserManager.getInstance().getCurrentBhWallet();
        SymbolCache symbolCache = CacheCenter.getInstance().getSymbolCache();
        String[] coin_list = BHUserManager.getInstance().getUserBalanceList().split("_");
        for (int i = 0; i < coin_list.length; i++) {
            BHBalance bhBalance = BHBalanceHelper.getBHBalanceBySymbol(coin_list[i]);
            BHToken bhToken = symbolCache.getBHToken(bhBalance.symbol.toLowerCase());
            if(bhToken!=null){
                bhBalance.chain = bhToken.chain;
                bhBalance.logo = bhToken.logo;
            }
            if(BHConstants.BHT_TOKEN.equalsIgnoreCase(bhBalance.chain)){
                //bhBalance.address = wallet.address;
            }
            list.add(bhBalance);
        }
        return list;
    }*/


    //获取balance位置
    public  int getIndexByCoin(List<BHBalance> list, BHTokenItem bhCoinItem){
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            BHBalance balance = list.get(i);
            if(balance.symbol.equalsIgnoreCase(bhCoinItem.symbol)){
                return i;
            }
        }
        return index;
    }

    public  BHBalance getBalanceByCoin(BHTokenItem coinItem){
        BHBalance balance = new BHBalance(coinItem.resId,coinItem.symbol);
        //LogUtils.d("BalancePresenter====>:",balance.symbol+"==resId=="+balance.resId);
        balance.chain = coinItem.chain;
        balance.logo = coinItem.logo;
        return balance;
    }


    public List<BHBalance> getBalanceList(List<BHBalance> list,List<BHBalance> res){
        //List<BHBalance> res = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            res.add(list.get(i));

        }
        return res;
    }

    /**
     * 计算所有Token价值
     * @return
     */
    public double calculateAllTokenPrice(Context context,AccountInfo accountInfo, List<BHChain> mOriginBalanceList){
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

            //LogUtils.d("BalanceFragment==>:","amount==="+bean.getAmount()+"=="+symbolPrice);
            double asset = NumberUtil.mul(String.valueOf(amount),String.valueOf(symbolPrice));
            allTokenPrice = NumberUtil.add(asset,allTokenPrice);

        }
        return allTokenPrice;
    }

    /**
     * 隐藏显示资产
     */
    public void hiddenAsset(BaseActivity context, AppCompatTextView tv_asset, AppCompatImageView eyeIv, BalanceAdapter balanceAdapter){
        String tag = (String) eyeIv.getTag();
        if(tag.equals(BH_BUSI_TYPE.显示.value)){
            tv_asset.setText("***");
            eyeIv.setTag(BH_BUSI_TYPE.隐藏.value);
            eyeIv.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_eye_close));
            balanceAdapter.setIsHidden(BH_BUSI_TYPE.隐藏.value);
        }else{
            String unhiddenText = tv_asset.getTag(R.id.tag_first).toString();
            SpannableString spanStr = new SpannableString(unhiddenText);
            spanStr.setSpan(new AbsoluteSizeSpan(PixelUtils.dp2px(getActivity(),15)), 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            tv_asset.setText(spanStr);
            eyeIv.setTag(BH_BUSI_TYPE.显示.value);
            eyeIv.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_eye));
            balanceAdapter.setIsHidden(BH_BUSI_TYPE.显示.value);
        }
    }

    public void hiddenAssetExt(BaseActivity context, AppCompatTextView tv_asset, AppCompatImageView eyeIv){
        String tag = (String) eyeIv.getTag();
        if(tag.equals(BH_BUSI_TYPE.显示.value)){
            tv_asset.setText("***");
            eyeIv.setTag(BH_BUSI_TYPE.隐藏.value);
            eyeIv.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_eye_close_white));
        }else{
            if(tv_asset.getTag(R.id.tag_first)!=null){
                String unhiddenText = tv_asset.getTag(R.id.tag_first).toString();
                SpannableString spanStr = new SpannableString(unhiddenText);
                spanStr.setSpan(new AbsoluteSizeSpan(PixelUtils.dp2px(getActivity(),15)), 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                tv_asset.setText(spanStr);
            }else{
                tv_asset.setText("");
            }
            eyeIv.setTag(BH_BUSI_TYPE.显示.value);
            eyeIv.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_eye_white));
        }
    }


    /**
     * 隐藏小额资产
     * @param context
     * @param ck_hidden_small
     * @param mOriginBalanceList
     * @param text
     * @return
     */
    public List<BHBalance> hiddenSmallToken(BaseActivity context,CheckedTextView ck_hidden_small,List<BHBalance> mOriginBalanceList,String text){
        List<BHBalance> result = new ArrayList<>();

        if(ck_hidden_small.isChecked()){
            for(BHBalance item :mOriginBalanceList){
                if(!TextUtils.isEmpty(item.amount) && Double.valueOf(item.amount)>0){
                    if(!TextUtils.isEmpty(text)){
                        if(item.symbol.toLowerCase().contains(text.toLowerCase())){
                            result.add(item);
                        }
                    }else{
                        result.add(item);
                    }
                }
            }
            ck_hidden_small.setTextColor(ContextCompat.getColor(context,R.color.checkbox_checked_text_color));
        }else{
            for (BHBalance item :mOriginBalanceList) {
                if(!TextUtils.isEmpty(text)){
                    if(item.symbol.toLowerCase().contains(text.toLowerCase())){
                        result.add(item);
                    }
                }else{
                    result.add(item);
                }
            }
            ck_hidden_small.setTextColor(ContextCompat.getColor(context,R.color.checkbox_text_color));
        }

        return result;
    }


    public void setTextFristSamll(AppCompatTextView tv_asset, String allTokenAssetsText) {

        SpannableString spanStr = new SpannableString(allTokenAssetsText);
        spanStr.setSpan(new AbsoluteSizeSpan(PixelUtils.dp2px(getActivity(),16)), 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spanStr.setSpan(new ForegroundColorSpan(ColorUtil.getColor(BaseApplication.getInstance(),R.color.white_60)), 0 ,1,Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tv_asset.setText(spanStr);
        tv_asset.setTag(R.id.tag_first,allTokenAssetsText);
    }


    public BHBalance getBthBalanceWithAccount(AccountInfo accountInfo){
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
                //balance.chain = assetsBean.getSymbol();
                BHToken bhToken = SymbolCache.getInstance().getBHToken(balance.symbol);
                balance.chain = bhToken.chain;
                balance.amount = assetsBean.amount;
                balance.frozen_amount = assetsBean.frozen_amount;
                balance.address = assetsBean.external_address;
            }
        }


        return balance;
    }

    /**
     * 是否为小额资产
     * @param accountInfo
     * @return
     */
    public boolean isSmallToken(AccountInfo accountInfo,String symbol){
        boolean flag = true;
        if(ToolUtils.checkListIsEmpty(accountInfo.assets)){
            return flag;
        }

        for(AccountInfo.AssetsBean assetsBean:accountInfo.assets){
            if(assetsBean.symbol.equalsIgnoreCase(symbol)
                && !TextUtils.isEmpty(assetsBean.amount)
                && Double.valueOf(assetsBean.amount)>0){
                flag = false;
                return flag;
            }
        }
        return flag;
    }
}
