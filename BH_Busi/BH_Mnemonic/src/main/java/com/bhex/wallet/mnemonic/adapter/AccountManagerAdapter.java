package com.bhex.wallet.mnemonic.adapter;

import android.view.View;

import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.helper.BHWalletHelper;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.mnemonic.R;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author gongdongyang
 * 账户管理
 * 2021-3-2 22:04:30
 */
public class AccountManagerAdapter extends BaseQuickAdapter<BHWallet, BaseViewHolder> {

    public AccountManagerAdapter( @Nullable List<BHWallet> data) {
        super(R.layout.item_account_manager, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, BHWallet bhWallet) {
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
    }
}
