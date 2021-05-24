package com.bhex.wallet.balance.adapter;

import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.bhex.tools.utils.ImageLoaderUtil;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.manager.CurrencyManager;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.model.BHTokenMapping;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author gongdongyang
 * 2020-10-10 17:04:36
 */
public class ChooseTokenAdapter extends BaseQuickAdapter<BHToken, BaseViewHolder> {
    private String mSymbol;
    //private int mOrigin;

    public ChooseTokenAdapter(@Nullable List<BHToken> data, String symbol) {
        super(R.layout.balance_item_choose_token, data);
        this.mSymbol = symbol;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, BHToken item) {

        holder.setText(R.id.tv_token_name,item.name.toUpperCase());
        AppCompatImageView iv_token_icon = holder.getView(R.id.iv_token_icon);

        ImageLoaderUtil.loadImageView(getContext(),item!=null?item.logo:"", iv_token_icon, R.mipmap.ic_default_coin);

        holder.setText(R.id.tv_token_id,item.symbol);

        //AppCompatTextView tv_token_amount = holder.getView(R.id.tv_token_amount);
        //BHBalance balance = BHBalanceHelper.getBHBalanceFromAccount(item.name);


        //String v_token_amount = NumberUtil.dispalyForUsertokenAmount4Level(balance.amount);

        //tv_token_amount.setText(v_token_amount+" "+item.name.toUpperCase());

        //价格
        //String symbol_prices = CurrencyManager.getInstance().getCurrencyRateDecription(getContext(),item.symbol);
        //holder.setText(R.id.tv_token_price, symbol_prices);
    }


}
