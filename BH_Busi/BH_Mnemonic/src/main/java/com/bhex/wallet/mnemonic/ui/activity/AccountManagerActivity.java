package com.bhex.wallet.mnemonic.ui.activity;

import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.network.base.LoadingStatus;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.MainActivityManager;
import com.bhex.wallet.common.model.BHWalletItem;
import com.bhex.wallet.common.viewmodel.BalanceViewModel;
import com.bhex.wallet.common.viewmodel.WalletViewModel;
import com.bhex.wallet.mnemonic.R;
import com.bhex.wallet.mnemonic.R2;
import com.bhex.wallet.mnemonic.adapter.AccountManagerAdapter;
import com.bhex.wallet.mnemonic.persenter.TrustManagerPresenter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import java8.util.stream.RefStreams;
import java8.util.stream.StreamSupport;

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

    private BalanceViewModel balanceViewModel;

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
        List<BHWalletItem> list = new ArrayList<>();
        StreamSupport.stream(walletList).forEach(item->{
            BHWalletItem bhWalletItem = BHWalletItem.makeBHWalletItem(item);
            list.add(bhWalletItem);
        });

        rcv_account_manager.setAdapter(accountAdapter = new AccountManagerAdapter(list));
        balanceViewModel = ViewModelProviders.of(MainActivityManager.getInstance().mainActivity).get(BalanceViewModel.class);

        balanceViewModel.accountLiveData.observe(MainActivityManager.getInstance().mainActivity,ldm->{
            if(ldm.getLoadingStatus()== LoadingStatus.SUCCESS){
                accountAdapter.updateAsset(ldm.getData());
            }
        });

    }

    @Override
    protected void addEvent() {
        accountAdapter.setOnItemClickListener((adapter, view, position) -> {
            BHWalletItem wallet = accountAdapter.getData().get(position);

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

        List<BHWallet> walletList = BHUserManager.getInstance().getAllWallet();
        for(int i=0;i<walletList.size();i++){
            BHWallet bhWallet = walletList.get(i);
            balanceViewModel.getAccountInfoByAddress(MainActivityManager.getInstance().mainActivity,bhWallet.address);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Account_request_code){
            List<BHWallet> walletList = BHUserManager.getInstance().getAllWallet();
            List<BHWalletItem> list = new ArrayList<>();
            StreamSupport.stream(walletList).forEach(item->{
                BHWalletItem bhWalletItem = BHWalletItem.makeBHWalletItem(item);
                list.add(bhWalletItem);
            });
            accountAdapter.setNewData(list);
        }
    }
}
