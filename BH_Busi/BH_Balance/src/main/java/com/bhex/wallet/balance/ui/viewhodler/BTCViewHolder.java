package com.bhex.wallet.balance.ui.viewhodler;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.ui.fragment.AddressQRFragment;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.helper.BHWalletHelper;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.BHBalance;

/**
 * @author gongdongyang
 * 2020-10-15 19:24:57
 */
public class BTCViewHolder {

    private static  BTCViewHolder  _instance = new BTCViewHolder();

    public RelativeLayout viewHolder;
    public RelativeLayout layout_btc_address;
    public AppCompatTextView tv_token_name;
    public AppCompatTextView tv_hbc_addr_label;
    public AppCompatTextView tv_hbc_address;

    public AppCompatTextView tv_token_address;
    public AppCompatTextView btn_make_address;

    public BaseActivity mContext;
    BHBalance mBalance;

    public static BTCViewHolder getInstance(){
        return _instance;
    }

    public BTCViewHolder initView(BaseActivity activity, RelativeLayout view, BHBalance balance){
        viewHolder = view;
        mContext = activity;
        layout_btc_address = view.findViewById(R.id.layout_btc_address);
        tv_token_name = view.findViewById(R.id.tv_token_name);
        tv_hbc_address = view.findViewById(R.id.tv_hbc_address);
        tv_hbc_addr_label = view.findViewById(R.id.tv_hbc_addr_label);

        tv_token_address = view.findViewById(R.id.tv_token_address);
        btn_make_address = view.findViewById(R.id.btn_make_address);

        this.mBalance = balance;

        view.findViewById(R.id.iv_hbc_qr).setOnClickListener(v -> {
            showQRDialog();
        });

        view.findViewById(R.id.iv_hbc_paste).setOnClickListener(v->{
            ToolUtils.copyText(tv_hbc_address.getTag().toString(),mContext);
            ToastUtils.showToast(mContext.getString(R.string.copyed));
        });

        AppCompatImageView imageView = view.findViewById(R.id.iv_hbc_paste);
        ColorUtil.setIconColor(imageView,51,117,224,255);

        view.findViewById(R.id.layout_index_0).bringToFront();
        /*CardView cardView = view.findViewById(R.id.card_view);
        CardUtil.setCardShadowColor(cardView, mContext.getResources().getColor(R.color.blue_1F3375E0), mContext.getResources().getColor(R.color.blue_1F3375E0));*/

        view.findViewById(R.id.iv_token_qr).setOnClickListener(v -> {
            showBTCQRDialog();
        });

        view.findViewById(R.id.iv_token_paste).setOnClickListener(v->{
            ToolUtils.copyText(tv_token_address.getTag().toString(),mContext);
            ToastUtils.showToast(mContext.getString(R.string.copyed));
        });

        view.findViewById(R.id.btn_make_address).setOnClickListener(v->{
            makeAddressAction();
        });


        return _instance;
    }

    public void setTokenAddress(BHBalance balance) {
        this.mBalance = balance;
        if(mBalance.chain.equalsIgnoreCase(BHConstants.BHT_TOKEN)){
            viewHolder.setVisibility(View.GONE);
        }
        tv_token_name.setText(mBalance.symbol.toUpperCase());
        tv_hbc_address.setTag(BHUserManager.getInstance().getCurrentBhWallet().address);
        BHWalletHelper.proccessAddress(tv_hbc_address,BHUserManager.getInstance().getCurrentBhWallet().address);
        tv_token_address.setTag(mBalance.external_address);
        BHWalletHelper.proccessAddress(tv_token_address,mBalance.external_address);
        if(TextUtils.isEmpty(mBalance.external_address)){
            btn_make_address.setVisibility(View.VISIBLE);
            layout_btc_address.setVisibility(View.GONE);
        }else{
            btn_make_address.setVisibility(View.GONE);
            layout_btc_address.setVisibility(View.VISIBLE);
        }
    }

    private void showQRDialog() {
        AddressQRFragment.showFragment(mContext.getSupportFragmentManager(),
                AddressQRFragment.class.getSimpleName(),
                mBalance.symbol.toLowerCase(),
                tv_hbc_address.getTag().toString());
    }

    private void showBTCQRDialog(){
        AddressQRFragment.showFragment(mContext.getSupportFragmentManager(),
                AddressQRFragment.class.getSimpleName(),
                mBalance.symbol.toUpperCase(),
                tv_token_address.getTag().toString());
    }

    private void makeAddressAction() {
        //BHBalance bthBalance = BHBalanceHelper.getBHBalanceFromAccount(BHConstants.BHT_TOKEN);

        Postcard postcard = ARouter.getInstance().build(ARouterConfig.Balance.Balance_cross_address)
                .withString(BHConstants.SYMBOL, mBalance.symbol)
                //.withObject("bhtBalance",bthBalance)
                .withInt("way", BH_BUSI_TYPE.跨链转账.getIntValue());
        LogisticsCenter.completion(postcard);
        Intent intent = new Intent(mContext, postcard.getDestination());
        intent.putExtras(postcard.getExtras());
        mContext.startActivityForResult(intent,100);
    }
}
