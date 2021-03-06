package com.bhex.wallet.mnemonic.ui.activity;

import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.viewmodel.WalletViewModel;
import com.bhex.wallet.mnemonic.R;
import com.bhex.wallet.mnemonic.R2;
import com.bhex.wallet.mnemonic.adapter.AccountManagerAdapter;
import com.bhex.wallet.mnemonic.persenter.TrustManagerPresenter;

import java.util.List;

import butterknife.BindView;

/**
 * @author gongdongyang
 * 2020-3-14 20:26:34
 * 托管单元管理
 */
@Route(path = ARouterConfig.ACCOUNT_MANAGER_PAGE)
public class AccountManagerActivity extends BaseActivity<TrustManagerPresenter>{

    private final int Account_request_code = 100;

    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;

    @BindView(R2.id.rcv_account_manager)
    RecyclerView rcv_account_manager;

    AccountManagerAdapter accountAdapter;

    //WalletViewModel walletViewModel;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_account_manager;
    }

    @Override
    protected void initView() {
        //账户管理
        tv_center_title.setText(getString(R.string.trustship_manager));
        //所有账户地址
        List<BHWallet> walletList = BHUserManager.getInstance().getAllWallet();
        rcv_account_manager.setAdapter(accountAdapter = new AccountManagerAdapter(walletList));
    }

    @Override
    protected void addEvent() {
        accountAdapter.setOnItemClickListener((adapter, view, position) -> {
            BHWallet wallet = accountAdapter.getData().get(position);

            ARouter.getInstance()
                    .build(ARouterConfig.My.ACCOUNT_DETAIL_PAGE)
                    .withString(BHConstants.WALLET_ADDRESS,wallet.address)
                    .navigation(AccountManagerActivity.this,Account_request_code);
        });

        //导入账户
        findViewById(R.id.btn_import_wallet).setOnClickListener(v->{
            ARouter.getInstance().build(ARouterConfig.Trusteeship.Trusteeship_Add_Index).navigation();
        });

        //导入账户
        findViewById(R.id.btn_create_wallet).setOnClickListener(v->{
            ARouter.getInstance().build(ARouterConfig.TRUSTEESHIP_MNEMONIC_FRIST).navigation();
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Account_request_code){
            LogUtils.d("AccountManagerActivity==>:","==onActivityResult==");
            List<BHWallet> walletList = BHUserManager.getInstance().getAllWallet();
            //accountAdapter.getData().clear();
            accountAdapter.setNewData(walletList);
        }
    }
}
