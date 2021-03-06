package com.bhex.wallet.mnemonic.ui.activity;

import android.view.WindowManager;

import androidx.appcompat.widget.AppCompatTextView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.tools.constants.BHConstants;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.mnemonic.R;
import com.bhex.wallet.mnemonic.R2;
import com.bhex.wallet.mnemonic.ui.fragment.ScreenShotTipsFragment;
import com.google.android.material.button.MaterialButton;

import butterknife.BindView;

/**
 * @author gongdongyang
 * 2020-5-16 19:11:34
 * 私钥导出提醒
 */
@Route(path = ARouterConfig.TRUSTEESHIP_EXPORT_INDEX, name = "私钥导出提醒")
public class ExportIndexActivity extends BaseActivity {

    @Autowired(name = "title")
    String title;

    @Autowired(name = "flag")
    String flag;

    @Autowired(name = BHConstants.WALLET_ADDRESS)
    String wallet_address;

    @Autowired(name=BHConstants.INPUT_PASSWORD)
    String inputPwd;

    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;

    @BindView(R2.id.backup_tip_1)
    AppCompatTextView backup_tip_1;
    @BindView(R2.id.backup_tip_2)
    AppCompatTextView backup_tip_2;
    @BindView(R2.id.backup_tip_3)
    AppCompatTextView backup_tip_3;

    @BindView(R2.id.btn_next)
    MaterialButton btn_next;



    @Override
    protected int getLayoutId() {
        return R.layout.activity_export_index;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        tv_center_title.setText(title);
        if(flag.endsWith(BH_BUSI_TYPE.备份KS.value)){
            backup_tip_1.setText(getString(R.string.backup_ks_tip_1));
            backup_tip_2.setText(getString(R.string.backup_ks_tip_2));
            backup_tip_3.setText(getString(R.string.backup_ks_tip_3));
        }
    }

    @Override
    protected void addEvent() {
        btn_next.setOnClickListener(v -> {
            ScreenShotTipsFragment fragment = ScreenShotTipsFragment.showDialog(getSupportFragmentManager(),
                    ScreenShotTipsFragment.class.getSimpleName());

            fragment.setIKnowListener(knowListener);
        });
    }

    private ScreenShotTipsFragment.IKnowListener knowListener = ()->{
        if(flag.equals(BH_BUSI_TYPE.备份私钥.value)){
            ARouter.getInstance().build(ARouterConfig.TRUSTEESHIP_EXPORT_PRIVATEKEY)
                    .withString("title",title)
                    .withString("flag",flag)
                    .withString(BHConstants.WALLET_ADDRESS,wallet_address)
                    .withString(BHConstants.INPUT_PASSWORD,inputPwd)
                    .navigation();
        }else{
            ARouter.getInstance().build(ARouterConfig.TRUSTEESHIP_EXPORT_KEYSTORE)
                    .withString("title",title)
                    .withString("flag",flag)
                    .withString(BHConstants.WALLET_ADDRESS,wallet_address)
                    .withString(BHConstants.INPUT_PASSWORD,inputPwd)
                    .navigation();
        }
    };


}
