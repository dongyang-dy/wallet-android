package com.bhex.wallet.balance.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.network.mvx.base.BaseDialogFragment;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.tools.utils.QRCodeEncoder;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.manager.BHUserManager;

/**
 * @author gongdongyagn
 * 2021-3-3 23:55:34
 */
public class HbcAddressQrFragment extends BaseDialogFragment {

    @Override
    public int getLayout() {
        return R.layout.fragment_hbc_address_qr;
    }

    public static HbcAddressQrFragment getInstance(){
        HbcAddressQrFragment fragment = new HbcAddressQrFragment();
        return fragment;
    }

    @Override
    protected void initView() {
        super.initView();
        //设置圆角背景
        GradientDrawable drawable = ShapeUtils.getRoundRectTopDrawable((int)getResources().getDimension(R.dimen.main_radius_conner),
                ColorUtil.getColor(getContext(),R.color.app_bg),true,0);
        mRootView.setBackgroundDrawable(drawable);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //当前钱包
        BHWallet currentWallet = BHUserManager.getInstance().getCurrentBhWallet();

        //钱包名称
        AppCompatTextView tv_wallet_name = mRootView.findViewById(R.id.tv_wallet_name);
        tv_wallet_name.setText(currentWallet.name);
        //钱包地址
        AppCompatTextView tv_wallet_address = mRootView.findViewById(R.id.tv_wallet_address);
        tv_wallet_address.setText(currentWallet.address);
        //增加背景
        GradientDrawable drawable = ShapeUtils.getRoundRectDrawable(PixelUtils.dp2px(getContext(),4),
                ColorUtil.getColor(getContext(),R.color.card_bg_color));
        tv_wallet_address.setBackgroundDrawable(drawable);
        //钱包二维码
        AppCompatImageView iv_address_qr = mRootView.findViewById(R.id.iv_address_qr);
        //tv_wallet_address.setText(currentWallet.address);
        Bitmap bitmap = QRCodeEncoder.syncEncodeQRCode(currentWallet.address,
                PixelUtils.dp2px(getContext(),160),
                ColorUtil.getColor(getContext(),android.R.color.black));

        iv_address_qr.setImageBitmap(bitmap);

        //复制地址
        mRootView.findViewById(R.id.btn_copy_address).setOnClickListener(v -> {
            if (TextUtils.isEmpty(currentWallet.address)) {
                return;
            }

            ToolUtils.copyText(currentWallet.address, getContext());
            ToastUtils.showToast(getString(R.string.copyed));
        });

        //关闭
        mRootView.findViewById(R.id.iv_close).setOnClickListener(v -> {
            dismissAllowingStateLoss();
        });
    }
}