package com.bhex.wallet.balance.ui.viewhodler;

import android.content.Context;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.tools.constants.BHConstants;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.ui.fragment.AccountListFragment;
import com.bhex.wallet.balance.ui.fragment.HbcAddressQrFragment;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.helper.BHWalletHelper;
import com.bhex.wallet.common.manager.BHUserManager;

/**
 * @author gongdongyang
 * 2021-3-3 16:41:16
 */
public class BalanceViewHolder {

    public BaseActivity mContext;
    public View viewHolder;
    //当前钱包
    BHWallet currentWallet;

    //钱包名称
    public AppCompatTextView tv_wallet_name;
    //钱包地址
    public AppCompatTextView tv_wallet_address;
    //资产
    public AppCompatTextView tv_asset;
    //钱包二维码地址展示
    public AppCompatImageView iv_wallet_qr;

    public BalanceViewHolder(BaseActivity mContext, View viewHolder) {
        this.mContext = mContext;
        this.viewHolder = viewHolder;

        currentWallet = BHUserManager.getInstance().getCurrentBhWallet();

        tv_wallet_name = viewHolder.findViewById(R.id.tv_wallet_name);
        tv_wallet_name.setText(currentWallet.name);

        tv_wallet_address = viewHolder.findViewById(R.id.tv_wallet_address);
        BHWalletHelper.proccessAddress(tv_wallet_address,currentWallet.address);

        //资产
        tv_asset = viewHolder.findViewById(R.id.tv_asset);
        //转入
        viewHolder.findViewById(R.id.iv_transfer_in).setOnClickListener(this::transferInAction);
        //转出
        viewHolder.findViewById(R.id.iv_transfer_out).setOnClickListener(this::transferOutAction);
        //转出
        viewHolder.findViewById(R.id.iv_entrust).setOnClickListener(this::entrustAction);
        //钱包二维码地址展示
        iv_wallet_qr = viewHolder.findViewById(R.id.iv_wallet_qr);
        //添加钱包
        viewHolder.findViewById(R.id.iv_add_wallet).setOnClickListener(v->{
            ARouter.getInstance().build(ARouterConfig.Trusteeship.Trusteeship_Add_Index).withInt("flag",1).navigation();
        });


    }

    //转入
    private void transferInAction(View view) {
        ARouter.getInstance().build(ARouterConfig.Balance.Balance_transfer_in)
                .withString("symbol", BHConstants.BHT_TOKEN)
                .withInt("way", BH_BUSI_TYPE.链内转账.getIntValue())
                .navigation();
    }

    //转出
    private void transferOutAction(View view) {
        ARouter.getInstance().build(ARouterConfig.Balance.Balance_transfer_out)
                .withString("symbol", BHConstants.BHT_TOKEN)
                .withInt("way", BH_BUSI_TYPE.链内转账.getIntValue())
                .navigation();
    }

    //转出
    private void entrustAction(View view) {
        ARouter.getInstance().build(ARouterConfig.Validator.Validator_Index)
                .navigation();
    }

}
