package com.bhex.wallet.balance.ui.fragment;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.network.mvx.base.BaseDialogFragment;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.adapter.AccountListAdapter;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.manager.BHUserManager;

import java.util.List;

/**
 * @author gongdongyang
 * 2021-3-3 21:09:58
 * 账户列表
 */
public class AccountListFragment extends BaseDialogFragment {
    private RecyclerView rcv_account_list;
    private AccountListAdapter accountListAdapter;

    @Override
    public int getLayout() {
        return R.layout.fragment_account_list;
    }

    @Override
    protected void initView() {
        super.initView();
        //设置圆角背景
        GradientDrawable drawable = ShapeUtils.getRoundRectTopDrawable((int)getResources().getDimension(R.dimen.main_large_radius_conner),
                ColorUtil.getColor(getContext(),R.color.app_bg),true,0);
        mRootView.setBackgroundDrawable(drawable);

        rcv_account_list = mRootView.findViewById(R.id.rcv_account_list);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        List<BHWallet> walletList = BHUserManager.getInstance().getAllWallet();
        rcv_account_list.setAdapter(accountListAdapter = new AccountListAdapter(walletList));
    }

    public static AccountListFragment getInstance(){
        AccountListFragment fragment = new AccountListFragment();
        return fragment;
    }
}