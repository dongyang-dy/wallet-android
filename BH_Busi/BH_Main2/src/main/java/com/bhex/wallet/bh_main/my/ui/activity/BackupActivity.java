package com.bhex.wallet.bh_main.my.ui.activity;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.tools.constants.BHConstants;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.R2;
import com.bhex.wallet.bh_main.my.helper.MyHelper;
import com.bhex.wallet.bh_main.my.ui.item.MyItem;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.ui.fragment.Password30PFragment;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import butterknife.BindView;

/**
 * @author gongdongyang
 * 导出备份
 * 2021-3-5 18:03:11
 */
@Route(path = ARouterConfig.My.My_Account_Backup,name="账户备份")
public class BackupActivity extends BaseActivity {

    @BindView(R2.id.rcv_function)
    RecyclerView rcv_function;
    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;

    private BackupAdapter adapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_backup;
    }

    @Override
    protected void initView() {
        tv_center_title.setText(getString(R.string.backup_export));
        List<MyItem> list = MyHelper.getBackupList(this);
        rcv_function.setAdapter(adapter = new BackupAdapter(list));
    }

    @Override
    protected void addEvent() {
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            switch (position){
                case 0:
                    Password30PFragment.showPasswordDialog(getSupportFragmentManager(),Password30PFragment.class.getName(),
                            this::passwordListener,BH_BUSI_TYPE.备份助记词.getIntValue(),false);
                    break;
                case 1:
                    Password30PFragment.showPasswordDialog(getSupportFragmentManager(),Password30PFragment.class.getName(),
                            this::passwordListener,BH_BUSI_TYPE.备份KS.getIntValue(),false);
                    break;
                case 2:
                    Password30PFragment.showPasswordDialog(getSupportFragmentManager(),Password30PFragment.class.getName(),
                            this::passwordListener,BH_BUSI_TYPE.备份私钥.getIntValue(),false);
                    break;
            }
        });
    }

    class BackupAdapter extends BaseQuickAdapter<MyItem, BaseViewHolder>{
        public BackupAdapter(@Nullable List<MyItem> data) {
            super(R.layout.item_my, data);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, MyItem myItem) {
            holder.setText(R.id.tv_title,myItem.title);

        }
    }


    private void passwordListener(String password,int position,int way) {
        BHWallet wallet = BHUserManager.getInstance().getCurrentBhWallet();

        if(position== BH_BUSI_TYPE.备份助记词.getIntValue()){
            ARouter.getInstance().build(ARouterConfig.MNEMONIC_BACKUP)
                    .withString(BHConstants.INPUT_PASSWORD,password)
                    .withString(BHConstants.WALLET_ADDRESS,wallet.address)
                    .withString("gotoTarget",BackupActivity.class.getName())
                    .navigation();
        }else if(position==BH_BUSI_TYPE.备份私钥.getIntValue()){

            ARouter.getInstance().build(ARouterConfig.TRUSTEESHIP_EXPORT_INDEX)
                    .withString("title",getString(R.string.backup_privatekey))
                    .withString(BHConstants.INPUT_PASSWORD,password)
                    .withString(BHConstants.WALLET_ADDRESS,wallet.address)
                    .withString("flag", BH_BUSI_TYPE.备份私钥.value)
                    .navigation();
        }else if(position==BH_BUSI_TYPE.备份KS.getIntValue()){
            //提醒页
            ARouter.getInstance().build(ARouterConfig.TRUSTEESHIP_EXPORT_INDEX)
                    .withString("title",getString(R.string.backup_keystore))
                    .withString(BHConstants.WALLET_ADDRESS,wallet.address)
                    .withString(BHConstants.INPUT_PASSWORD,password)
                    .withString("flag",BH_BUSI_TYPE.备份KS.value)
                    .navigation();
        }
    }
}