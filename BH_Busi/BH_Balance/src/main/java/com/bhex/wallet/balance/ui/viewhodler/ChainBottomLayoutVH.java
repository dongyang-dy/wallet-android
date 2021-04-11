package com.bhex.wallet.balance.ui.viewhodler;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.balance.CoinBottomBtn;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.balance.ui.activity.ChainTokenActivity;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.event.RequestTokenEvent;
import com.bhex.wallet.common.manager.SequenceManager;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHTokenMapping;
import com.google.android.material.button.MaterialButton;

import org.greenrobot.eventbus.EventBus;

/**
 * @author gongdongyang
 * 2020-12-5 23:03:00
 */
public class ChainBottomLayoutVH {

    public ChainTokenActivity activity;

    public View mRootView;

    AppCompatTextView btn_item1;
    AppCompatTextView btn_item2;
    AppCompatTextView btn_item3;
    AppCompatTextView btn_item4;
    /*AppCompatTextView btn_swap;
    AppCompatTextView btn_transfer_in;
    AppCompatTextView btn_transfer_out;*/

    private String mChain;
    private String mSymbol;
    public ChainBottomLayoutVH(ChainTokenActivity activity, View mRootView,String chain,String sybmol) {
        this.activity = activity;
        this.mRootView = mRootView;
        this.mChain = chain;
        this.mSymbol = sybmol;
        btn_item1 = mRootView.findViewById(R.id.btn_item1);
        btn_item2 = mRootView.findViewById(R.id.btn_item2);
        btn_item3 = mRootView.findViewById(R.id.btn_item3);
        btn_item4 = mRootView.findViewById(R.id.btn_item4);
        //btn_transfer_in = mRootView.findViewById(R.id.btn_transfer_in);
        //btn_transfer_out = mRootView.findViewById(R.id.btn_transfer_out);
        //btn_swap.setOnClickListener(this::onSwapAction);
        //btn_transfer_in.setOnClickListener(this::onTransferInAction);
        //btn_transfer_out.setOnClickListener(this::onTransferOutAction);
        btn_item1.setOnClickListener(this::onTransferInAction);
        btn_item2.setOnClickListener(this::onTransferOutAction);
        btn_item3.setOnClickListener(this::onSwapAction);
        btn_item4.setOnClickListener(this::onTradeAction);
    }



    public void initContentView() {
        if(mChain.toLowerCase().equals(BHConstants.BHT_TOKEN)){
            btn_item1.setText(activity.getResources().getString(R.string.transfer_in));
            btn_item2.setText(activity.getResources().getString(R.string.transfer));
            btn_item3.setVisibility(View.GONE);
        }else {
            btn_item1.setText(activity.getResources().getString(R.string.cross_deposit0));
            btn_item2.setText(activity.getResources().getString(R.string.cross_withdraw0));
        }
    }

    //映射
    private void onSwapAction(View view) {
        ARouter.getInstance()
                .build(ARouterConfig.Market_swap_mapping)
                .withString(BHConstants.SYMBOL,mSymbol)
                .navigation();
    }

    //交易
    private void onTradeAction(View view) {
        Postcard postcard =  ARouter.getInstance()
                .build(ARouterConfig.Main.main_mainindex)
                .withString("go_token",mSymbol)
                .withString("go_position",BH_BUSI_TYPE.市场.value);
        LogisticsCenter.completion(postcard);
        Intent intent = new Intent(activity, postcard.getDestination());
        intent.putExtras(postcard.getExtras());
        activity.startActivity(intent);
        EventBus.getDefault().post(new RequestTokenEvent(mSymbol));
        return;
    }

    //收款-充值
    private void onTransferInAction(View view){
        if(mChain.toLowerCase().equals(BHConstants.BHT_TOKEN)){
            ARouter.getInstance().build(ARouterConfig.Balance.Balance_transfer_in)
                    .withString(BHConstants.SYMBOL, mSymbol)
                    .navigation();
            return;
        }

        //判断是否有链外地址
        BHBalance balance = BHBalanceHelper.getBHBalanceFromAccount(mSymbol);
        if(!TextUtils.isEmpty(balance.external_address)){

            ARouter.getInstance().build(ARouterConfig.Balance.Balance_transfer_in_cross)
                    .withString(BHConstants.SYMBOL, mSymbol)
                    .navigation();
        }else if(!TextUtils.isEmpty(SequenceManager.getInstance().getAddressStatus())){
            ToastUtils.showToast(activity.getString(R.string.cross_address_generatoring));
            return;
        }else{
            ARouter.getInstance()
                    .build(ARouterConfig.Balance.Balance_cross_address)
                    .withString(BHConstants.CHAIN,mChain)
                    .withString(BHConstants.SYMBOL,mSymbol).navigation();
        }
    }

    //转账--提币
    private void onTransferOutAction(View view){
        if(mChain.toLowerCase().equals(BHConstants.BHT_TOKEN)){
            ARouter.getInstance().build(ARouterConfig.Balance.Balance_transfer_out)
                    .withString(BHConstants.SYMBOL, mSymbol)
                    .navigation();
        }else {
            ARouter.getInstance().build(ARouterConfig.Balance.Balance_transfer_out_cross)
                    .withString(BHConstants.SYMBOL, mSymbol)
                    .navigation();
        }
    }
    /**
     * //兑换
     *         if(view.getId() == R.id.btn_item4){
     *             ARouter.getInstance().build(ARouterConfig.Market_swap_mapping).withString("symbol",mSymbol).navigation();
     *         }
     */

}
