package com.bhex.wallet.balance.ui.viewhodler;

import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatTextView;

import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.ui.activity.ChainTokenActivity;
import com.bhex.wallet.balance.ui.fragment.AddressQRFragment;
import com.bhex.wallet.common.helper.BHWalletHelper;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.BHBalance;

/**
 * @author gongdongyang
 * 2020-10-15 19:02:22
 */
public class HBCViewHolder {

    private static  HBCViewHolder  _instance = new HBCViewHolder();

    public LinearLayout viewHolder;

    public AppCompatTextView tv_token_name;
    public AppCompatTextView tv_hbc_addr_label;
    public AppCompatTextView tv_hbc_address;
    public AppCompatTextView tv_apply_test_coin;

    public BaseActivity mContext;
    BHBalance mBalance;

    public static HBCViewHolder getInstance(){
        return _instance;
    }

    public HBCViewHolder initView(BaseActivity activity,LinearLayout view, BHBalance balance){
        viewHolder = view;
        mContext = activity;
        /*tv_token_name = view.findViewById(R.id.tv_token_name);
        tv_hbc_address = view.findViewById(R.id.tv_hbc_address);
        tv_hbc_addr_label = view.findViewById(R.id.tv_hbc_addr_label);

        tv_apply_test_coin = view.findViewById(R.id.tv_apply_test_coin);

        this.mBalance = balance;

        view.findViewById(R.id.iv_hbc_qr).setOnClickListener(v -> {
            showQRDialog();
        });

        view.findViewById(R.id.iv_hbc_paste).setOnClickListener(v->{
            ToolUtils.copyText(tv_hbc_address.getTag().toString(),mContext);
            ToastUtils.showToast(mContext.getString(R.string.copyed));
        });

        tv_apply_test_coin.setOnClickListener(v -> {
            ChainTokenActivity chainTokenActivity = (ChainTokenActivity)activity;
            chainTokenActivity.applyTestToken();
        });

        GradientDrawable drawable = ShapeUtils.getRoundRectDrawable(28, ColorUtil.getColor(mContext, R.color.white));
        tv_apply_test_coin.setBackground(drawable);*/

        return _instance;
    }

    public void setTokenAddress(BHBalance balance) {
        this.mBalance = balance;
        if(!mBalance.chain.equalsIgnoreCase(BHConstants.BHT_TOKEN)){
            viewHolder.setVisibility(View.GONE);
        }
        tv_token_name.setText(mBalance.symbol.toUpperCase());
        tv_hbc_address.setTag(BHUserManager.getInstance().getCurrentBhWallet().address);
        BHWalletHelper.proccessAddress(tv_hbc_address,BHUserManager.getInstance().getCurrentBhWallet().address);
    }

    private void showQRDialog() {
        AddressQRFragment.showFragment(mContext.getSupportFragmentManager(),
                AddressQRFragment.class.getSimpleName(),
                mBalance.symbol.toUpperCase(),
                tv_hbc_address.getTag().toString());
    }
}
