package com.bhex.wallet.balance.helper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.support.hsf.HSFJSONUtils;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.network.utils.JsonUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.model.TxOrderItem;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.enums.TRANSCATION_BUSI_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.tx.TransactionOrder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/4/10
 * Time: 17:25
 */
public class TransactionHelper {
    /**
     * 获取交易类型
     */
    public static String getTranscationType(Context context, TransactionOrder txo){
        TransactionOrder.ActivitiesBean bean = txo.activities.get(0);
        StringBuffer type_label = new StringBuffer(
                TRANSCATION_BUSI_TYPE.getValue(bean.getType())
        );

        if(bean.getType().equals(TRANSCATION_BUSI_TYPE.转账.getType())){
            TransactionOrder.ActivitiesBean.ValueBean transferBean = JsonUtils.fromJson(bean.value.toString(),
                    TransactionOrder.ActivitiesBean.ValueBean.class);
            String address = BHUserManager.getInstance().getCurrentBhWallet().address;
            if(transferBean.getTo_address().equals(address)){
                type_label.delete(0,type_label.length()).append(context.getResources().getString(R.string.make_collection));
            }
        }
        if(txo.activities.size()>1){
            for(TransactionOrder.ActivitiesBean bean1:txo.activities){
                String type_str = TRANSCATION_BUSI_TYPE.getValue(bean1.getType());
                if(!type_label.toString().contains(type_str)){
                    type_label.append("+").append(type_str);
                }
            }

        }
        return type_label.toString();
    }

    public static String getTranscationType(Context context, TxOrderItem txo){
        TxOrderItem.ActivitiesBean bean = txo.getActivities().get(0);
        StringBuffer type_label = new StringBuffer(TRANSCATION_BUSI_TYPE.getValue(bean.getType()));
        if(txo.getActivities().size()>1){
            String type = bean.getType();
            for(TxOrderItem.ActivitiesBean bean1 : txo.getActivities()){
                String type_str = TRANSCATION_BUSI_TYPE.getValue(bean1.getType());
                if(!type_label.toString().contains(type_str)){
                    type_label.append("+").append(type_str);
                }
            }

        }
        return type_label.toString();
    }


    public static void setTranscationStatus(Context context, boolean status, AppCompatTextView tv_status,TransactionOrder txo){
        String statusLabel = "";
        TransactionOrder.ActivitiesBean bean = txo.activities.get(0);
        if(bean.type.equalsIgnoreCase(TRANSCATION_BUSI_TYPE.跨链充值.getType())
                || bean.type.equalsIgnoreCase(TRANSCATION_BUSI_TYPE.跨链提币.getType()) ){
            if(txo.ibc_status==1||txo.ibc_status==2){
                statusLabel = context.getResources().getString(R.string.processing);
                tv_status.setTextColor(ContextCompat.getColor(context,R.color.global_main_gray_color));
                //tv_status.setBackgroundColor(ContextCompat.getColor(context,R.color.color_20_orange));

            }else if(txo.ibc_status==3){
                statusLabel = context.getResources().getString(R.string.fail);
                tv_status.setTextColor(ContextCompat.getColor(context,R.color.color_red));
                //tv_status.setBackgroundColor(ContextCompat.getColor(context,R.color.color_20_red));
            }else if(txo.ibc_status==4){
                statusLabel = context.getResources().getString(R.string.success);
                tv_status.setTextColor(ContextCompat.getColor(context,R.color.color_green));
                //tv_status.setBackgroundColor(ContextCompat.getColor(context,R.color.color_20_green));
            }
        }else{
            if(status){
                statusLabel = context.getResources().getString(R.string.success);
                tv_status.setTextColor(ContextCompat.getColor(context,R.color.color_green));
                //tv_status.setBackgroundColor(ContextCompat.getColor(context,R.color.color_20_green));
            }else{
                statusLabel = context.getResources().getString(R.string.fail);
                tv_status.setTextColor(ContextCompat.getColor(context,R.color.color_red));
                //tv_status.setBackgroundColor(ContextCompat.getColor(context,R.color.color_20_red));
            }
        }
        tv_status.setText(statusLabel);
    }

    /**
     * 交易详情用
     * @param context
     * @param tv_status
     */
    public static void setTranscationStatusExt(Context context, TransactionOrder txo, AppCompatTextView tv_status){
        String statusLabel = "";
        TransactionOrder.ActivitiesBean bean = txo.activities.get(0);
        if(bean.type.equalsIgnoreCase(TRANSCATION_BUSI_TYPE.跨链充值.getType())
                || bean.type.equalsIgnoreCase(TRANSCATION_BUSI_TYPE.跨链提币.getType()) ){
            if(txo.ibc_status==1||txo.ibc_status==2){
                statusLabel = context.getResources().getString(R.string.processing);
                tv_status.setTextColor(ContextCompat.getColor(context,R.color.global_main_gray_color));
            }else if(txo.ibc_status==3){
                statusLabel = context.getResources().getString(R.string.fail);
                tv_status.setTextColor(ContextCompat.getColor(context,R.color.color_red));
            }else if(txo.ibc_status==4){
                statusLabel = context.getResources().getString(R.string.success);
                tv_status.setTextColor(ContextCompat.getColor(context,R.color.color_green));
            }
        }else{
            if(txo.success){
                statusLabel = context.getResources().getString(R.string.success);
                tv_status.setTextColor(ContextCompat.getColor(context,R.color.color_green));
            }else{
                statusLabel = context.getResources().getString(R.string.fail);
                tv_status.setTextColor(ContextCompat.getColor(context,R.color.color_red));
            }
        }

        tv_status.setText(statusLabel);
    }

    public static TxOrderItem getTxOrderItem(TransactionOrder txo){
        String jsonString = JsonUtils.toJson(txo);
        TxOrderItem txOrderItem = JsonUtils.fromJson(jsonString,TxOrderItem.class);
        txOrderItem.value = txo.activities.get(0).getValue().toString();
        txOrderItem.activities = new ArrayList<>();
        for(TransactionOrder.ActivitiesBean activitiesBean:txo.activities){
            TxOrderItem.ActivitiesBean activitiesBean1 = new TxOrderItem.ActivitiesBean();
            activitiesBean1.valueIem = activitiesBean.getValue().toString();
            activitiesBean1.type = activitiesBean.getType();
            txOrderItem.activities.add(activitiesBean1);
        }
        return txOrderItem;
    }

    /**
     *
     * @param txOrderItem
     */
    public static void gotoTranscationDetail(TxOrderItem txOrderItem,String mSymbol){
        ARouter.getInstance().build(ARouterConfig.Balance.Balance_transcation_view)
                //.withObject("txo",txOrderItem)
                .withString("transactionId",txOrderItem.hash)
                .withString(BHConstants.SYMBOL,mSymbol)
                .navigation();
    }


    public static void displayTranscationAmount(AppCompatTextView tv,String symbol,TransactionOrder txo){
        tv.setVisibility(View.GONE);
        tv.setText("");
        if(ToolUtils.checkListIsEmpty(txo.balance_flows)){
            //tv.setVisibility(View.GONE);
            return;
        }

        String currentAddress = BHUserManager.getInstance().getCurrentBhWallet().address;
        for (TransactionOrder.BalanceFlowsBean bean:txo.balance_flows) {
            if(!bean.address.equalsIgnoreCase(currentAddress)){
                continue;
            }
            if(!bean.symbol.equalsIgnoreCase(symbol)){
                continue;
            }
            String fmt_amount = NumberUtil.dispalyForUsertokenAmount4Level(bean.amount);
            //
            BHToken bhToken = SymbolCache.getInstance().getBHToken(bean.symbol);
            tv.setText(fmt_amount+" "+bhToken.name.toUpperCase());
            tv.setVisibility(View.VISIBLE);
        }
    }

    public static void setRealAmount(AppCompatTextView tv,String amount,String symbol,String flag){
        BHToken token = SymbolCache.getInstance().getBHToken(symbol);
        if(token!=null){
            double real_amount = NumberUtil.divide(amount,Math.pow(10,token.decimals)+"",token.decimals);
            String tv_amount = NumberUtil.dispalyForUsertokenAmount4Level(String.valueOf(real_amount))+symbol.toUpperCase();
            tv.setText(flag+tv_amount);
        }
    }


    public static void displayTranscationFromTo(Context context,AppCompatTextView tv_from,
                                                AppCompatTextView tv_to,String txType,String json){

        if(txType.equalsIgnoreCase(TRANSCATION_BUSI_TYPE.转账.getType())){
            TransactionOrder.ActivitiesBean.ValueBean valueBean
                    = JsonUtils.fromJson(json,TransactionOrder.ActivitiesBean.ValueBean.class);

            TransactionOrder.ActivitiesBean.ValueBean.AmountBean bean1 = valueBean.getAmount().get(0);

            tv_from.setText(valueBean.getFrom_address());
            tv_to.setText(valueBean.getTo_address());

        }else if(txType.equalsIgnoreCase(TRANSCATION_BUSI_TYPE.跨链地址生成.getType())){
            TransactionOrder.ActivitiesBean.AddressGenBean addressGenBean =
                    JsonUtils.fromJson(json,TransactionOrder.ActivitiesBean.AddressGenBean.class);

            tv_from.setText(addressGenBean.from);
            tv_to.setText(addressGenBean.to);
        }else if(txType.equalsIgnoreCase(TRANSCATION_BUSI_TYPE.跨链提币.getType())){

            TransactionOrder.ActivitiesBean.WithdrawalBean withdrawalBean =
                    JsonUtils.fromJson(json,TransactionOrder.ActivitiesBean.WithdrawalBean.class);
            tv_from.setText(withdrawalBean.from_cu);
            tv_to.setText(withdrawalBean.to_multi_sign_address);
        }else if(txType.equalsIgnoreCase(TRANSCATION_BUSI_TYPE.跨链充值.getType())){
            TransactionOrder.ActivitiesBean.DepositBean depositBean =
                    JsonUtils.fromJson(json,TransactionOrder.ActivitiesBean.DepositBean.class);

            tv_from.setText(depositBean.from_cu);
            tv_to.setText(depositBean.to_adddress);
        }else if(txType.equalsIgnoreCase(TRANSCATION_BUSI_TYPE.委托.getType())){
            TransactionOrder.ActivitiesBean.DelegateBean delegateBean =
                    JsonUtils.fromJson(json,TransactionOrder.ActivitiesBean.DelegateBean.class);

            tv_from.setText(delegateBean.delegator_address);

            tv_to.setText(delegateBean.validator_address);
        }else if(txType.equalsIgnoreCase(TRANSCATION_BUSI_TYPE.解委托.getType())){

        }
    }


    public  static boolean transactionIsProccess(String txType){
        if(TRANSCATION_BUSI_TYPE.转账.getType().equalsIgnoreCase(txType)
                || TRANSCATION_BUSI_TYPE.委托.getType().equalsIgnoreCase(txType)
                || TRANSCATION_BUSI_TYPE.跨链地址生成.getType().equalsIgnoreCase(txType)
                || TRANSCATION_BUSI_TYPE.提取收益.getType().equalsIgnoreCase(txType)
                || TRANSCATION_BUSI_TYPE.跨链充值.getType().equalsIgnoreCase(txType)
                || TRANSCATION_BUSI_TYPE.跨链提币.getType().equalsIgnoreCase(txType)
                || TRANSCATION_BUSI_TYPE.复投分红.getType().equalsIgnoreCase(txType)
                || TRANSCATION_BUSI_TYPE.解委托.getType().equalsIgnoreCase(txType)){
            return true;
        }
        return false;
    }
}
