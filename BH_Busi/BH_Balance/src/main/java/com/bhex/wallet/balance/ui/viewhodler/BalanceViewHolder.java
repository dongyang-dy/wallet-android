package com.bhex.wallet.balance.ui.viewhodler;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.balance.ui.fragment.AccountListFragment;
import com.bhex.wallet.balance.ui.fragment.HbcAddressQrFragment;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.helper.BHWalletHelper;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.CurrencyManager;
import com.bhex.wallet.common.ui.activity.BHQrScanActivity;

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
    //钱包地址扫描
    public AppCompatImageView btn_address_scan;
    //资产
    public AppCompatTextView tv_asset;
    //钱包二维码地址展示
    public AppCompatImageView iv_wallet_qr;
    //是否隐藏资产
    public AppCompatImageView iv_open_eye;

    public BalanceViewHolder(BaseActivity mContext, View viewHolder) {
        this.mContext = mContext;
        this.viewHolder = viewHolder;

        currentWallet = BHUserManager.getInstance().getCurrentBhWallet();

        tv_wallet_name = viewHolder.findViewById(R.id.tv_wallet_name);
        tv_wallet_name.setText(currentWallet.name);

        //背景
        GradientDrawable bg_wallet_drawable = ShapeUtils.getRoundRectDrawable(PixelUtils.dp2px(mContext,15),
                ColorUtil.getColor(mContext,R.color.white));
        tv_wallet_name.setBackgroundDrawable(bg_wallet_drawable);

        tv_wallet_address = viewHolder.findViewById(R.id.tv_wallet_address);
        BHWalletHelper.proccessAddress(tv_wallet_address,currentWallet.address);

        //资产
        tv_asset = viewHolder.findViewById(R.id.tv_asset);
        //转入
        viewHolder.findViewById(R.id.tv_transfer_in).setOnClickListener(this::transferInAction);
        //转出
        viewHolder.findViewById(R.id.tv_transfer_out).setOnClickListener(this::transferOutAction);
        //转出
        viewHolder.findViewById(R.id.tv_entrust).setOnClickListener(this::entrustAction);
        //钱包二维码地址展示
        iv_wallet_qr = viewHolder.findViewById(R.id.iv_wallet_qr);
        //添加钱包
        viewHolder.findViewById(R.id.iv_add_wallet).setOnClickListener(v->{
            ARouter.getInstance().build(ARouterConfig.Trusteeship.Trusteeship_Add_Index).withInt(BHConstants.FLAG,1).navigation();
        });
        iv_open_eye = viewHolder.findViewById(R.id.iv_open_eye);

        //

        btn_address_scan =  viewHolder.findViewById(R.id.btn_address_scan);

        //二维码扫描
        btn_address_scan.setOnClickListener(v -> {
            ARouter.getInstance().build(ARouterConfig.Common.commom_scan_qr).navigation(mContext, BHQrScanActivity.REQUEST_CODE);
        });
    }

    //转入
    private void transferInAction(View view) {
        ARouter.getInstance().build(ARouterConfig.Balance.Balance_transfer_in)
                .withString(BHConstants.SYMBOL, BHConstants.BHT_TOKEN)
                .withInt(BHConstants.WAY, BH_BUSI_TYPE.链内转账.getIntValue())
                .navigation();
    }

    //转出
    private void transferOutAction(View view) {
        ARouter.getInstance().build(ARouterConfig.Balance.Balance_transfer_out)
                .withString(BHConstants.SYMBOL, BHConstants.BHT_TOKEN)
                .withInt(BHConstants.WAY, BH_BUSI_TYPE.链内转账.getIntValue())
                .navigation();
    }

    //委托
    private void entrustAction(View view) {
        ARouter.getInstance().build(ARouterConfig.Validator.Validator_Index)
                .navigation();
    }


    public void updateWalletName(){
        currentWallet = BHUserManager.getInstance().getCurrentBhWallet();
        tv_wallet_name.setText(currentWallet.name);
    }

    public void updateAsset(boolean isOpenEye) {
        double allTokenAssets = BHBalanceHelper.calculateAllTokenPrice(mContext,BHUserManager.getInstance().getAccountInfo());
        String allTokenAssetsText = CurrencyManager.getInstance().getCurrencyDecription(mContext,allTokenAssets);
        if(isOpenEye){
            iv_open_eye.setImageResource(R.mipmap.ic_asset_open);
            tv_asset.setText(allTokenAssetsText);
        }else{
            iv_open_eye.setImageResource(R.mipmap.ic_asset_close);
            tv_asset.setText("***");
        }
        //BHBalanceHelper.setTextFristSamll(mContext,tv_asset,allTokenAssetsText);
    }

    /*public void updateAssetFailStatus(boolean isOpenEye){
        if(isOpenEye){
            iv_open_eye.setImageResource(R.mipmap.ic_asset_open);
            tv_asset.setText(mContext.getString(R.string.place_holder));
        }else{
            iv_open_eye.setImageResource(R.mipmap.ic_asset_close);
            tv_asset.setText("***");
        }
    }*/
}
