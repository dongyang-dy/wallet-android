package com.bhex.wallet.balance.adapter;

import android.widget.RadioButton;

import com.bhex.tools.utils.ColorUtil;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.helper.BHWalletHelper;
import com.bhex.wallet.common.manager.BHUserManager;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author gongdongyang
 * 2021-3-3 22:11:03
 * 账户列表
 */
public class AccountListAdapter extends BaseQuickAdapter<BHWallet, BaseViewHolder> {

    public AccountListAdapter(@Nullable List<BHWallet> data) {
        super(R.layout.item_account_list, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, BHWallet wallet) {
        //账户名称
        holder.setText(R.id.tv_wallet_name,wallet.name);
        //账户地址
        BHWalletHelper.proccessAddress(holder.getView(R.id.tv_wallet_address),wallet.address);
        //是否选中
        RadioButton ck_wallet = holder.getView(R.id.ck_wallet);
        //当前账户
        BHWallet currentWallet = BHUserManager.getInstance().getCurrentBhWallet();
        if(currentWallet.address.equals(wallet.address)){
            ck_wallet.setChecked(true);
            //设置背景色
            holder.itemView.setBackgroundColor(ColorUtil.getColor(getContext(),R.color.bg_wallet_checked));
        }else{
            ck_wallet.setChecked(false);
            //设置背景色
            holder.itemView.setBackgroundColor(ColorUtil.getColor(getContext(),R.color.bg_wallet_uncheck));
        }
    }
}
