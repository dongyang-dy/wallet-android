package com.bhex.wallet.balance.ui.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RadioButton;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.network.mvx.base.BaseDialogFragment;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.helper.BHWalletHelper;
import com.bhex.wallet.common.manager.BHUserManager;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author gongdongyang
 * 2021-3-3 21:09:58
 * 账户列表
 */
public class AccountListFragment extends BaseDialogFragment {
    private RecyclerView rcv_account_list;
    private AccountListAdapter accountListAdapter;

    private ChooseAccountListener chooseAccountListener;

    @Override
    public int getLayout() {
        return R.layout.fragment_account_list;
    }

    @Override
    public void initStart() {
        setStyle(DialogFragment.STYLE_NO_TITLE, STYLE_NO_TITLE);
        Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getAttributes().windowAnimations = com.bhex.network.R.style.bottomDialogStyle;

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;

        //钱包数量
        List<BHWallet> list = BHUserManager.getInstance().getAllWallet();
        if (list.size()<=2) {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }else{
            params.height = PixelUtils.dp2px(getContext(),400);
        }

        window.setAttributes(params);
    }

    @Override
    protected void initView() {
        super.initView();
        //设置圆角背景
        GradientDrawable drawable = ShapeUtils.getRoundRectTopDrawable((int)getResources().getDimension(R.dimen.main_radius_conner),
                ColorUtil.getColor(getContext(),R.color.app_bg),true,0);
        mRootView.setBackgroundDrawable(drawable);

        rcv_account_list = mRootView.findViewById(R.id.rcv_account_list);
        FrameLayout layout_create_wallet = mRootView.findViewById(R.id.layout_create_wallet);
        //设置圆角背景
        drawable = ShapeUtils.getRoundRectDrawable(PixelUtils.dp2px(getContext(),100),
                Color.parseColor("#1998C1FF"));
        layout_create_wallet.setBackgroundDrawable(drawable);

        layout_create_wallet.setOnClickListener(v -> {
            ARouter.getInstance().build(ARouterConfig.Trusteeship.Trusteeship_Add_Index)
                    .withInt(BHConstants.FLAG,1)
                    .navigation();
            dismissAllowingStateLoss();
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        List<BHWallet> walletList = BHUserManager.getInstance().getAllWallet();
        rcv_account_list.setAdapter(accountListAdapter = new AccountListAdapter(walletList));

        accountListAdapter.setOnItemClickListener((adapter, view, position) -> {
            AppCompatCheckBox ck_wallet = view.findViewById(R.id.ck_wallet);
            if(ck_wallet.isChecked()){
                return;
            }

            BHWallet bhWallet = accountListAdapter.getData().get(position);
            if(chooseAccountListener!=null){
                chooseAccountListener.chooseAccount(bhWallet);
            }
            dismiss();
        });

        //

    }

    public static AccountListFragment getInstance(ChooseAccountListener listener){
        AccountListFragment fragment = new AccountListFragment();
        fragment.chooseAccountListener = listener;
        return fragment;
    }


    class AccountListAdapter extends BaseQuickAdapter<BHWallet, BaseViewHolder> {

        public AccountListAdapter(@org.jetbrains.annotations.Nullable List<BHWallet> data) {
            super(R.layout.item_account_list, data);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, BHWallet wallet) {
            //账户名称
            holder.setText(R.id.tv_wallet_name,wallet.name);
            //账户地址
            BHWalletHelper.proccessAddress(holder.getView(R.id.tv_wallet_address),wallet.address);
            //是否选中
            AppCompatCheckBox ck_wallet = holder.getView(R.id.ck_wallet);
            ck_wallet.setEnabled(false);
            //当前账户
            BHWallet currentWallet = BHUserManager.getInstance().getCurrentBhWallet();
            if(currentWallet.address.equals(wallet.address)){
                ck_wallet.setChecked(true);
                //设置背景色
                holder.itemView.setBackgroundColor(ColorUtil.getColor(getContext(),R.color.bg_wallet_checked));
            }else{
                ck_wallet.setChecked(false);
                //设置背景色
                holder.itemView.setBackgroundColor(ColorUtil.getColor(getContext(),R.color.bg_wallet_uncheck));
            }
        }
    }

    //
    public interface ChooseAccountListener{
        void chooseAccount(BHWallet bhWallet);
    }

}