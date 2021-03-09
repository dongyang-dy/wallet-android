package com.bhex.wallet.bh_main.validator.presenter;

import android.text.TextUtils;

import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.base.BasePresenter;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.tools.utils.RegexUtil;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.model.BHToken;


public class DoEntrustPresenter extends BasePresenter {

    public DoEntrustPresenter(BaseActivity activity) {
        super(activity);
    }

    public boolean checkDoEntrust(String transfer_amount,
                                          String available_amount,String fee_amount){

        if(TextUtils.isEmpty(transfer_amount) || Double.valueOf(transfer_amount)<=0){
            ToastUtils.showToast(getActivity().getString(R.string.check_do_entrust_amount));
            return false;
        }


        if(TextUtils.isEmpty(available_amount) || !RegexUtil.checkNumeric(available_amount)){
            ToastUtils.showToast(getActivity().getString(R.string.not_available_amount));
            return false;
        }
        //委托数量不能大于可用
        if(!TextUtils.isEmpty(transfer_amount) && Double.valueOf(transfer_amount)>Double.valueOf(available_amount)){
            ToastUtils.showToast(getActivity().getString(R.string.entrust_amount_more_avilable));
            return false;
        }

        if(TextUtils.isEmpty(fee_amount) || Double.valueOf(fee_amount)<=0){
            ToastUtils.showToast(getActivity().getString(R.string.check_empty_fee));
            return false;
        }

        if(NumberUtil.add(transfer_amount,fee_amount) >Double.valueOf(available_amount)){
            ToastUtils.showToast(getActivity().getString(R.string.check_do_entrust_amount_and_fee_max));
            return false;
        }

        return true;
    }

    public boolean checkReliveEntrust(String transfer_amount, String available_amount,
                                  String relive_amount,String fee_amount){
        try{
            if(TextUtils.isEmpty(transfer_amount) || Double.valueOf(transfer_amount)<=0){
                ToastUtils.showToast(getActivity().getString(R.string.check_relive_entrust_amount));
                return false;
            }

            if(TextUtils.isEmpty(relive_amount)){
                ToastUtils.showToast(getActivity().getString(R.string.not_avilable_relieve)+BHConstants.BHT_TOKEN.toUpperCase());
                return false;
            }

            if(Double.valueOf(transfer_amount) >Double.valueOf(relive_amount)){
                ToastUtils.showToast(getActivity().getString(R.string.check_relive_entrust_amount_max));
                return false;
            }

            if(TextUtils.isEmpty(fee_amount) || Double.valueOf(fee_amount)<=0){
                ToastUtils.showToast(getActivity().getString(R.string.check_empty_fee));
                return false;
            }


            if(available_amount==null || !RegexUtil.checkNumeric(available_amount)){
                ToastUtils.showToast(getActivity().getString(R.string.fee_notenough));
                return false;
            }
            if(Double.valueOf(fee_amount) > Double.valueOf(available_amount)){
                ToastUtils.showToast(getActivity().getString(R.string.fee_notenough));
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }


        return true;
    }


    public String getAmountForUser(String amount, String frozen_amount, String symbol) {
        BHToken bhToken = CacheCenter.getInstance().getSymbolCache().getBHToken(symbol);
        int decimals = bhToken!=null?bhToken.decimals:2;
        decimals = 0;
        String tmp = NumberUtil.sub(amount,frozen_amount);
        double displayAmount = NumberUtil.divide(tmp, Math.pow(10,decimals)+"");
        return NumberUtil.dispalyForUsertokenAmount4Level(String.valueOf(displayAmount));
    }

}

