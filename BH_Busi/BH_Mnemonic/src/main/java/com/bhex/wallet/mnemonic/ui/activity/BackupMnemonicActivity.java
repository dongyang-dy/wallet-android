package com.bhex.wallet.mnemonic.ui.activity;


import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.NavigateUtil;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.common.ActivityCache;
import com.bhex.wallet.common.base.BaseCacheActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.event.AccountEvent;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.mnemonic.R;
import com.bhex.wallet.mnemonic.R2;
import com.bhex.wallet.mnemonic.adapter.MnemonicAdapter;
import com.bhex.wallet.mnemonic.helper.MnemonicDataHelper;
import com.bhex.wallet.mnemonic.ui.item.MnemonicItem;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * gdy
 * 2020-3-4 21:09:06
 */
@Route(path = ARouterConfig.MNEMONIC_BACKUP)
public class BackupMnemonicActivity extends BaseCacheActivity {

    @BindView(R2.id.recycler_mnemonic)
    RecyclerView recycler_mnemonic;

    @BindView(R2.id.btn_start_verify)
    AppCompatButton btn_start_verify;

    @Autowired(name=BHConstants.INPUT_PASSWORD)
    String inputPwd;

    @Autowired(name=BHConstants.WALLET_ADDRESS)
    String wallet_address;

    @Autowired(name = BHConstants.GOTO_TARGET)
    String mGotoTarget;

    private List<MnemonicItem> mnemonicItemList;

    private MnemonicAdapter mnemonicAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_backup_mnemonic;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    protected void addEvent() {
        mnemonicItemList = MnemonicDataHelper.makeMnemonic(inputPwd,wallet_address);

        mnemonicAdapter = new MnemonicAdapter(mnemonicItemList);

        GridLayoutManager layoutManager = new GridLayoutManager(this,3);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recycler_mnemonic.setLayoutManager(layoutManager);
        recycler_mnemonic.setAdapter(mnemonicAdapter);

        if(ToolUtils.checkListIsEmpty(mnemonicItemList)){
            btn_start_verify.setEnabled(false);
        }
    }

    @OnClick({R2.id.btn_start_verify})
    public void onViewClicked(View view) {
        if(view.getId()== R.id.btn_start_verify){
            ARouter.getInstance().build(ARouterConfig.MNEMONIC_VERIFY);
            Postcard postcard = ARouter.getInstance().build(ARouterConfig.MNEMONIC_VERIFY);
            postcard.withString(BHConstants.INPUT_PASSWORD,inputPwd);
            postcard.withString(BHConstants.WALLET_ADDRESS,wallet_address);
            if(!TextUtils.isEmpty(mGotoTarget)){
                postcard.withString(BHConstants.GOTO_TARGET,mGotoTarget);
            }
            postcard.navigation();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(TextUtils.isEmpty(mGotoTarget)){
            NavigateUtil.startMainActivity(this,
                    new String[]{BHConstants.BACKUP_TEXT, BHConstants.LATER_BACKUP});
            ActivityCache.getInstance().finishActivity();
            EventBus.getDefault().post(new AccountEvent());
        }else{
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(TextUtils.isEmpty(mGotoTarget)){
            NavigateUtil.startMainActivity(this,
                    new String[]{BHConstants.BACKUP_TEXT, BHConstants.LATER_BACKUP});
            ActivityCache.getInstance().finishActivity();
            EventBus.getDefault().post(new AccountEvent());
        }else{
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


}
