package com.bhex.wallet.market.adapter;

import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.bhex.tools.utils.ImageLoaderUtil;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.cache.TokenMapCache;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.model.BHTokenMapping;
import com.bhex.wallet.market.R;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author gongdongyang
 * 2020-10-10 17:04:36
 */
public class ChooseTokenAdapter extends BaseQuickAdapter<BHTokenMapping, BaseViewHolder> {
    private String mSymbol;
    private int mOrigin;

    public ChooseTokenAdapter( @Nullable List<BHTokenMapping> data,String symbol,int origin) {
        super(R.layout.item_choose_token, data);
        this.mSymbol = symbol;
        this.mOrigin = origin;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, BHTokenMapping item) {
        //token-name
        BHToken target_token = TokenMapCache.getInstance().getBHToken((mOrigin==0)?item.coin_symbol:item.target_symbol);
        holder.setText(R.id.tv_token_name,target_token.name.toUpperCase());
        AppCompatImageView iv_token_icon = holder.getView(R.id.iv_token_icon);
        //BHToken bhToken = CacheCenter.getInstance().getSymbolCache().getBHToken((mOrigin==0)?item.coin_symbol.toLowerCase():item.target_symbol.toLowerCase());
        BHToken bhToken = CacheCenter.getInstance().getSymbolCache().getBHToken((mOrigin==0)?item.coin_symbol:item.target_symbol);

        ImageLoaderUtil.loadImageView(getContext(),bhToken!=null?bhToken.logo:"", iv_token_icon, R.mipmap.ic_default_coin);

        AppCompatTextView tv_token_amount = holder.getView(R.id.tv_token_amount);
        BHBalance balance = BHBalanceHelper.getBHBalanceFromAccount((mOrigin==0)?item.coin_symbol:item.target_symbol);

        //String[]  res = BHBalanceHelper.getAmountToCurrencyValue(getContext(),balance.amount,item.coin_symbol.toLowerCase(),false);
        //tv_token_amount.setText(res[0]);

        tv_token_amount.setText(getContext().getString(R.string.balance)+": "+NumberUtil.dispalyForUsertokenAmount4Level(balance.amount));

        if(mOrigin==0){
            if(item.coin_symbol.equalsIgnoreCase(mSymbol)){
                holder.getView(R.id.ck_token).setVisibility(View.VISIBLE);
            }else{
                holder.getView(R.id.ck_token).setVisibility(View.INVISIBLE);
            }
        }else{
            if(item.target_symbol.equalsIgnoreCase(mSymbol)){
                holder.getView(R.id.ck_token).setVisibility(View.VISIBLE);
            }else{
                holder.getView(R.id.ck_token).setVisibility(View.INVISIBLE);
            }
        }

    }


}
