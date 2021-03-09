package com.bhex.wallet.bh_main.proposals.presenter;

import android.text.TextUtils;

import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.base.BasePresenter;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.model.BHToken;

public class CreateProposalPresenter extends BasePresenter {

    public CreateProposalPresenter(BaseActivity activity) {
        super(activity);
    }




    public String getAmountForUser(String amount, String frozen_amount, String symbol) {
        SymbolCache symbolCache = CacheCenter.getInstance().getSymbolCache();
        BHToken bhToken = symbolCache.getBHToken(symbol);
        int decimals = bhToken!=null?bhToken.decimals:2;
        decimals = 0;
        String tmp = NumberUtil.sub(amount,frozen_amount);
        double displayAmount = NumberUtil.divide(tmp, Math.pow(10,decimals)+"");

        //LogUtils.d("BHBalanceHelper==>:","displayAmount==="+displayAmount);
        //DecimalFormat format = new DecimalFormat();
        return NumberUtil.dispalyForUsertokenAmount4Level(String.valueOf(displayAmount));
    }

    public boolean checkCreateProposal(String title, String desc, String available_amount, String amount, String fee_amount) {

        if (TextUtils.isEmpty(title)) {
            ToastUtils.showToast(getActivity().getString(R.string.check_proposal_title));
            return false;
        }
        if (TextUtils.isEmpty(desc)) {
            ToastUtils.showToast(getActivity().getString(R.string.check_proposal_desc));
            return false;
        }


        if (TextUtils.isEmpty(amount) || Double.valueOf(amount) <= 0) {
            ToastUtils.showToast(getActivity().getString(R.string.check_pledge_amount));
            return false;
        }

        if (TextUtils.isEmpty(fee_amount) || Double.valueOf(fee_amount) <= 0) {
            ToastUtils.showToast(getActivity().getString(R.string.check_empty_fee));
            return false;
        }

        if (NumberUtil.add(amount, fee_amount) > Double.valueOf(available_amount)) {
            ToastUtils.showToast(getActivity().getString(R.string.check_pledge_amount_and_fee_max));
            return false;
        }

        return true;
    }
}

