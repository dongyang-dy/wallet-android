package com.bhex.wallet.mnemonic.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.View;

import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.bhex.network.base.LoadingStatus;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.helper.BHWalletHelper;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.CurrencyManager;
import com.bhex.wallet.common.manager.MainActivityManager;
import com.bhex.wallet.common.model.AccountInfo;
import com.bhex.wallet.common.model.BHWalletItem;
import com.bhex.wallet.common.viewmodel.BalanceViewModel;
import com.bhex.wallet.mnemonic.R;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java8.util.stream.StreamSupport;

/**
 * @author gongdongyang
 * 账户管理
 * 2021-3-2 22:04:30
 */
public class AccountManagerAdapter extends BaseQuickAdapter<BHWalletItem, BaseViewHolder>  {
    public AccountManagerAdapter( @Nullable List<BHWalletItem> data) {
        super(R.layout.item_account_manager, data);

    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, BHWalletItem bhWallet) {
        //钱包名称
        holder.setText(R.id.tv_wallet_name,bhWallet.name);
        //钱包地址
        BHWalletHelper.proccessAddress(holder.getView(R.id.tv_wallet_address),bhWallet.address);

        BHWallet currentWallet = BHUserManager.getInstance().getCurrentBhWallet();

        if(currentWallet.address.equals(bhWallet.address)){
            holder.getView(R.id.iv_wallet_check).setVisibility(View.VISIBLE);
        }else{
            holder.getView(R.id.iv_wallet_check).setVisibility(View.INVISIBLE);
        }

        //LogUtils.d("AccountManagerAdapter==","bhWallet.asset==>:"+bhWallet.asset);
        if(!TextUtils.isEmpty(bhWallet.asset)){
            holder.setText(R.id.tv_asset,bhWallet.asset);
        }

    }

    public void updateAsset(AccountInfo accountInfo){
        if(accountInfo!=null){
            List<BHWalletItem> bhWalletList = (List<BHWalletItem>)getData();
            StreamSupport.stream(bhWalletList).forEach(item->{
                if(item.address.equals(accountInfo.address)){
                    double d_asset = calculateAllTokenPrice(getContext(),accountInfo);
                    String v_asset = CurrencyManager.getInstance().getCurrencyDecription(getContext(),d_asset);
                    item.asset = v_asset;
                    notifyDataSetChanged();
                }
            });
        }
    }

    /**
     * 计算所有Token价值
     * @return
     */
    public static double calculateAllTokenPrice(Context context, AccountInfo accountInfo){
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
}
