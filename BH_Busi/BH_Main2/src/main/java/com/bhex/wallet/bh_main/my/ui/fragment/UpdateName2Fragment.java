package com.bhex.wallet.bh_main.my.ui.fragment;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.network.mvx.base.BaseDialogFragment;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.viewmodel.WalletViewModel;

/**
 *
 */
public class UpdateName2Fragment extends BaseDialogFragment {

    private WalletViewModel walletViewModel;

    private BHWallet bhWallet;

    private EditText inp_wallet_name;

    private UpdateNameListener updateNameListener;

    @Override
    public int getLayout() {
        return R.layout.fragment_update_name2;
    }

    @Override
    protected void initView() {
        super.initView();

        walletViewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
        walletViewModel.mutableLiveData.observe(this,ldm->{
            updateUserNameCallBack(ldm);
        });
        //
        //String wallet_name = getArguments().getString("name");

        //设置圆角背景
        GradientDrawable drawable = ShapeUtils.getRoundRectTopDrawable((int)getResources().getDimension(R.dimen.main_large_radius_conner),
                ColorUtil.getColor(getContext(),R.color.app_bg),true,0);
        mRootView.setBackgroundDrawable(drawable);

        LogUtils.d("UpdateName2Fragment==>:","bhWallet=="+bhWallet);
        //设置钱包名称
        inp_wallet_name = mRootView.findViewById(R.id.inp_wallet_name);
        inp_wallet_name.setText(bhWallet.getName());

        //
        mRootView.findViewById(R.id.btn_sure).setOnClickListener(v -> {
            updateUserNameAction();
        });

        mRootView.findViewById(R.id.iv_close).setOnClickListener(v -> {
            dismissAllowingStateLoss();
        });
    }

    private void updateUserNameAction(){
        String wallet_name = inp_wallet_name.getText().toString().trim();
        if(TextUtils.isEmpty(wallet_name)){
            ToastUtils.showToast(getString(R.string.hint_input_username));
            return;
        }
        ToolUtils.hintKeyBoard(getActivity(), inp_wallet_name);
        bhWallet.name = wallet_name;
        walletViewModel.updateWalletName(this,bhWallet);

    }

    private void updateUserNameCallBack(LoadDataModel ldm) {
        dismiss();
        if(ldm.loadingStatus == LoadingStatus.SUCCESS){
            ToastUtils.showToast(getString(R.string.update_success));
        }
        if(updateNameListener!=null){
            updateNameListener.updateNameCallback();
        }
    }

    public static UpdateName2Fragment getInstance(BHWallet wallet,UpdateNameListener listener){
        UpdateName2Fragment fragment = new UpdateName2Fragment();
        fragment.bhWallet = wallet;
        fragment.updateNameListener = listener;
        return fragment;
    }

    public interface  UpdateNameListener{
        void updateNameCallback();
    }
}