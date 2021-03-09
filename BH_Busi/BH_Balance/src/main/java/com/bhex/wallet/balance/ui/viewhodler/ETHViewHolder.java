package com.bhex.wallet.balance.ui.viewhodler;

import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.lib.uikit.widget.layout.XUILinearLayout;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.ImageLoaderUtil;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.tools.utils.ViewUtil;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.balance.ui.activity.ChainTokenActivity;
import com.bhex.wallet.balance.ui.fragment.AddressQRFragment;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.helper.BHWalletHelper;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.SequenceManager;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHChain;
import com.bhex.wallet.common.model.BHToken;

public class ETHViewHolder {

    public ChainTokenActivity mContext;
    public BHBalance mBalance;
    public BHChain mBhChain;
    public LinearLayout viewHolder;


    public AppCompatTextView tv_token_name;
    public AppCompatImageView iv_token_icon;
    //public LinearLayout layout_token_address_make;
    public ETHViewHolder(ChainTokenActivity activity, LinearLayout view, BHChain bhChain){
        viewHolder = view;
        mContext = activity;
        this.mBhChain = bhChain;
    }

    public void initViewContent( BHChain bhChain){
        this.mBhChain = bhChain;
        //Token-Logo
        AppCompatImageView iv_token_icon = viewHolder.findViewById(R.id.iv_token_icon);
        BHToken symbolToken = SymbolCache.getInstance().getBHToken(bhChain.chain.toLowerCase());
        ImageLoaderUtil.loadImageView(mContext,symbolToken!=null?symbolToken.logo:"",iv_token_icon,R.mipmap.ic_default_coin);
        setTokenAddress(bhChain.chain);
    }

    //
    public void setTokenAddress(String symbol) {
        //链地址-label
        AppCompatTextView tv_token_address_label = viewHolder.findViewById(R.id.tv_token_address_label);

        XUILinearLayout layout_token_address = viewHolder.findViewById(R.id.layout_token_address);
        AppCompatTextView tv_token_address = viewHolder.findViewById(R.id.tv_token_address);

        LinearLayout layout_token_address_make = viewHolder.findViewById(R.id.layout_token_address_make);
        AppCompatTextView tv_make_address = viewHolder.findViewById(R.id.tv_make_address);

        //hbc链地址
        if(mBhChain.chain.equalsIgnoreCase(BHConstants.BHT_TOKEN)){
            tv_token_address_label.setText(mContext.getResources().getString(R.string.hbtc_chain_address));
            BHWalletHelper.proccessAddress(tv_token_address,BHUserManager.getInstance().getCurrentBhWallet().address);
            layout_token_address.setOnClickListener(this::showAdddressQRFragment);
            tv_token_address.setTag(BHUserManager.getInstance().getCurrentBhWallet().address);
            layout_token_address.setVisibility(View.VISIBLE);
            layout_token_address_make.setVisibility(View.GONE);
            return;
        }

        //跨链地址
        tv_token_address_label.setText(mContext.getResources().getString(R.string.crosslink_deposit_address));
        tv_token_address_label.setTextColor(ColorUtil.getColor(mContext,R.color.label_address_color));
        //跨链地址-资产
        BHBalance chainBalance = BHBalanceHelper.getBHBalanceFromAccount(mBhChain.chain);
        if(!TextUtils.isEmpty(chainBalance.external_address)){
            BHWalletHelper.proccessAddress(tv_token_address,chainBalance.external_address);
            layout_token_address.setOnClickListener(this::showAdddressQRFragment);
            tv_token_address.setTag(chainBalance.external_address);
            layout_token_address.setVisibility(View.VISIBLE);
            layout_token_address_make.setVisibility(View.GONE);
        }else if(!TextUtils.isEmpty(SequenceManager.getInstance().getAddressStatus())){
            //创建跨链充值地址
            tv_make_address.setText(mContext.getString(R.string.cross_address_generatoring));
            //清空点击事件
            ViewUtil.getListenInfo(layout_token_address_make);
            layout_token_address.setVisibility(View.GONE);
            layout_token_address_make.setVisibility(View.VISIBLE);
        }else{
            tv_make_address.setText(mContext.getString(R.string.create_crosslink_deposit_address));
            layout_token_address_make.setOnClickListener(v->{
                ARouter.getInstance()
                        .build(ARouterConfig.Balance.Balance_cross_address)
                        .withString("chain",mBhChain.chain)
                        .withString("symbol",symbol).navigation();
            });
            layout_token_address.setVisibility(View.GONE);
            layout_token_address_make.setVisibility(View.VISIBLE);
        }
    }
    /*public void setTokenAddress(String symbol) {
        XUILinearLayout layout_token_address = viewHolder.findViewById(R.id.layout_token_address);
        BHToken symbolToken = SymbolCache.getInstance().getBHToken(symbol);
        //hbc链地址
        AppCompatTextView tv_token_address_label = viewHolder.findViewById(R.id.tv_token_address_label);
        tv_token_address = viewHolder.findViewById(R.id.tv_token_address);

        if(mBhChain.chain.equalsIgnoreCase(BHConstants.BHT_TOKEN)){
            tv_token_address_label.setText(mContext.getResources().getString(R.string.hbtc_chain_address));
            BHWalletHelper.proccessAddress(tv_token_address,BHUserManager.getInstance().getCurrentBhWallet().address);
            layout_token_address.setOnClickListener(this::showAdddressQRFragment);
            tv_token_address.setTag(BHUserManager.getInstance().getCurrentBhWallet().address);
            return;
        }

        //跨链地址
        tv_token_address_label.setText(mContext.getResources().getString(R.string.crosslink_deposit_address));

        //跨链地址
        BHBalance chainBalance = BHBalanceHelper.getBHBalanceFromAccount(mBhChain.chain);
        //
        if(!TextUtils.isEmpty(chainBalance.external_address)){
            BHWalletHelper.proccessAddress(tv_token_address,chainBalance.external_address);
            tv_token_address.setTextColor(ColorUtil.getColor(mContext,R.color.global_label_text_color));
            layout_token_address.setOnClickListener(this::showAdddressQRFragment);
            tv_token_address.setTag(chainBalance.external_address);
        }else if(!TextUtils.isEmpty(SequenceManager.getInstance().getAddressStatus())){
            //创建跨链充值地址
            tv_token_address.setText(mContext.getString(R.string.cross_address_generatoring));
            ViewUtil.getListenInfo(layout_token_address);
        }else{
            tv_token_address.setText(mContext.getString(R.string.create_crosslink_deposit_address));
            tv_token_address.setTextColor(ColorUtil.getColor(mContext,R.color.highlight_text_color));
            layout_token_address.setOnClickListener(v->{
                ARouter.getInstance()
                        .build(ARouterConfig.Balance.Balance_cross_address)
                        .withString("chain",mBhChain.chain)
                        .withString("symbol",symbol).navigation();
            });
        }
    }*/

    //显示地址Fragment
    private void showAdddressQRFragment(View view) {
        AppCompatTextView tv_token_address = viewHolder.findViewById(R.id.tv_token_address);
        if(tv_token_address.getTag()==null){
            return;
        }
        AddressQRFragment.showFragment(mContext.getSupportFragmentManager(),
                AddressQRFragment.class.getSimpleName(),
                mBhChain.chain.toLowerCase(),
                tv_token_address.getTag().toString());
    }



}
