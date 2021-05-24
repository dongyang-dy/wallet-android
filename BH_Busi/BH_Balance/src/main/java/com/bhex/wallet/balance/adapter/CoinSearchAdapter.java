package com.bhex.wallet.balance.adapter;

import androidx.appcompat.widget.AppCompatCheckedTextView;
import androidx.appcompat.widget.AppCompatImageView;

import com.bhex.tools.utils.ImageLoaderUtil;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.helper.BHTokenHelper;
import com.bhex.wallet.common.model.BHToken;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/4/3
 * Time: 15:20
 */
public class CoinSearchAdapter extends BaseQuickAdapter<BHToken, BaseViewHolder> {

    public CoinSearchAdapter( @Nullable List<BHToken> data) {
        super(R.layout.item_seach_coin, data);
        addChildClickViewIds(R.id.ck_select);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder viewHolder,
                           @Nullable BHToken coin) {

        viewHolder.setText(R.id.tv_coin_name,coin.name.toUpperCase());

        AppCompatCheckedTextView ck = viewHolder.getView(R.id.ck_select);
        ck.setChecked(false);
        AppCompatImageView iv_coin_ic = viewHolder.getView(R.id.iv_coin_ic);

        ImageLoaderUtil.loadImageView(getContext(),coin.logo,iv_coin_ic,R.mipmap.ic_default_coin);

        //判断是否在官方列表存在
        boolean flag = BHTokenHelper.isExistDefaultToken(coin.symbol);
        ck.setChecked(flag);
    }
}
