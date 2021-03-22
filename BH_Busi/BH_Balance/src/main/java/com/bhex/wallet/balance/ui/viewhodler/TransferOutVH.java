package com.bhex.wallet.balance.ui.viewhodler;

import android.graphics.Color;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.tools.utils.RegexUtil;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.ui.activity.BHQrScanActivity;
import com.google.android.material.button.MaterialButton;

public class TransferOutVH {
    private BaseActivity m_activity;
    private View mRootView;

    //转出地址
    public AppCompatTextView tv_transfer_out_address;
    //接收地址
    public AppCompatEditText inp_transfer_in_address;
    //扫描地址
    public AppCompatImageView btn_address_scan;
    //地址簿功能
    public AppCompatImageView btn_address_book;
    //选择token
    public View layout_select_token;
    public AppCompatTextView tv_transfer_token;
    //转账数量
    public AppCompatEditText inp_transfer_amount;
    //全部
    public AppCompatTextView btn_all;
    //可用余额
    public AppCompatTextView tv_available_amount;
    //手续费
    public AppCompatTextView tv_fee;
    
    //转账提示
    public AppCompatTextView tv_transfer_out_tip;

    //转账按钮
    public MaterialButton btn_transfer;

    //转账token
    public BHToken tranferToken;
    //交易手续费token bht
    public BHToken hbtFeeToken;
    //转账资产
    public BHBalance tranferBalance;
    //交易手续费资产
    public BHBalance hbtFeeBalance;
    //可以提币或转账数量
    public double available_amount;

    public TransferOutVH(BaseActivity activity, View rootView){
        this.m_activity = activity;
        this.mRootView = rootView;

        //初始化控件
        tv_transfer_out_address = mRootView.findViewById(R.id.tv_transfer_out_address);
        inp_transfer_in_address = mRootView.findViewById(R.id.inp_transfer_in_address);
        btn_address_scan = mRootView.findViewById(R.id.btn_address_scan);
        btn_address_book = mRootView.findViewById(R.id.btn_address_book);
        layout_select_token = mRootView.findViewById(R.id.layout_select_token);
        tv_transfer_token = mRootView.findViewById(R.id.tv_transfer_token);
        inp_transfer_amount = mRootView.findViewById(R.id.inp_transfer_amount);
        btn_all = mRootView.findViewById(R.id.btn_all);
        tv_available_amount = mRootView.findViewById(R.id.tv_available_amount);
        tv_fee = mRootView.findViewById(R.id.tv_fee);
        btn_transfer = mRootView.findViewById(R.id.btn_transfer);
        tv_transfer_out_tip = mRootView.findViewById(R.id.tv_transfer_out_tip);

        //二维码扫描
        btn_address_scan.setOnClickListener(v -> {
            ARouter.getInstance().build(ARouterConfig.Common.commom_scan_qr).navigation(m_activity, BHQrScanActivity.REQUEST_CODE);
        });
        //地址簿功能
        btn_address_book.setOnClickListener(v->{
            String v_inp_transfer_in_address = inp_transfer_in_address.getText().toString().trim();

            ARouter.getInstance().build(ARouterConfig.Balance.Balance_address_list)
                    .withString(BHConstants.SYMBOL,tranferToken.symbol)
                    .withString("address",v_inp_transfer_in_address)
                    .navigation(m_activity, BHQrScanActivity.REQUEST_CODE);
        });

        btn_all.setOnClickListener(this::transferAllAction);

        //设置输入框键盘类型
        //inp_transfer_amount.setInputType(InputType.Te|InputType.TYPE_NUMBER_FLAG_DECIMAL);

        //设置提示文本颜色
        String v_transfer_inner_tips = activity.getString(R.string.transfer_inner_tips);
        SpannableString spannableString = new SpannableString(v_transfer_inner_tips);
        int start_index = v_transfer_inner_tips.indexOf(BHConstants.BHT_TOKEN.toUpperCase());
        if(start_index>=0){
            //设置HBC 高亮
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(ColorUtil.getColor(m_activity,R.color.transfer_highlight_text_color));

            spannableString.setSpan(colorSpan,start_index ,
                    start_index+BHConstants.BHT_TOKEN.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            tv_transfer_out_tip.setText(spannableString);
        }
    }



    public void updateTokenInfo(String symbol){
        tranferToken = CacheCenter.getInstance().getSymbolCache().getBHToken(symbol);
        //转账地址
        BHWallet currentWallet = BHUserManager.getInstance().getCurrentBhWallet();
        tv_transfer_out_address.setText(currentWallet.address);
        //转账币种资产
        tranferBalance = BHBalanceHelper.getBHBalanceFromAccount(tranferToken.symbol);

        //设置转账Token
        tv_transfer_token.setText(tranferToken.name.toUpperCase());
        //可用数量
        String available_amount_str =  BHBalanceHelper.getAmountForUser(m_activity,tranferBalance.amount,"0",tranferBalance.symbol);
        available_amount = Double.valueOf(available_amount_str);
        tv_available_amount.setText(m_activity.getString(R.string.available)+" "+available_amount_str+" "+tranferToken.name.toUpperCase());

        //手续费
        tv_fee.setText(BHUserManager.getInstance().getDefaultGasFee().displayFee);
    }

    //全部转账
    private void transferAllAction(View view) {
        //主链币
        if(tranferToken.name.equalsIgnoreCase(tranferToken.chain)){
            String all_count = NumberUtil.sub(String.valueOf(available_amount),tv_fee.getText().toString());
            all_count = Double.valueOf(all_count)<0?"0":all_count;
            inp_transfer_amount.setText(all_count);
        }else{
            inp_transfer_amount.setText(NumberUtil.toPlainString(available_amount));
        }
        inp_transfer_amount.setSelection(inp_transfer_amount.getText().length());
        inp_transfer_amount.requestFocus();
    }

    //更新可用资产
    public void updateAssets() {
        tranferBalance = BHBalanceHelper.getBHBalanceFromAccount(tranferToken.symbol);
        //可用数量
        String available_amount_str =  BHBalanceHelper.getAmountForUser(m_activity,tranferBalance.amount,"0",tranferBalance.symbol);
        available_amount = Double.valueOf(available_amount_str);
        tv_available_amount.setText(m_activity.getString(R.string.available)+" "+available_amount_str+" "+tranferToken.name.toUpperCase());
    }

    //验证转账输入
    public boolean verifyTransferAction() {
        //接收地址
        String v_transfer_in_address = inp_transfer_in_address.getText().toString().trim();
        if(TextUtils.isEmpty(v_transfer_in_address)){
            ToastUtils.showToast(m_activity.getString(R.string.input_enter_address));
            inp_transfer_in_address.requestFocus();
            return false;
        }
        //当前钱包地址
        BHWallet bhWallet = BHUserManager.getInstance().getCurrentBhWallet();
        String current_address = bhWallet.getAddress();
        if(!v_transfer_in_address.toUpperCase().startsWith(BHConstants.BHT_TOKEN.toUpperCase())
                || current_address.equalsIgnoreCase(v_transfer_in_address)){
            ToastUtils.showToast(m_activity.getString(R.string.error_transfer_address));
            inp_transfer_in_address.requestFocus();
            return false;
        }

        //可用余额判断
        if(TextUtils.isEmpty(String.valueOf(available_amount))|| available_amount<=0){
            ToastUtils.showToast(m_activity.getString(R.string.not_available_amount));
            return false;
        }

        //转账数量判断
        String v_transfer_amount = inp_transfer_amount.getText().toString().trim();
        if(TextUtils.isEmpty(v_transfer_amount) || !RegexUtil.checkNumeric(v_transfer_amount)  || Double.valueOf(v_transfer_amount)<=0){
            ToastUtils.showToast(m_activity.getString(R.string.please_transfer_amount));
            inp_transfer_amount.requestFocus();
            return false;
        }
        //手续费
        String v_fee_amount = tv_fee.getText().toString().trim();
        if(TextUtils.isEmpty(v_fee_amount) || !RegexUtil.checkNumeric(v_fee_amount) || Double.valueOf(v_fee_amount)<=0){
            ToastUtils.showToast(m_activity.getResources().getString(R.string.please_input_gasfee));
            return false;
        }

        //转账金额判断
        //1 主链币
        if(tranferToken.name.equalsIgnoreCase(tranferToken.chain)){
            Double inputAllAmount = NumberUtil.add(v_transfer_amount,v_fee_amount);
            if(Double.valueOf(inputAllAmount) > Double.valueOf(available_amount)){
                ToastUtils.showToast(m_activity.getString(R.string.tip_transferout_amount_error_0));
                inp_transfer_amount.requestFocus();
                return false;
            }
        }else{
            if(Double.valueOf(v_transfer_amount) > available_amount ){
                ToastUtils.showToast(m_activity.getString(R.string.tip_transferout_amount_error));
                inp_transfer_amount.requestFocus();
                return false;
            }
        }
        return  true;
    }
}
