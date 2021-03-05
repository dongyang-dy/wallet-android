package com.bhex.wallet.balance.ui.viewhodler;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.balance.CoinBottomBtn;
import com.bhex.tools.constants.BHConstants;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.balance.ui.activity.ChainTokenActivity;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.event.RequestTokenEvent;
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

    MaterialButton btn_item1;
    MaterialButton btn_item2;
    MaterialButton btn_item3;
    MaterialButton btn_item4;

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

        btn_item1.setOnClickListener(this::onBtnItemClick);
        btn_item2.setOnClickListener(this::onBtnItemClick);
        btn_item3.setOnClickListener(this::onBtnItemClick);
        btn_item4.setOnClickListener(this::onBtnItemClick);

    }

    public void initContentView() {
        if(mChain.toLowerCase().equals(BHConstants.BHT_TOKEN)){
            btn_item1.setText(activity.getResources().getString(R.string.transfer_in));
            btn_item2.setText(activity.getResources().getString(R.string.transfer));
            //btn_item3.setVisibility(View.GONE);
        }else {
            btn_item1.setText(activity.getResources().getString(R.string.cross_deposit0));
            btn_item2.setText(activity.getResources().getString(R.string.cross_withdraw0));
        }

        //兑币功能
        /*BHTokenMapping tokenMapping = CacheCenter.getInstance().getTokenMapCache().getTokenMappingOne(mSymbol);
        if(tokenMapping!=null){
            btn_item3.setVisibility(View.VISIBLE);
            btn_item3.setText(activity.getResources().getString(R.string.swap));
        }else{
            btn_item3.setVisibility(View.GONE);
        }*/
    }

    private void onBtnItemClick(View view) {
        if(mChain.toLowerCase().equals(BHConstants.BHT_TOKEN)){
            if (view.getId() == R.id.btn_item1) {//链内转账
                ARouter.getInstance().build(ARouterConfig.Balance.Balance_transfer_in)
                        .withString("symbol", mSymbol)
                        .withInt("way", BH_BUSI_TYPE.链内转账.getIntValue())
                        .navigation();
            } else if (view.getId() == R.id.btn_item2) {
                ARouter.getInstance().build(ARouterConfig.Balance.Balance_transfer_out)
                        .withString("symbol", mSymbol)
                        .withInt("way", BH_BUSI_TYPE.链内转账.getIntValue())
                        .navigation();
            }
        }else{
            if (view.getId() == R.id.btn_item1) {//链外转账
                //判断是否有链外地址
                BHBalance balance = BHBalanceHelper.getBHBalanceFromAccount(mSymbol);
                if(TextUtils.isEmpty(balance.external_address)){
                    ARouter.getInstance()
                            .build(ARouterConfig.Balance.Balance_cross_address)
                            .withString("chain",mChain)
                            .withString("symbol",mSymbol).navigation();
                }else{
                    ARouter.getInstance().build(ARouterConfig.Balance.Balance_transfer_in_cross)
                            .withString("symbol", mSymbol)
                            .withInt("way", BH_BUSI_TYPE.跨链转账.getIntValue())
                            .navigation();
                }
            } else if (view.getId() == R.id.btn_item2) {

                ARouter.getInstance().build(ARouterConfig.Balance.Balance_transfer_out_cross)
                        .withString("symbol", mSymbol)
                        .withInt("way", BH_BUSI_TYPE.跨链转账.getIntValue())
                        .navigation();
            }
        }

        //市场
        if (view.getId() == R.id.btn_item3) {
            Postcard postcard =  ARouter.getInstance()
                                .build(ARouterConfig.Main.main_mainindex)
                                .withString("go_token",mSymbol)
                                .withString("go_position",BH_BUSI_TYPE.市场.value);
            LogisticsCenter.completion(postcard);
            Intent intent = new Intent(activity, postcard.getDestination());
            intent.putExtras(postcard.getExtras());
            activity.startActivity(intent);
            EventBus.getDefault().post(new RequestTokenEvent(mSymbol));
        }

        //兑换
        if(view.getId() == R.id.btn_item4){
            ARouter.getInstance().build(ARouterConfig.Market_swap_mapping).withString("symbol",mSymbol).navigation();
        }

    }

}
