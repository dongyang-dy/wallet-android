package com.bhex.wallet.mnemonic.ui.activity;

import android.text.InputType;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.InputView;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.common.base.BaseCacheActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.helper.BHWalletHelper;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.SecuritySettingManager;
import com.bhex.wallet.common.model.BHWalletItem;
import com.bhex.wallet.common.utils.ARouterUtil;
import com.bhex.wallet.common.viewmodel.WalletViewModel;
import com.bhex.wallet.mnemonic.R;
import com.bhex.wallet.mnemonic.R2;
import com.bhex.wallet.mnemonic.persenter.LoginPresenter;
import com.bhex.wallet.mnemonic.ui.fragment.AddressFragment;
import com.google.android.material.button.MaterialButton;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 登录验证界面
 *
 * @author gongdongyang
 * 2020-3-12
 */
@Route(path = ARouterConfig.Account.Account_Login_Password, name="密码登录")
public class LockActivity extends BaseCacheActivity<LoginPresenter> implements AddressFragment.AddressChangeListener {

    @BindView(R2.id.tv_bh_address)
    AppCompatTextView tv_bh_address;

    @BindView(R2.id.inp_wallet_pwd)
    InputView inp_wallet_pwd;

    @BindView(R2.id.btn_confirm)
    AppCompatButton btn_confirm;

    @BindView(R2.id.tv_import_mnemonic)
    AppCompatTextView tv_import_mnemonic;

    @BindView(R2.id.iv_username)
    AppCompatTextView iv_username;

    @BindView(R2.id.tv_forget_pwd)
    AppCompatTextView tv_forget_pwd;
    @BindView(R2.id.btn_wallet_create)
    MaterialButton btn_wallet_create;
    @BindView(R2.id.btn_wallet_impot)
    MaterialButton btn_wallet_impot;

    //当前钱包
    private BHWallet mCurrentWallet;

    private WalletViewModel walletVM;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_lock;
    }

    @Override
    protected void initPresenter() {
        mPresenter = new LoginPresenter(this);
    }

    @Override
    protected void initView() {
        //inp_wallet_pwd.getEditText().setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        mCurrentWallet = BHUserManager.getInstance().getCurrentBhWallet();
        tv_bh_address.setText(mCurrentWallet.getAddress());
        iv_username.setText(mCurrentWallet.getName());
        BHWalletHelper.proccessAddress(tv_bh_address, mCurrentWallet.getAddress());
        //BHKey.test();
    }

    @Override
    protected void addEvent() {
        SecuritySettingManager.getInstance().initSecuritySetting();

        walletVM = ViewModelProviders.of(this).get(WalletViewModel.class);
        walletVM.mutableLiveData.observe(this, ldm -> {
            if (ldm.loadingStatus == LoadingStatus.SUCCESS) {
                //ToastUtils.showToast("==loadingStatus==" + loadDataModel.loadingStatus);
            }
        });
        walletVM.pwdVerifyLiveData.observe(this, ldm -> {
            passwordVerify(ldm);
        });

    }


    @OnClick({R2.id.btn_confirm, R2.id.tv_import_mnemonic, R2.id.tv_forget_pwd, R2.id.tv_bh_address,
            R2.id.btn_wallet_create, R2.id.btn_wallet_impot})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.btn_confirm) {
            ToolUtils.hintKeyBoard(this);
            walletVM.verifyKeystore(this,BHUserManager.getInstance().getCurrentBhWallet().getKeystorePath(),inp_wallet_pwd.getInputString(),true,true);

        } else if (view.getId() == R.id.tv_import_mnemonic) {
            ARouter.getInstance().build(ARouterConfig.TRUSTEESHIP_IMPORT_INDEX).navigation();
        } else if (view.getId() == R.id.tv_forget_pwd) {
            //ARouterUtil.startActivity(ARouterConfig.TRUSTEESHIP_IMPORT_INDEX);
            ARouter.getInstance().build(ARouterConfig.TRUSTEESHIP_IMPORT_INDEX)
                    .navigation();
        } else if (view.getId() == R.id.tv_bh_address) {
            AddressFragment fragment = new AddressFragment();
            fragment.setChangeListener(this);
            fragment.show(getSupportFragmentManager(), "");
        } else if(view.getId() == R.id.btn_wallet_create){
            ARouterUtil.startActivity(ARouterConfig.TRUSTEESHIP_MNEMONIC_FRIST);
        } else if(view.getId() == R.id.btn_wallet_impot){
            ARouter.getInstance().build(ARouterConfig.TRUSTEESHIP_IMPORT_INDEX).navigation();
        }
    }


    @Override
    public void onItemClick(int position) {
        BHWallet bhWallet = BHUserManager.getInstance().getAllWallet().get(position);
        if(bhWallet.id!=mCurrentWallet.id){
            mCurrentWallet = bhWallet;
            BHUserManager.getInstance().setCurrentBhWallet(mCurrentWallet);
            walletVM.updateWallet(this, mCurrentWallet, mCurrentWallet.id, BHWalletItem.SELECTED,false);
            BHWalletHelper.proccessAddress(tv_bh_address, mCurrentWallet.getAddress());
            iv_username.setText(mCurrentWallet.getName());
        }
    }

    @Override
    protected boolean isShowBacking() {
        return false;
    }

    //密码校验
    private void passwordVerify(LoadDataModel ldm) {
        if(ldm.getLoadingStatus()== LoadingStatus.SUCCESS){
            ARouter.getInstance().build(ARouterConfig.Main.main_mainindex).navigation();
            finish();
        }else{
            //ToastUtils.showToast(getString(R.string.error_password));

        }
    }
}
