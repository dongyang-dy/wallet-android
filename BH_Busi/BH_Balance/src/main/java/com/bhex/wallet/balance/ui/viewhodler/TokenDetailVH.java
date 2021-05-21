package com.bhex.wallet.balance.ui.viewhodler;

import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.ImageLoaderUtil;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.R2;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHToken;

import butterknife.BindView;

/**
 * @Token详情
 */
public class TokenDetailVH {

    public BaseActivity activity;
    public View layoutView;

    public AppCompatTextView tv_coin_amount;
    public AppCompatTextView tv_coin_currency;
    public AppCompatImageView iv_coin_ic;
    public ConstraintLayout layout_hbc_view;

    public AppCompatTextView tv_available_value;
    public AppCompatTextView tv_entrust_value;
    public AppCompatTextView tv_redemption_value;
    public AppCompatTextView tv_income_value;

    //充值
    public AppCompatTextView btn_deposit;
    //提现
    public AppCompatTextView btn_withdraw;
    //交易
    public AppCompatTextView btn_trade;

    private String mSybmol;
    public TokenDetailVH(BaseActivity activity,String symbol) {
        this.activity = activity;
        this.mSybmol = symbol;
        tv_coin_amount = activity.findViewById(R.id.tv_coin_amount);
        tv_coin_currency = activity.findViewById(R.id.tv_coin_currency);
        iv_coin_ic = activity.findViewById(R.id.iv_coin_ic);
        layout_hbc_view = activity.findViewById(R.id.layout_hbc_view);
        tv_available_value = activity.findViewById(R.id.tv_available_value);
        tv_entrust_value = activity.findViewById(R.id.tv_entrust_value);
        tv_redemption_value = activity.findViewById(R.id.tv_redemption_value);
        tv_income_value = activity.findViewById(R.id.tv_income_value);
        btn_deposit = activity.findViewById(R.id.btn_deposit);
        btn_withdraw = activity.findViewById(R.id.btn_withdraw);
        btn_trade = activity.findViewById(R.id.btn_trade);

        //
        GradientDrawable btn_drawable =  ShapeUtils.getRoundRectDrawable(PixelUtils.dp2px(activity,100),
                ColorUtil.getColor(activity,R.color.global_button_color_bg));
        btn_deposit.setBackgroundDrawable(btn_drawable);

        btn_withdraw.setBackgroundDrawable(btn_drawable);

        GradientDrawable btn_stroke_drawable = ShapeUtils.getRoundRectStrokeDrawable(PixelUtils.dp2px(activity,100),
                ColorUtil.getColor(activity,R.color.white),ColorUtil.getColor(activity,R.color.global_button_color_bg),
                PixelUtils.dp2px(activity,1));
        btn_trade.setBackgroundDrawable(btn_stroke_drawable);

        //充值
        btn_deposit.setOnClickListener(this::depositAction);

        //提现
        btn_withdraw.setOnClickListener(this::withDrawAction);

    }

    public void updateLayout(String symbol){

        BHToken symbolToken  = CacheCenter.getInstance().getSymbolCache().getBHToken(symbol);

        if(BHConstants.BHT_TOKEN.equalsIgnoreCase(symbolToken.name)){
            layout_hbc_view.setVisibility(View.VISIBLE);
        }else{
            layout_hbc_view.setVisibility(View.GONE);
        }

        if(!TextUtils.isEmpty(symbolToken.logo)){
            iv_coin_ic.setAlpha(0.1f);
            ImageLoaderUtil.loadImageView(activity,symbolToken.logo,iv_coin_ic,R.mipmap.ic_default_coin);
        }
    }

    public void updateTokenAsset(String symbol){
        BHToken symbolToken  = CacheCenter.getInstance().getSymbolCache().getBHToken(symbol);
        BHBalance symbolBalance = BHBalanceHelper.getBHBalanceFromAccount(symbol);

        if(BHUserManager.getInstance().getAccountInfo()==null){
            return;
        }
        //计算持有资产
        String []res = BHBalanceHelper.getAmountToCurrencyValue(activity,symbolBalance.amount,symbolToken.symbol,false);
        tv_coin_amount.setText(res[0]);
        //对应法币实际值
        tv_coin_currency.setText(res[1]);
        //链上资产

        //初始化话HBC资产
        if(BHUserManager.getInstance().getAccountInfo()==null){
            return;
        }
        String available_value = NumberUtil.dispalyForUsertokenAmount4Level(BHUserManager.getInstance().getAccountInfo() .available );
        tv_available_value.setText(available_value);

        //委托中
        String bonded_value = NumberUtil.dispalyForUsertokenAmount4Level(BHUserManager.getInstance().getAccountInfo().bonded);
        tv_entrust_value.setText(bonded_value);
        //赎回中
        String unbonding_value = NumberUtil.dispalyForUsertokenAmount4Level(BHUserManager.getInstance().getAccountInfo().unbonding);
        tv_redemption_value.setText(unbonding_value);

        //已收益
        String claimed_reward_value = NumberUtil.dispalyForUsertokenAmount4Level(BHUserManager.getInstance().getAccountInfo().claimed_reward);
        tv_income_value.setText(claimed_reward_value);
    }

    //充值
    private void depositAction(View view) {
        ARouter.getInstance().build(ARouterConfig.Balance.Balance_transfer_in_cross)
                .withString(BHConstants.SYMBOL,mSybmol)
                .navigation();
    }

    //提现
    private void withDrawAction(View view) {
        ARouter.getInstance().build(ARouterConfig.Balance.Balance_transfer_out_cross)
                .withString(BHConstants.SYMBOL,mSybmol)
                .navigation();
    }
}
