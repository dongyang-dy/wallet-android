package com.bhex.wallet.balance.adapter;

import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.ImageLoaderUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.enums.CURRENCY_TYPE;
import com.bhex.wallet.common.manager.CurrencyManager;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHChain;
import com.bhex.wallet.common.model.BHToken;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/18
 * Time: 0:18
 */
public class ChainAdapter extends BaseQuickAdapter<BHChain, BaseViewHolder> {

    private String isHidden = "0";

    public ChainAdapter(@Nullable List<BHChain> data) {
        super(R.layout.item_chain, data);
    }

    @Override
    public void onBindViewHolder(@NotNull BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder viewHolder, @Nullable BHChain bhChain) {
        AppCompatImageView iv = viewHolder.getView(R.id.iv_coin);
        iv.setImageResource(0);

        SymbolCache symbolCache  = CacheCenter.getInstance().getSymbolCache();

        BHToken bhCoin = symbolCache.getBHToken(bhChain.chain.toLowerCase());

        ImageLoaderUtil.loadImageView(getContext(),bhCoin!=null?bhCoin.logo:"", iv,R.mipmap.ic_default_coin);

        viewHolder.setText(R.id.tv_coin_name,bhChain.chain.toUpperCase());

        AppCompatTextView tv_coin_type = viewHolder.getView(R.id.tv_coin_type);

        //实时价格
        //String symbol_prices = CurrencyManager.getInstance().getCurrencyRateDecription(getContext(),balanceItem.symbol);

        viewHolder.setText(R.id.tv_coin_price, bhChain.full_name);
        viewHolder.setText(R.id.tv_coin_count, "");
        //价格
        double value_double = BHBalanceHelper.getAssetByChain(getContext(),bhChain.chain);
        //LogUtils.d("ChainAdapter===>:","bhChain.chain=="+bhChain.chain+"==value_double=="+value_double);
        String value_str = CurrencyManager.getInstance().getCurrencyDecription(getContext(),value_double);
        if(isHidden.equals("0")){
            viewHolder.setText(R.id.tv_coin_count,"≈"+value_str);
        }else {
            viewHolder.setText(R.id.tv_coin_count, "***");
        }

        tv_coin_type.setVisibility(View.VISIBLE);

        if(bhChain.chain.equalsIgnoreCase(BHConstants.BHT_TOKEN)){
            //tv_coin_type.setText(getContext().getString(R.string.native_token_test_list));
            tv_coin_type.setText(getContext().getString(R.string.string_native));
            tv_coin_type.setTextColor(ColorUtil.getColor(getContext(),R.color.native_token_text_color));
            //tv_coin_type.setTextAppearance(getContext(),R.style.tx_native_link_token);
            //tv_coin_type.setBackgroundDrawable(ContextCompat.getDrawable(getContext(),R.drawable.shape_native_token));
        }else{
            //tv_coin_type.setText(getContext().getString(R.string.cross_chain_token_list));
            tv_coin_type.setText(getContext().getString(R.string.crosslink));
            tv_coin_type.setTextColor(ColorUtil.getColor(getContext(),R.color.cross_token_text_color));
            //tv_coin_type.setTextAppearance(getContext(),R.style.tx_cross_link_token);
            //tv_coin_type.setBackgroundDrawable(ContextCompat.getDrawable(getContext(),R.drawable.shape_dark20_blue));

        }
    }

    public void changeAssets() {

    }

    public String getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(String isHidden) {
        this.isHidden = isHidden;
        notifyDataSetChanged();
    }


}
