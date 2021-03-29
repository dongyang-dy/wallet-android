package com.bhex.wallet.bh_main.my.adapter;

import android.view.View;

import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.my.model.CurrencyItem;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/4/18
 * Time: 19:01
 */
public class RateAdapter extends BaseQuickAdapter<CurrencyItem, BaseViewHolder> {

    public RateAdapter(@Nullable List<CurrencyItem> data) {
        super(R.layout.item_rate, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder helper, @Nullable CurrencyItem item) {
        helper.setText(R.id.tv_language,item.fullName+"("+item.shortName+")");
        helper.getView(R.id.iv_choosed).setVisibility(View.INVISIBLE);
        if(item.selected){
            helper.getView(R.id.iv_choosed).setVisibility(View.VISIBLE);
        }
    }
}
