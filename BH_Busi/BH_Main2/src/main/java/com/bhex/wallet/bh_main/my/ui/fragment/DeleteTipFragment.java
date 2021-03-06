package com.bhex.wallet.bh_main.my.ui.fragment;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.lib.uikit.widget.editor.SimpleTextWatcher;
import com.bhex.network.mvx.base.BaseDialogFragment;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.common.config.ARouterConfig;

/**
 * @author gongdongyang
 * 2020-12-5 21:58:18
 */
@Route(path = ARouterConfig.TRUSTEESHIP_Delete_tip,name="删除提示")
public class DeleteTipFragment extends BaseDialogFragment {

    private DeleteListener mDeleteListener;
    private AppCompatEditText inp_password;

    @Override
    public int getLayout() {
        return R.layout.fragment_delete_tip;
    }

    @Override
    protected void initView() {
        super.initView();
        //设置背景
        GradientDrawable drawable = ShapeUtils.getRoundRectTopDrawable(PixelUtils.dp2px(getContext(),16),
                ColorUtil.getColor(getContext(),R.color.dialog_fragment_background),
                true,0);
        mRootView.setBackgroundDrawable(drawable);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        AppCompatButton btn_confirm = mRootView.findViewById(R.id.btn_confirm);
        inp_password = mRootView.findViewById(R.id.inp_password);
        btn_confirm.setOnClickListener(v -> {
            /*dismiss();*/
            if(mDeleteListener==null){
                return;
            }

            ToolUtils.hintKeyBoard(getActivity(),inp_password);
            String v_inp_password = inp_password.getText().toString().trim();
            mDeleteListener.deleteAction(v_inp_password);
        });

        inp_password.addTextChangedListener(new SimpleTextWatcher(){
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                String v_inp_password = inp_password.getText().toString().trim();
                if(TextUtils.isEmpty(v_inp_password)){
                    btn_confirm.setEnabled(false);
                    //btn.setBackgroundResource(R.drawable.btn_disabled_gray);
                }else{
                    btn_confirm.setEnabled(true);
                }
            }
        });
    }


    public static DeleteTipFragment getInstance(DeleteListener listener){
        DeleteTipFragment fragment = new DeleteTipFragment();
        fragment.mDeleteListener = listener;
        return fragment;
    }

    //删除
    public interface DeleteListener{
        public void deleteAction(String pwd);
    }

}