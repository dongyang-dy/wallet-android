package com.bhex.wallet.bh_main.my.ui.activity;

/**
 * @au
 */
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.mvx.base.BaseDialogFragment;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.R2;
import com.bhex.wallet.bh_main.my.adapter.AccountFunctionAdapter;
import com.bhex.wallet.bh_main.my.helper.MyHelper;
import com.bhex.wallet.bh_main.my.ui.decoration.FunctionItemDecoration;
import com.bhex.wallet.bh_main.my.ui.fragment.DeleteTipFragment;
import com.bhex.wallet.bh_main.my.ui.fragment.UpdateName2Fragment;
import com.bhex.wallet.bh_main.my.ui.item.MyItem;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.ui.fragment.Password30PFragment;
import com.bhex.wallet.common.viewmodel.WalletViewModel;

import java.util.List;

import butterknife.BindView;

/**
 * @author gongdongyang
 * 账户详情
 * 2021-3-2 22:48:39
 */
@Route(path = ARouterConfig.My.ACCOUNT_DETAIL_PAGE, name = "账户详情")
public class AccountDetailActivity extends BaseActivity {

    @Autowired(name=BHConstants.WALLET_ADDRESS)
    String wallet_address;

    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;
    @BindView(R2.id.rcv_account_function)
    RecyclerView rcv_account_function;

    AccountFunctionAdapter accountAdapter;

    BHWallet chooseWallet;

    WalletViewModel walletViewModel;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_account_detail;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        tv_center_title.setText(getString(R.string.account_detail));
        chooseWallet = BHUserManager.getInstance().getBHWalletByAddress(wallet_address);
        List<MyItem> list = MyHelper.getAcountDetailFunction(this,chooseWallet);
        rcv_account_function.setAdapter(accountAdapter = new AccountFunctionAdapter(list,chooseWallet.name));

        FunctionItemDecoration itemDecoration = new FunctionItemDecoration(this,
                ColorUtil.getColor(this,R.color.global_divider_color),
                getResources().getDimension(R.dimen.item_line_divider_height));
        rcv_account_function.addItemDecoration(itemDecoration);
    }

    @Override
    protected void addEvent() {
        walletViewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
        walletViewModel.pwdVerifyLiveData.observe(this,ldm->{
            verifyPasswordStatus(ldm);
        });
        walletViewModel.deleteLiveData.observe(this,ldm->{
            deleteWalletStatus(ldm);
        });
        accountAdapter.setOnItemClickListener((adapter, view, position) -> {
            MyItem item = accountAdapter.getData().get(position);
            if(item.title.equals(getString(R.string.update_name))){
                UpdateName2Fragment.getInstance(chooseWallet,this::updateNameCallback).show(getSupportFragmentManager(),
                        UpdateName2Fragment.class.getName());
            }else if(item.title.equals(getString(R.string.reset_security_pwd))){
                ARouter.getInstance().build(ARouterConfig.My.My_Update_Password)
                        .withString(BHConstants.TITLE,item.title)
                        .withString(BHConstants.WALLET_ADDRESS,chooseWallet.getAddress())
                        .navigation();
            } else if(item.title.equals(getString(R.string.backup_mnemonic))){
                Password30PFragment fragment  = Password30PFragment.showPasswordChooseDialog(
                        getSupportFragmentManager(),this::passwordListener, BH_BUSI_TYPE.备份助记词.getIntValue(),wallet_address);
                fragment.show(getSupportFragmentManager(),Password30PFragment.class.getName());
            } else if(item.title.equals(getString(R.string.backup_privatekey))){
                Password30PFragment fragment  = Password30PFragment.showPasswordChooseDialog(
                        getSupportFragmentManager(),this::passwordListener, BH_BUSI_TYPE.备份私钥.getIntValue(),wallet_address);
                fragment.show(getSupportFragmentManager(),Password30PFragment.class.getName());
            }else if(item.title.equals(getString(R.string.backup_keystore))){
                Password30PFragment fragment  = Password30PFragment.showPasswordChooseDialog(
                        getSupportFragmentManager(),this::passwordListener, BH_BUSI_TYPE.备份KS.getIntValue(),wallet_address);
                fragment.show(getSupportFragmentManager(),Password30PFragment.class.getName());
            }else if(item.title.equals(getString(R.string.delete_wallet))){
                //判断是否是当前账户
                BHWallet currentWallet = BHUserManager.getInstance().getCurrentBhWallet();
                if(currentWallet.address.equals(chooseWallet.address)){
                    ToastUtils.showToast(getString(R.string.delete_wallet_tips));
                    return;
                }
                DeleteTipFragment.getInstance(this::deleteCallback).show(AccountDetailActivity.this.getSupportFragmentManager(),
                       DeleteTipFragment.class.getName());
            }

        });
    }

    //删除账户--回调
    private void deleteCallback(String inp_password) {
        walletViewModel.verifyKeystore(this,chooseWallet.keystorePath,inp_password,false,false);
    }

    //删除账户-验证密码状态
    private void verifyPasswordStatus(LoadDataModel ldm) {
        if(ldm.getLoadingStatus()==LoadDataModel.ERROR){
            ToastUtils.showToast(getString(R.string.error_password));
            return;
        }
        walletViewModel.deleteWallet(this,chooseWallet.id);
    }

    //删除账户--
    private void deleteWalletStatus(LoadDataModel ldm){
        if(ldm.getLoadingStatus()==LoadDataModel.SUCCESS){
            ToastUtils.showToast(getString(R.string.deleted));
            finish();
        }
    }

    //修改用户名称-回调
    private void updateNameCallback() {
        chooseWallet = BHUserManager.getInstance().getBHWalletByAddress(wallet_address);
        accountAdapter.setWalletName(chooseWallet.name);
        accountAdapter.notifyDataSetChanged();
    }

    //密码输入框回调---备份助记词、备份私钥、备份
    private void passwordListener(String password,int position,int way) {
        if(position== BH_BUSI_TYPE.备份助记词.getIntValue()){
            ARouter.getInstance().build(ARouterConfig.MNEMONIC_BACKUP)
                    .withString(BHConstants.INPUT_PASSWORD,password)
                    .withString(BHConstants.WALLET_ADDRESS,chooseWallet.address)
                    .withString("gotoTarget",AccountDetailActivity.class.getName())
                    .navigation();
        }else if(position==BH_BUSI_TYPE.备份私钥.getIntValue()){

            ARouter.getInstance().build(ARouterConfig.TRUSTEESHIP_EXPORT_INDEX)
                    .withString(BHConstants.TITLE,getString(R.string.backup_privatekey))
                    .withString(BHConstants.INPUT_PASSWORD,password)
                    .withString(BHConstants.WALLET_ADDRESS,chooseWallet.address)
                    .withString("flag", BH_BUSI_TYPE.备份私钥.value)
                    .navigation();
        }else if(position==BH_BUSI_TYPE.备份KS.getIntValue()){
            //提醒页
            ARouter.getInstance().build(ARouterConfig.TRUSTEESHIP_EXPORT_INDEX)
                    .withString(BHConstants.TITLE,getString(R.string.backup_keystore))
                    .withString(BHConstants.WALLET_ADDRESS,chooseWallet.address)
                    .withString(BHConstants.INPUT_PASSWORD,password)
                    .withString("flag",BH_BUSI_TYPE.备份KS.value)
                    .navigation();
        }
    }

}