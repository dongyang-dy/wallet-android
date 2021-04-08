package com.bhex.wallet.mnemonic.ui.activity;


import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.GradientTabLayout;
import com.bhex.tools.constants.BHConstants;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.mnemonic.R;
import com.bhex.wallet.mnemonic.R2;
import com.bhex.wallet.mnemonic.ui.fragment.ExportQRFragment;
import com.bhex.wallet.mnemonic.ui.fragment.ExportTextFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @author gongdongyang
 * 导出私钥
 * 2020-5-16 16:42:35
 */
@Route(path = ARouterConfig.TRUSTEESHIP_EXPORT_PRIVATEKEY)
public class ExportPrivateKeyActivity extends ExportBaseActivity {

    @Autowired(name=BHConstants.TITLE)
    String title;

    @Autowired(name=BHConstants.FLAG)
    String flag;

    @Autowired(name="inputPwd")
    String inputPwd;

    @Autowired(name = BHConstants.WALLET_ADDRESS)
    String wallet_address;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_export_private_key;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        super.initView();
        tv_center_title.setText(title);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    protected void addEvent() {

    }

    @Override
    protected String getFlag() {
        return flag;
    }

    @Override
    protected String getInputPwd() {
        return inputPwd;
    }

    @Override
    protected String getWalletAddress() {
        return wallet_address;
    }
}
