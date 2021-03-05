package com.bhex.wallet.balance.ui.viewhodler;

import android.text.InputType;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.tools.utils.RegexUtil;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.ui.activity.BHQrScanActivity;
import com.google.android.material.button.MaterialButton;

/**
 * @author gdy
 * 跨链提币
 */
public class TransferOutCrossVH {
    private BaseActivity m_activity;
    private View mRootView;

    //提币地址
    public AppCompatEditText inp_drawwith_address;

    //扫描按钮
    public AppCompatImageView btn_scan_address;
    //提币Token
    public AppCompatTextView tv_withdraw_token;
    //提币数量
    public AppCompatEditText inp_withdraw_amount;
    //全部
    public AppCompatTextView btn_all;
    //可用
    public AppCompatTextView tv_available_amount;
    //跨链手续费
    public AppCompatTextView tv_withdraw_fee;

    public AppCompatTextView tv_withdraw_fee_token;
    //手续费
    public AppCompatTextView tv_fee;
    public AppCompatTextView tv_fee_token;

    //选择币种
    public View layout_select_token;

    //提币按钮
    public MaterialButton btn_drawwith_coin;

    //转账token
    public BHToken tranferToken;
    //提币手续费token
    public BHToken withDrawFeeToken;
    //转账资产
    public BHBalance tranferBalance;

    //可以提币或转账数量
    public double available_amount;

    public TransferOutCrossVH(BaseActivity m_activity, View mRootView) {
        this.m_activity = m_activity;
        this.mRootView = mRootView;
        //初始化布局
        inp_drawwith_address = mRootView.findViewById(R.id.inp_drawwith_address);
        btn_scan_address = mRootView.findViewById(R.id.btn_scan_address);
        tv_withdraw_token = mRootView.findViewById(R.id.tv_withdraw_token);
        inp_withdraw_amount = mRootView.findViewById(R.id.inp_withdraw_amount);
        btn_all = mRootView.findViewById(R.id.btn_all);
        tv_available_amount = mRootView.findViewById(R.id.tv_available_amount);
        tv_withdraw_fee = mRootView.findViewById(R.id.tv_withdraw_fee);
        tv_withdraw_fee_token = mRootView.findViewById(R.id.tv_withdraw_fee_token);

        tv_fee = mRootView.findViewById(R.id.tv_fee);
        tv_fee_token = mRootView.findViewById(R.id.tv_fee_token);

        layout_select_token = mRootView.findViewById(R.id.layout_select_token);
        btn_drawwith_coin  = mRootView.findViewById(R.id.btn_drawwith_coin);
        //二维码扫描
        btn_scan_address.setOnClickListener(v -> {
            ARouter.getInstance().build(ARouterConfig.Common.commom_scan_qr).navigation(m_activity, BHQrScanActivity.REQUEST_CODE);
        });

        btn_all.setOnClickListener(this::withdrawAllAction);

        //设置输入框键盘类型
        inp_withdraw_amount.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    //更新token信息
    public void updateTokenInfo(String symbol){
        //

        tranferToken = CacheCenter.getInstance().getSymbolCache().getBHToken(symbol);
        //设置提币Token
        tv_withdraw_token.setText(tranferToken.name.toUpperCase());

        tranferBalance = BHBalanceHelper.getBHBalanceFromAccount(tranferToken.symbol);

        //设置可用提币数量
        String available_amount_str =  BHBalanceHelper.getAmountForUser(m_activity,tranferBalance.amount,"0",tranferBalance.symbol);
        available_amount = Double.valueOf(available_amount_str);
        tv_available_amount.setText(m_activity.getString(R.string.available)+" "+available_amount_str+" "+tranferToken.name.toUpperCase());

        //跨链手续费
        withDrawFeeToken = CacheCenter.getInstance().getSymbolCache().getBHToken(tranferToken.chain);
        tv_withdraw_fee.setText(tranferToken!=null?tranferToken.withdrawal_fee:"");

        tv_withdraw_fee_token.setText(tranferToken.name.toUpperCase());
        //手续费
        tv_fee.setText(BHUserManager.getInstance().getDefaultGasFee().displayFee);
        tv_fee_token.setText(BHConstants.BHT_TOKEN.toUpperCase());


    }

    //全部提币
    private void withdrawAllAction(View view) {

        String v_input_withdraw_fee = tv_withdraw_fee.getText().toString();
        //主链币
        if(tranferToken.name.equalsIgnoreCase(tranferToken.chain)){
            String all_count = NumberUtil.sub(String.valueOf(available_amount),v_input_withdraw_fee);
            all_count = Double.valueOf(all_count)<0?"0":all_count;
            inp_withdraw_amount.setText(all_count);
        }else{
            inp_withdraw_amount.setText(NumberUtil.toPlainString(available_amount));
        }
        inp_withdraw_amount.setSelection(inp_withdraw_amount.getText().length());
        inp_withdraw_amount.requestFocus();
    }

    //提币验证
    public boolean verifyWithDrawAction() {
        //提币地址
        String v_withdraw_address = inp_drawwith_address.getText().toString().trim();
        if(TextUtils.isEmpty(v_withdraw_address)){
            ToastUtils.showToast(m_activity.getResources().getString(R.string.input_withdraw_address));
            inp_drawwith_address.requestFocus();
            return false;
        }

        //提币数量
        String v_withdraw_amount = inp_withdraw_amount.getText().toString().trim();
        if(TextUtils.isEmpty(v_withdraw_amount) || !RegexUtil.checkNumeric(v_withdraw_amount)|| Double.valueOf(v_withdraw_amount)<=0 ){
            ToastUtils.showToast(m_activity.getResources().getString(R.string.input_withdraw_amount));
            inp_withdraw_amount.requestFocus();
            return false;
        }

        //请输入正确的数量，且不大于可用余额{n}
        if(!RegexUtil.checkNumeric(v_withdraw_amount) || Double.valueOf(v_withdraw_amount)>available_amount){
            String tip_text = String.format(m_activity.getString(R.string.amount_rule),String.valueOf(available_amount));
            ToastUtils.showToast(tip_text);
            return false;
        }

        //提币手续费
        String v_inp_withdraw_fee = tv_withdraw_fee.getText().toString().trim();
        
        //判断提币数量大于可用
        //1. 主链币
        if(tranferToken.name.equals(tranferToken.chain)){
            Double inputAllAmount = NumberUtil.add(v_withdraw_amount,v_inp_withdraw_fee);
            if(Double.valueOf(inputAllAmount) > available_amount){
                ToastUtils.showToast(m_activity.getString(R.string.tip_withdraw_amount_error_0));
                return false;
            }
        }else{
            if( Double.valueOf(v_withdraw_amount)>available_amount){
                ToastUtils.showToast(m_activity.getString(R.string.error_withdraw_amout_more_available));
                return false;
            }
        }
        return true;
    }

    //更新可用资产
    public void updateAssets() {
        tranferBalance = BHBalanceHelper.getBHBalanceFromAccount(tranferToken.symbol);
        //可用数量
        String available_amount_str =  BHBalanceHelper.getAmountForUser(m_activity,tranferBalance.amount,"0",tranferBalance.symbol);
        available_amount = Double.valueOf(available_amount_str);
        tv_available_amount.setText(m_activity.getString(R.string.available)+" "+available_amount_str+" "+tranferToken.name.toUpperCase());
    }
}
