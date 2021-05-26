package com.bhex.wallet.balance.ui.viewhodler;

import android.text.InputFilter;
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
import com.bhex.tools.utils.ImageLoaderUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.tools.utils.RegexUtil;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.balance.helper.BHTokenHelper;
import com.bhex.wallet.balance.ui.activity.AddressBookListActivity;
import com.bhex.wallet.balance.ui.fragment.DepositTipsFragment;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.filter.DecimalDigitsInputFilter;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHChain;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.ui.activity.BHQrScanActivity;
import com.bhex.wallet.common.utils.AddressUtil;
import com.google.android.material.button.MaterialButton;

/**
 * @author gdy
 * 跨链提币
 */
public class TransferOutCrossVH {
    private BaseActivity m_activity;
    private View mRootView;
    //选择Token
    public View layout_select_token;
    public AppCompatImageView iv_token_icon;
    public AppCompatTextView tv_withdraw_token;

    //选择链
    public View layout_select_chain;
    public AppCompatImageView iv_chain_icon;
    public AppCompatTextView tv_chain_name;

    //提币地址
    public AppCompatEditText inp_withdraw_address;

    //扫描地址
    public AppCompatImageView btn_address_scan;
    //地址簿功能
    public AppCompatImageView btn_address_book;

    public AppCompatTextView tv_cross_transfer_out_tip;
    //提币数量
    public AppCompatEditText inp_withdraw_amount;

    //提币Token
    public AppCompatTextView tv_withdraw_unit;

    //提币手续费
    public AppCompatTextView tv_fee;
    public AppCompatTextView tv_fee_token;
    public AppCompatTextView tv_withdraw_fee;
    public AppCompatTextView tv_withdraw_fee_token;

    //全部
    public AppCompatTextView btn_all;
    //可用数量
    public AppCompatTextView tv_available_amount;

    //提币按钮
    public MaterialButton btn_drawwith_coin;

    public BHToken showToken;
    //提币Token
    public BHToken withDrawToken;
    //提币资产
    public BHBalance withDrawBalance;

    //可以提币或转账数量
    public double available_amount;

    //跨链手续费
    public AppCompatTextView tv_withdraw_fee_label;

    public TransferOutCrossVH(BaseActivity m_activity, View mRootView) {
        this.m_activity = m_activity;
        this.mRootView = mRootView;
        //初始化布局
        layout_select_token = mRootView.findViewById(R.id.layout_select_token);
        iv_token_icon = mRootView.findViewById(R.id.iv_token_icon);
        tv_withdraw_token = mRootView.findViewById(R.id.tv_withdraw_token);

        layout_select_chain = mRootView.findViewById(R.id.layout_select_chain);
        iv_chain_icon = mRootView.findViewById(R.id.iv_chain_icon);
        tv_chain_name = mRootView.findViewById(R.id.tv_chain_name);

        inp_withdraw_address = mRootView.findViewById(R.id.inp_withdraw_address);
        btn_address_scan = mRootView.findViewById(R.id.btn_address_scan);
        btn_address_book = mRootView.findViewById(R.id.btn_address_book);

        inp_withdraw_address = mRootView.findViewById(R.id.inp_withdraw_address);

        btn_address_scan = mRootView.findViewById(R.id.btn_address_scan);
        btn_address_book = mRootView.findViewById(R.id.btn_address_book);

        tv_cross_transfer_out_tip = mRootView.findViewById(R.id.tv_cross_transfer_out_tip);

        inp_withdraw_amount = mRootView.findViewById(R.id.inp_withdraw_amount);
        tv_withdraw_unit = mRootView.findViewById(R.id.tv_withdraw_unit);
        tv_withdraw_unit = mRootView.findViewById(R.id.tv_withdraw_unit);

        tv_fee  = mRootView.findViewById(R.id.tv_fee);
        tv_fee_token  = mRootView.findViewById(R.id.tv_fee_token);
        tv_withdraw_fee  = mRootView.findViewById(R.id.tv_withdraw_fee);
        tv_withdraw_fee_token  = mRootView.findViewById(R.id.tv_withdraw_fee_token);

        btn_all  = mRootView.findViewById(R.id.btn_all);
        tv_available_amount  = mRootView.findViewById(R.id.tv_available_amount);

        btn_drawwith_coin  = mRootView.findViewById(R.id.btn_drawwith_coin);

        tv_withdraw_fee_label = mRootView.findViewById(R.id.tv_withdraw_fee_label);

        //二维码扫描
        btn_address_scan.setOnClickListener(v -> {
            ARouter.getInstance().build(ARouterConfig.Common.commom_scan_qr).navigation(m_activity, BHQrScanActivity.REQUEST_CODE);
        });

        //地址簿功能
        btn_address_book.setOnClickListener(v->{
            String v_inp_drawwith_address = inp_withdraw_address.getText().toString().trim();
            LogUtils.d("AddAddressVH===","chain==1=="+withDrawToken.chain);
            ARouter.getInstance().build(ARouterConfig.Balance.Balance_address_list)
                    .withString(BHConstants.CHAIN,withDrawToken.chain)
                    .withString(BHConstants.ADDRESS,v_inp_drawwith_address)
                    .navigation(m_activity, AddressBookListActivity.REQUEST_ADDRESS);
        });

        //设置输入框键盘类型
        inp_withdraw_amount.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        tv_withdraw_fee.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inp_withdraw_amount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(BHConstants.BHT_DEFAULT_DECIMAL+1)});

        btn_all.setOnClickListener(this::withdrawAllAction);

        //
        tv_withdraw_fee_label.setOnClickListener(v -> {
            DepositTipsFragment.newInstance(m_activity.getString(R.string.cross_withdraw_tips))
                    .show(m_activity.getSupportFragmentManager(),DepositTipsFragment.class.getName());
        });
    }


    public void updateTokenInfo(String symbol,String chain) {
        showToken = CacheCenter.getInstance().getSymbolCache().getBHToken(symbol);

        withDrawToken = BHTokenHelper.getCrossBHToken(symbol,chain);

        //设置提币Token
        tv_withdraw_token.setText(showToken.name.toUpperCase());
        tv_withdraw_unit.setText(showToken.name.toUpperCase());
        ImageLoaderUtil.loadImageViewToCircle(m_activity,showToken.logo,iv_token_icon,R.mipmap.ic_default_coin);
        //获取链的BHChain
        BHChain bhChain = BHTokenHelper.getBHChain(chain);
        tv_chain_name.setText(bhChain.full_name);
        ImageLoaderUtil.loadImageViewToCircle(m_activity,bhChain.logo,iv_chain_icon,R.mipmap.ic_default_coin);

        //设置提示文本颜色
        String v_cross_transfer_tips = m_activity.getString(R.string.cross_transfer_out_tips);
        String v_hightlight_text = m_activity.getString(R.string.cross_transfer_highlight_text);
        SpannableString spannableString = new SpannableString(v_cross_transfer_tips);
        int start_index = v_cross_transfer_tips.indexOf(v_hightlight_text);
        if(start_index>=0){
            //设置HBC 高亮
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(ColorUtil.getColor(m_activity,R.color.transfer_highlight_text_color));
            spannableString.setSpan(colorSpan,start_index ,
                    start_index+v_hightlight_text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            tv_cross_transfer_out_tip.setText(spannableString);
        }

        //跨链手续费Token
        tv_withdraw_fee_token.setText(withDrawToken.chain.toUpperCase());
        tv_withdraw_fee.setText(withDrawToken!=null?withDrawToken.withdrawal_fee:"");
        //手续费
        tv_fee.setText(BHUserManager.getInstance().getDefaultGasFee().displayFee);
        tv_fee_token.setText(BHConstants.BHT_TOKEN.toUpperCase());
    }

    //更新资产
    public void updateAssets() {
        withDrawBalance = BHBalanceHelper.getBHBalanceFromAccount(showToken.symbol);
        //可用数量
        String available_amount_str =  BHBalanceHelper.getAmountForUser(m_activity,withDrawBalance.amount,"0",showToken.symbol);
        available_amount = Double.valueOf(available_amount_str);
        tv_available_amount.setText(m_activity.getString(R.string.available)+" "+available_amount_str+" "+withDrawBalance.name.toUpperCase());
    }


    public boolean verifyWithDrawAction(){
        //提币地址
        String v_withdraw_address = inp_withdraw_address.getText().toString().trim();
        if(TextUtils.isEmpty(v_withdraw_address)){
            ToastUtils.showToast(m_activity.getResources().getString(R.string.input_enter_address));
            inp_withdraw_address.requestFocus();
            return false;
        }

        //检验地址合法性
        //1. 判断是否数字或字母
        if(!RegexUtil.isLetterDigit(v_withdraw_address)){
            ToastUtils.showToast(m_activity.getString(R.string.address_verify_error));
            return false;
        }

        boolean flag = true;
        //地址检测
        if(withDrawToken.chain.equals("btc")){
            flag = AddressUtil.validBtcAddress(v_withdraw_address);
        }

        if(withDrawToken.chain.equals("eth")){
            flag = AddressUtil.validEthAddress(v_withdraw_address);
        }

        if(!flag){
            ToastUtils.showToast(m_activity.getString(R.string.address_verify_error));
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
            inp_withdraw_amount.requestFocus();
            ToastUtils.showToast(tip_text);
            return false;
        }

        //请输入正确的数量，且不大于max_withdrawal_amount
        if(Double.valueOf(v_withdraw_amount) > Double.valueOf(withDrawToken.max_withdrawal_amount)){
            String tip_text = String.format(m_activity.getString(R.string.amount_max_rule),withDrawToken.max_withdrawal_amount);
            inp_withdraw_amount.requestFocus();
            ToastUtils.showToast(tip_text);
            return false;
        }

        //判断手续费
        BHBalance hbcBalance = BHBalanceHelper.getBHBalanceFromAccount(BHConstants.BHT_TOKEN);
        if(hbcBalance== null){
            ToastUtils.showToast(m_activity.getString(R.string.fee_notenough));
            return false;
        }

        if(Double.valueOf(hbcBalance.amount) < Double.valueOf(BHUserManager.getInstance().getDefaultGasFee().displayFee)){
            ToastUtils.showToast(m_activity.getString(R.string.fee_notenough));
            return false;
        }

        //判断跨链手续费


        return true;
    }

    //全部提币
    private void withdrawAllAction(View view) {
        //主链币
        inp_withdraw_amount.setText(NumberUtil.toPlainString(available_amount));
        inp_withdraw_amount.setSelection(inp_withdraw_amount.getText().length());
        inp_withdraw_amount.requestFocus();
    }

    public void updateWithDrawFee() {
        tv_withdraw_fee_token.setText(withDrawToken.chain.toUpperCase());
        tv_withdraw_fee.setText(withDrawToken!=null?withDrawToken.withdrawal_fee:"");
    }
}
