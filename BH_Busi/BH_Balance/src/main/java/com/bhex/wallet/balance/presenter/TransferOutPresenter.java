package com.bhex.wallet.balance.presenter;

import android.text.TextUtils;

import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.base.BasePresenter;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.tools.utils.RegexUtil;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.ui.viewhodler.TransferOutVH;
import com.bhex.wallet.common.manager.BHUserManager;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/4/15
 * Time: 11:00
 */
/*public class TransferOutPresenter extends BasePresenter {
    public TransferOutVH mTransferViewHolder;

    public TransferOutPresenter(BaseActivity activity) {
        super(activity);
    }

    public boolean checklinkInnerTransfer(){
        String to_address = mTransferViewHolder.input_to_address.getInputString();
        String transfer_amount = mTransferViewHolder.input_transfer_amount.getText().toString();
        String fee_amount = mTransferViewHolder.input_tx_fee.getInputString();
        String available_amount = String.valueOf(mTransferViewHolder.available_amount);

        String current_address = BHUserManager.getInstance().getCurrentBhWallet().address;

        if(TextUtils.isEmpty(to_address)){
            ToastUtils.showToast(getActivity().getString(R.string.input_receive_address));
            mTransferViewHolder.input_to_address.getEditText().requestFocus();
            return false;
        }

        if(!to_address.toUpperCase().startsWith(BHConstants.BHT_TOKEN.toUpperCase()) || current_address.equalsIgnoreCase(to_address)){
            ToastUtils.showToast(getActivity().getString(R.string.error_transfer_address));
            mTransferViewHolder.input_to_address.getEditText().requestFocus();
            return false;
        }

        if(TextUtils.isEmpty(available_amount)|| Double.valueOf(available_amount)<=0){
            ToastUtils.showToast(getActivity().getString(R.string.not_available_amount));
            return false;
        }

        if(TextUtils.isEmpty(transfer_amount) || !RegexUtil.checkNumeric(transfer_amount)  || Double.valueOf(transfer_amount)<=0){
            ToastUtils.showToast(getActivity().getString(R.string.please_transfer_amount));
            mTransferViewHolder.input_transfer_amount.requestFocus();
            return false;
        }

        if(TextUtils.isEmpty(fee_amount) || !RegexUtil.checkNumeric(fee_amount) || Double.valueOf(fee_amount)<=0){
            ToastUtils.showToast(getActivity().getResources().getString(R.string.please_input_gasfee));
            return false;
        }

        //?????????+????????????--?????????
        if(mTransferViewHolder.tranferToken.symbol.equals(mTransferViewHolder.tranferToken.chain)){
            Double inputAllAmount = NumberUtil.add(transfer_amount,fee_amount);
            if(Double.valueOf(inputAllAmount) > Double.valueOf(available_amount)){
                ToastUtils.showToast(getActivity().getString(R.string.tip_transferout_amount_error_0));
                mTransferViewHolder.input_transfer_amount.requestFocus();
                return false;
            }
        }else{
            if(Double.valueOf(transfer_amount) > Double.valueOf(available_amount) ){
                ToastUtils.showToast(getActivity().getString(R.string.tip_transferout_amount_error));
                mTransferViewHolder.input_transfer_amount.requestFocus();
                return false;
            }
        }



        return true;
    }

    public boolean checkCrossLinkTransfer(){
        String to_address = mTransferViewHolder.input_to_address.getInputString();
        String transfer_amount = mTransferViewHolder.input_transfer_amount.getText().toString().trim();
        String available_amount = String.valueOf(mTransferViewHolder.available_amount);
        String tx_fee_amount = mTransferViewHolder.input_tx_fee.getInputString();
        //
        String input_withdraw_fee = mTransferViewHolder.input_withdraw_fee.getInputString();
        String min_withdraw_fee = mTransferViewHolder.tranferToken.withdrawal_fee;
        //?????????????????????
        String available_withdraw_fee = mTransferViewHolder.withDrawFeeBalance.amount;
        //????????????
        if(TextUtils.isEmpty(to_address)){
            ToastUtils.showToast(getActivity().getResources().getString(R.string.input_withdraw_address));
            mTransferViewHolder.input_to_address.getEditText().requestFocus();
            return false;
        }

        //????????????
        if(TextUtils.isEmpty(transfer_amount) || Double.valueOf(transfer_amount)<=0 ){
            ToastUtils.showToast(getActivity().getResources().getString(R.string.input_withdraw_amount));
            mTransferViewHolder.input_transfer_amount.requestFocus();
            return false;
        }

        //??????????????????????????????
        //??????????????????
        if(TextUtils.isEmpty(available_amount) || Double.valueOf(available_amount)<=0){
            ToastUtils.showToast(getActivity().getString(R.string.not_available_amount));
            return false;
        }

        //???????????????????????????????????????????????????{n}
        if(!RegexUtil.checkNumeric(transfer_amount) || Double.valueOf(transfer_amount)>Double.valueOf(available_amount)){
            String tip_text = String.format(getActivity().getString(R.string.amount_rule),available_amount);
            ToastUtils.showToast(tip_text);
            return false;
        }

        //???????????????
        if(TextUtils.isEmpty(input_withdraw_fee) || Double.valueOf(input_withdraw_fee)<=0){
            ToastUtils.showToast(getActivity().getResources().getString(R.string.please_input_withdraw_fee));
            return false;
        }

        //?????????????????????
        //???????????????????????????????????????
        if(!RegexUtil.checkNumeric(input_withdraw_fee) || Double.valueOf(input_withdraw_fee)>Double.valueOf(available_withdraw_fee)){
            String tip_text = String.format(getActivity().getString(R.string.crosss_fee_high_rule),available_withdraw_fee);
            ToastUtils.showToast(tip_text);
            return false;
        }

        //???????????????????????????????????????
        if(!RegexUtil.checkNumeric(input_withdraw_fee) || Double.valueOf(input_withdraw_fee)<Double.valueOf(min_withdraw_fee)){
            //ToastUtils.showToast( getActivity().getString(R.string.withdraw_fee_notenough));
            String tip_text = String.format(getActivity().getString(R.string.crosss_fee_rule),min_withdraw_fee);
            ToastUtils.showToast(tip_text);
            return false;
        }

        //???????????????
        if(TextUtils.isEmpty(tx_fee_amount) || Double.valueOf(tx_fee_amount)<=0){
            ToastUtils.showToast(getActivity().getResources().getString(R.string.please_input_gasfee));
            return false;
        }

        //??????????????????????????????
        if(mTransferViewHolder.tranferToken.symbol.equals(mTransferViewHolder.tranferToken.chain)){
            Double inputAllAmount = NumberUtil.add(transfer_amount,input_withdraw_fee);
            if(Double.valueOf(inputAllAmount) > Double.valueOf(available_amount)){
                ToastUtils.showToast(getActivity().getString(R.string.tip_withdraw_amount_error_0));
                return false;
            }
        }else{
            if( Double.valueOf(transfer_amount)>Double.valueOf(available_amount)){
                ToastUtils.showToast(getActivity().getString(R.string.error_withdraw_amout_more_available));
                return false;
            }
        }
        return true;
    }
}*/
