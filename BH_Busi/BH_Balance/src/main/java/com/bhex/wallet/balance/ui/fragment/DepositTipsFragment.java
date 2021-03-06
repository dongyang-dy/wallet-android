package com.bhex.wallet.balance.ui.fragment;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.network.mvx.base.BaseBottomSheetDialog;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.wallet.balance.R;

/**
 * @author gongdongyang
 * 2020-12-23 15:55:06
 */
public class DepositTipsFragment extends BaseBottomSheetDialog {
    private String mTipCotent;

    @Override
    public int getLayout() {
        return R.layout.fragment_deposit_tips;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        GradientDrawable drawable = ShapeUtils.getRoundRectTopDrawable(PixelUtils.dp2px(getContext(),6),
                ColorUtil.getColor(getContext(),R.color.app_bg),true,0);
        mRootView.setBackground(drawable);

        mRootView.findViewById(R.id.btn_i_know).setOnClickListener(v->{
            dismiss();
        });

        mRootView.findViewById(R.id.iv_close).setOnClickListener(v -> {
            dismiss();
        });

        AppCompatTextView tv_tip_content = mRootView.findViewById(R.id.tv_tip_content);
        if(!TextUtils.isEmpty(mTipCotent)){
            tv_tip_content.setText(mTipCotent);
        }

    }

    public static DepositTipsFragment newInstance(String msg){
        DepositTipsFragment fragment = new DepositTipsFragment();
        fragment.mTipCotent = msg;
        return  fragment;
    }

}