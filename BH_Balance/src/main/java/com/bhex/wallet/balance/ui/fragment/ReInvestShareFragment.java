package com.bhex.wallet.balance.ui.fragment;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.bhex.lib.uikit.util.PixelUtils;
import com.bhex.network.app.BaseApplication;
import com.bhex.network.mvx.base.BaseDialogFragment;
import com.bhex.wallet.balance.R;
import com.google.android.material.button.MaterialButton;

/**
 * @author gongdongyang
 * 2020-4-8 16:40:31
 * 复投分红对话框
 */
public class ReInvestShareFragment extends BaseDialogFragment {

    private FragmentItemListener mItemListener;

    @Override
    public int getLayout() {
        return R.layout.fragment_re_invest_share;
    }



    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getAttributes().windowAnimations = R.style.centerDialogStyle;

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;

        params.width = ViewGroup.LayoutParams.MATCH_PARENT;

        params.width = dm.widthPixels- PixelUtils.dp2px(BaseApplication.getInstance(),24);
        params.height = PixelUtils.dp2px(BaseApplication.getInstance(),280);

        window.setAttributes(params);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        MaterialButton btn_cancel = mRootView.findViewById(R.id.btn_cancel);

        MaterialButton btn_confirm = mRootView.findViewById(R.id.btn_confirm);

        btn_cancel.setOnClickListener(v -> {
            dismiss();
            if(mItemListener==null){
                return;
            }

            mItemListener.clickItemAction(0);
        });


        btn_cancel.setOnClickListener(v -> {
            dismiss();
            if(mItemListener==null){
                return;
            }

            mItemListener.clickItemAction(1);
        });
    }

    public FragmentItemListener getItemListener() {
        return mItemListener;
    }

    public void setItemListener(FragmentItemListener mItemListener) {
        this.mItemListener = mItemListener;
    }

    public static void showWithDrawShareFragment(FragmentManager fm, String tag, FragmentItemListener listener){
        ReInvestShareFragment fragment = new ReInvestShareFragment();
        fragment.mItemListener = listener;
        fragment.show(fm,tag);
    }


    public interface FragmentItemListener{
        public void clickItemAction(int position);
    }


}
