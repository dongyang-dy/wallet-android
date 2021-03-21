package com.bhex.wallet.balance.ui.viewhodler;

import android.app.Activity;
import android.location.Address;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ImageLoaderUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.RegexUtil;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.ui.activity.AddAddressActivity;
import com.bhex.wallet.balance.ui.fragment.ChooseChainFragment;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.ui.activity.BHQrScanActivity;
import com.bhex.wallet.common.utils.AddressUtil;

/**
 * @author gongdongyang
 * 2021-3-20 17:32:05
 * 添加地址
 */
public class AddAddressVH {

    private AddAddressActivity activity;

    //private View view;
    public AppCompatImageView iv_chain_icon;
    public AppCompatTextView tv_chain_name;
    public AppCompatImageView btn_address_scan;
    //
    public AppCompatEditText inp_address;
    public AppCompatEditText inp_address_name;
    public AppCompatEditText inp_address_remark;

    public RelativeLayout layout_choose_chain;

    public AddAddressVH(AddAddressActivity activity) {
        this.activity = activity;
        //设置标题
        AppCompatTextView tv_center_title = activity.findViewById(R.id.tv_center_title);
        tv_center_title.setText(activity.getString(R.string.create_address));

        iv_chain_icon = activity.findViewById(R.id.iv_chain_icon);
        tv_chain_name = activity.findViewById(R.id.tv_chain_name);
        btn_address_scan = activity.findViewById(R.id.btn_address_scan);

        inp_address = activity.findViewById(R.id.inp_address);
        inp_address_name = activity.findViewById(R.id.inp_address_name);
        inp_address_remark = activity.findViewById(R.id.inp_address_remark);

        layout_choose_chain = activity.findViewById(R.id.layout_choose_chain);

        //二维码扫描
        btn_address_scan.setOnClickListener(v -> {
            ARouter.getInstance().build(ARouterConfig.Common.commom_scan_qr).navigation(activity, BHQrScanActivity.REQUEST_CODE);
        });


    }


    public void setChainIcon(String chain) {
        BHToken bhChainToken = SymbolCache.getInstance().getBHToken(chain);
        ImageLoaderUtil.loadImageView(activity,bhChainToken.logo,iv_chain_icon,R.mipmap.ic_default_coin);
        tv_chain_name.setText(chain.toUpperCase());
    }

    public boolean verifyInputAction(String chain) {
        BHToken bhChainToken = SymbolCache.getInstance().getBHToken(chain);

        String v_input_address = inp_address.getText().toString().trim();
        if(TextUtils.isEmpty(v_input_address)){
            ToastUtils.showToast(activity.getString(R.string.input_address_hint));
            return false;
        }

        String v_input_name = inp_address_name.getText().toString().trim();
        if(TextUtils.isEmpty(v_input_name)){
            ToastUtils.showToast(activity.getString(R.string.please_input_name));
            return false;
        }

        boolean flag = false;
        //检测地址是否数字或字母
        if(!RegexUtil.isLetterDigit(v_input_address)){
            ToastUtils.showToast(activity.getString(R.string.address_verify_error));
            return false;
        }

        //地址检测
        if(bhChainToken.chain.equals("btc")){
           flag = AddressUtil.validBtcAddress(v_input_address);
        }

        if(bhChainToken.chain.equals("eth")){
            flag = AddressUtil.validEthAddress(v_input_address);
        }

        if(bhChainToken.chain.equals(BHConstants.BHT_TOKEN)){
            flag = AddressUtil.validHbcAddress(v_input_address);
        }

        if(!flag){
            ToastUtils.showToast(activity.getString(R.string.address_verify_error));
            return false;
        }

        return true;
    }
}
