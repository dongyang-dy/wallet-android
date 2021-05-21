package com.bhex.wallet.balance.adapter;

import android.text.TextUtils;

import com.bhex.tools.utils.ImageLoaderUtil;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.common.enums.CURRENCY_TYPE;
import com.bhex.wallet.common.manager.CurrencyManager;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHToken;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author gdy
 * 2021-5-21 10:53:17
 */
public class HBalanceAdapter extends BaseQuickAdapter<BHToken, BaseViewHolder> {

    private boolean isOpenEye = true;

    public HBalanceAdapter(@Nullable List<BHToken> data) {
        super(R.layout.item_chain, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder helper, BHToken bhToken) {
        //显示Logo
        ImageLoaderUtil.loadImageView(getContext(),bhToken.logo,helper.getView(R.id.iv_icon),R.mipmap.ic_default_coin);
        //显示名称
        helper.setText(R.id.tv_token_name,bhToken.name.toUpperCase());

        //获取Token数量
        BHBalance balanceItem = BHBalanceHelper.getBHBalanceFromAccount(bhToken.symbol);

        //更新资产和数量
        updatePriceAndAmount(balanceItem,helper);
    }

    //更新资产和数量
    private void updatePriceAndAmount(BHBalance balanceItem,BaseViewHolder viewHolder){

        if(viewHolder==null){
            return;
        }
        //币的数量
        if(isOpenEye){
            if(!TextUtils.isEmpty(balanceItem.amount) && Double.valueOf(balanceItem.amount)>0) {
                String []result = BHBalanceHelper.getAmountToCurrencyValue(getContext(),balanceItem.amount,balanceItem.symbol,false);
                viewHolder.setText(R.id.tv_token_amount, result[0]);
                viewHolder.setText(R.id.tv_token_value, "≈"+result[1]);
            }else{
                viewHolder.setText(R.id.tv_token_amount, "0");
                viewHolder.setText(R.id.tv_token_value, "≈"+
                        CURRENCY_TYPE.valueOf(CurrencyManager.getInstance().loadCurrency(getContext()).toUpperCase()).character+"0");
            }
        }else{
            viewHolder.setText(R.id.tv_token_amount, "***");
            viewHolder.setText(R.id.tv_token_value, "***");
        }
    }

    public void setOpenEye(boolean isOpenEye){
        this.isOpenEye = isOpenEye;
        notifyDataSetChanged();
    }
}
