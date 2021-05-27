package com.bhex.wallet.balance.ui.fragment;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.bhex.tools.utils.PixelUtils;
import com.bhex.network.app.BaseApplication;
import com.bhex.network.mvx.base.BaseDialogFragment;
import com.bhex.tools.constants.BHConstants;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.common.manager.BHUserManager;
import com.google.android.material.button.MaterialButton;

/**
 * @author gongdongyang
 * 2020-4-8 15:46:11
 * 提取分红
 */
public class WithDrawShareFragment extends BaseDialogFragment {

    private FragmentItemListener mItemListener;

    private String with_reward_content;

    public AppCompatTextView tv_reward_text;

    public String mAllReward;

    @Override
    public int getLayout() {
        return R.layout.fragment_with_draw_share;
    }

    @Override
    public void initStart() {
        setStyle(DialogFragment.STYLE_NO_TITLE, STYLE_NO_TITLE);
        Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getAttributes().windowAnimations = R.style.centerDialogStyle;

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;

        params.width = ViewGroup.LayoutParams.MATCH_PARENT;

        params.width = dm.widthPixels- PixelUtils.dp2px(BaseApplication.getInstance(),48);
        //params.height = PixelUtils.dp2px(BaseApplication.getInstance(),280);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        window.setAttributes(params);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tv_reward_text = mRootView.findViewById(R.id.tv_reward_text);

        with_reward_content = getActivity().getResources().getString(R.string.with_reward_content);

        tv_reward_text.setText(String.format(with_reward_content,mAllReward, BHUserManager.getInstance().getDefaultGasFee().displayFee));

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        MaterialButton btn_cancel = mRootView.findViewById(R.id.btn_cancel);

        MaterialButton btn_confirm = mRootView.findViewById(R.id.btn_confirm);

        btn_cancel.setOnClickListener(v -> {
            dismiss();
        });


        btn_confirm.setOnClickListener(v -> {
            dismiss();
            if(mItemListener==null){
                return;
            }

            mItemListener.clickItemAction(1);
        });
    }

    public static void showWithDrawShareFragment(FragmentManager fm, String tag, FragmentItemListener listener,String allReward){
        WithDrawShareFragment fragment = new WithDrawShareFragment();
        fragment.mItemListener = listener;
        fragment.mAllReward = allReward;
        fragment.show(fm,tag);
    }

    public void setItemListener(FragmentItemListener mItemListener) {
        this.mItemListener = mItemListener;
    }

    public interface FragmentItemListener{
        public void clickItemAction(int position);
    }
}
