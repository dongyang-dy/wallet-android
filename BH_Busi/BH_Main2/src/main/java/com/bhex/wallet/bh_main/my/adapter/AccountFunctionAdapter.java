package com.bhex.wallet.bh_main.my.adapter;

import android.view.View;

import com.bhex.tools.utils.ColorUtil;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.my.ui.item.MyItem;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author gongdongyang
 */
public class AccountFunctionAdapter extends BaseQuickAdapter<MyItem, BaseViewHolder> {
    private String m_wallet_name;
    public AccountFunctionAdapter(@Nullable List<MyItem> data,String wallet_name) {
        super(R.layout.item_account_detail, data);
        m_wallet_name = wallet_name;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, MyItem myItem) {
        //设置Label
        holder.setText(R.id.tv_title,myItem.title);
        if(myItem.title.equals(getContext().getString(R.string.update_name))){
            holder.getView(R.id.tv_right_txt).setVisibility(View.VISIBLE);
            holder.setText(R.id.tv_right_txt,m_wallet_name);
        }else{
            holder.getView(R.id.tv_right_txt).setVisibility(View.INVISIBLE);
        }

        if(myItem.title.equals(getContext().getString(R.string.delete_wallet))){
            holder.setTextColor(R.id.tv_title,
                    ColorUtil.getColor(getContext(),R.color.alarm_highlight_text_color));
        }else{
            holder.setTextColor(R.id.tv_title,
                    ColorUtil.getColor(getContext(),R.color.global_main_text_color));
        }
    }

    public void setWalletName(String m_wallet_name) {
        this.m_wallet_name = m_wallet_name;
    }
}
