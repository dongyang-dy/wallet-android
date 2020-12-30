package com.bhex.wallet.common.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckedTextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.keyborad.PasswordInputView;
import com.bhex.lib.uikit.widget.keyborad.PasswordKeyBoardView;
import com.bhex.network.mvx.base.BaseDialogFragment;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.common.R;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.base.BaseFragment;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.SecuritySettingManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author gongdongyang
 * 2020-12-29 19:09:56
 */
public class Password30PFragment extends BaseDialogFragment {

    private int position;
    private FragmentManager mFm;

    private int verifyPwdWay = BH_BUSI_TYPE.校验当前账户密码.getIntValue();

    private boolean isShow30Password = true;

    PasswordInputView mPasswordInputView;
    PasswordKeyBoardView mPasswordKeyboardView;
    CheckedTextView ck_password;

    private Password30PFragment.PasswordClickListener passwordClickListener;

    @Override
    public int getLayout() {
        return R.layout.fragment_password30_p;
    }

    @Override
    protected void initView() {
        mRootView.findViewById(R.id.keyboard_root).setVisibility(View.VISIBLE);

        mPasswordInputView = mRootView.findViewById(R.id.input_password);
        mPasswordKeyboardView = mRootView.findViewById(R.id.my_keyboard);
        ck_password = mRootView.findViewById(R.id.ck_password);

        //mRootView.findViewById(R.id.keyboard_tool).setVisibility(View.GONE);



        /*btn_finish.setOnClickListener(v -> {
            dismissAllowingStateLoss();
        });*/

        mPasswordKeyboardView.setAttachToEditText(getActivity(),mPasswordInputView.m_input_content,mPasswordInputView,
                mRootView.findViewById(R.id.keyboard_root));

        mPasswordKeyboardView.setOnKeyListener(new PasswordKeyBoardView.OnKeyListener() {
            @Override
            public void onInput(String text) {
                mPasswordInputView.onInputChange(mPasswordInputView.m_input_content.getEditableText());
            }

            @Override
            public void onDelete() {
                mPasswordInputView.onKeyDelete();
            }
        });


        mPasswordInputView.setOnInputListener(new PasswordInputView.OnInputListener() {
            @Override
            public void onComplete(String input) {
                checkPassword(input);
            }

            @Override
            public void onChange(String input) {

            }

            @Override
            public void onClear() {

            }
        });

        //点击事件
        mRootView.findViewById(R.id.iv_close).setOnClickListener(this::onViewClick);
        mRootView.findViewById(R.id.ck_password).setOnClickListener(this::onViewClick);

        mRootView.findViewById(R.id.ck_password).setVisibility(isShow30Password?View.VISIBLE:View.GONE);
    }

    //
    public void getListenInfo(View view){
        try{
            Method method = View.class.getDeclaredMethod("getListenerInfo");
            //设置权限
            method.setAccessible(true);
            //得到点击事件持有者
            Object listenerInfo = method.invoke(view);
            //得到点击事件对象
            Class<?> listenerInfoClz = Class.forName("android.view.View$ListenerInfo");
            Field field = listenerInfoClz.getDeclaredField("mOnClickListener");
            //将点击事件代理类设置到“持有者中”
            field.set(listenerInfo, null);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //键盘设置清空
        AppCompatTextView btn_finish = mRootView.findViewById(R.id.btn_finish);
        btn_finish.setText(getString(R.string.clear));
        getListenInfo(btn_finish);
        btn_finish.setOnClickListener(v->{
            //
            mPasswordInputView.m_input_content.setText("");
        });
    }

    public void initStart(){
        setStyle(DialogFragment.STYLE_NO_TITLE, STYLE_NO_TITLE);
        Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getAttributes().windowAnimations = com.bhex.network.R.style.bottomDialogStyle;

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;

        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = dm.heightPixels- PixelUtils.dp2px(getContext(),20);

        window.setAttributes(params);
    }


    //检验密码
    private void checkPassword(String inputPassword){
        BHWallet currentWallet = BHUserManager.getInstance().getCurrentBhWallet();
        if(TextUtils.isEmpty(inputPassword)){
            ToastUtils.showToast(getResources().getString(R.string.please_input_password));
            return;
        }

        //
        if(verifyPwdWay== BH_BUSI_TYPE.校验当前账户密码.getIntValue()){
            if(!ToolUtils.isVerifyPass(inputPassword,currentWallet.password)){
                CommonFragment fragment = CommonFragment
                        .builder(getActivity())
                        .setMessage(getActivity().getString(R.string.password_error_retry))
                        .setLeftText(getActivity().getString(R.string.cancel))
                        .setRightText(getActivity().getString(R.string.retry))
                        .setAction(this::commonViewClick)
                        .create();
                fragment.show(getActivity().getSupportFragmentManager(),CommonFragment.class.getName());
                dismissAllowingStateLoss();

                return;
            }else{
                dismiss();
                passwordClickListener.confirmAction(inputPassword,position,verifyPwdWay,true);
                if(ck_password.isChecked()){
                    //开启30分钟计时
                    SecuritySettingManager.getInstance().request_thirty_in_time(true,inputPassword);
                }else{
                    //关闭30分钟计时
                    SecuritySettingManager.getInstance().request_thirty_in_time(false,"");
                }
            }
        }else{
            if(!ToolUtils.isVerifyPass(inputPassword,currentWallet.password)){
                passwordClickListener.confirmAction(inputPassword,position,verifyPwdWay,false);
            }else{
                passwordClickListener.confirmAction(inputPassword,position,verifyPwdWay,true);
            }
        }

    }

    //低
    private void commonViewClick(View view) {
        if(view.getId()==R.id.btn_sure){
            //再次弹出密码输入框
            Password30PFragment.showPasswordDialog(mFm,Password30PFragment.class.getName(),passwordClickListener,position,isShow30Password);
        }else{
            //ToastUtils.showToast("btn_cancel");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog =  super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        return dialog;
    }

    //点击事件
    private void onViewClick(View view) {
        if(view.getId()==R.id.iv_close){
            dismissAllowingStateLoss();
        }else if(view.getId()==R.id.ck_password){
            ck_password.toggle();
        }
    }

    public static Password30PFragment showPasswordDialog(FragmentManager fm, String tag,
                                                         Password30PFragment.PasswordClickListener listener, int position,boolean isShow30Password) {
        Password30PFragment pfrag = new Password30PFragment();
        pfrag.passwordClickListener = listener;
        pfrag.position = position;
        pfrag.isShow30Password = isShow30Password;
        pfrag.mFm = fm;
        if(pfrag.isShow30Password){
            if(SecuritySettingManager.getInstance().notNeedPwd()){
                pfrag.passwordClickListener.confirmAction(
                        BHUserManager.getInstance().getCurrentBhWallet().pwd,
                        position,BH_BUSI_TYPE.校验当前账户密码.getIntValue(),
                        true);
            }else{
                pfrag.show(fm, tag);
            }
        }else{
            pfrag.show(fm, tag);
        }
        return pfrag;
    }

    public void setVerifyPwdWay(int verifyPwdWay) {
        this.verifyPwdWay = verifyPwdWay;
    }

    public interface PasswordClickListener {
        void confirmAction(String password, int position,int way,boolean isRight);
    }

}