package com.bhex.wallet.bh_main.my.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.RecycleViewExtDivider;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.network.utils.PackageUtils;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.language.LocalManageUtil;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.my.helper.MyHelper;
import com.bhex.wallet.bh_main.my.ui.item.MyItem;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.BH_BUSI_URL;
import com.bhex.wallet.common.model.UpgradeInfo;
import com.bhex.wallet.common.ui.fragment.UpgradeFragment;
import com.bhex.wallet.common.viewmodel.UpgradeViewModel;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.gyf.immersionbar.ImmersionBar;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * @author gongdongyang
 * 2020-12-28 00:01:29
 */
@Route(path = ARouterConfig.My.My_About,name = "关于我们")
public class AboutActivity extends BaseActivity {

    private UpgradeViewModel mUpgradeVM;
    private AppCompatTextView tv_version_update;
    private AppCompatTextView tv_contact_us;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected void initView() {

        ImmersionBar.with(this).statusBarColor(R.color.page_bg_color)
                .statusBarDarkFont(true)
                .barColor(R.color.page_bg_color)
                .fitsSystemWindows(true).init();

        //标题
        AppCompatTextView tv_center_title = findViewById(R.id.tv_center_title);
        tv_center_title.setText(getString(R.string.about_us));
        //当前版本
        AppCompatTextView tv_version = findViewById(R.id.tv_version);
        String v_version = "v"+ PackageUtils.getVersionName(this)+"("+PackageUtils.getVersionCode(this)+")";
        tv_version.setText(v_version);

        tv_contact_us = findViewById(R.id.tv_contact_us);
        tv_contact_us.setText(BHConstants.EMAIL);

        tv_version_update = findViewById(R.id.tv_version_update);

    }

    @Override
    protected void addEvent() {
        mUpgradeVM = ViewModelProviders.of(this).get(UpgradeViewModel.class);
        mUpgradeVM.upgradeLiveData.observe(this,ldm->{
            processUpgradeInfo(ldm);
        });

        mUpgradeVM.getUpgradeInfo(this,false);

        //复制联系我们
        tv_contact_us.setOnClickListener(v -> {
            ToolUtils.copyText(BHConstants.EMAIL,this);
            ToastUtils.showToast(getString(R.string.copyed));
        });

        //点击更新版本检测
        tv_version_update.setOnClickListener(v -> {
            isCheckUpdate = true;
            mUpgradeVM.getUpgradeInfo(this,true);
        });
    }

    boolean isCheckUpdate = false;



    private void processUpgradeInfo(LoadDataModel<UpgradeInfo> ldm) {
        if(ldm.loadingStatus== LoadingStatus.SUCCESS){
            UpgradeInfo upgradeInfo = ldm.getData();
            if(!isCheckUpdate){
                String v_last_version = getString(R.string.latest_version)+(upgradeInfo.apkVersion);
                String v_version_update = (upgradeInfo.needUpdate==1)?(v_last_version):getString(R.string.app_up_to_minute_version);
                tv_version_update.setText(v_version_update);
            }else{
                if(upgradeInfo.needUpdate==1){
                    showUpgradeDailog(upgradeInfo);
                }else{
                    ToastUtils.showToast(getString(R.string.app_up_to_minute_version));
                }
            }
        }else if(ldm.loadingStatus== LoadingStatus.ERROR){
            tv_version_update.setText(getString(R.string.app_up_to_minute_version));
        }
    }


    private void showUpgradeDailog(UpgradeInfo upgradeInfo){
        //是否强制升级
        UpgradeFragment fragment = UpgradeFragment.Companion.showUpgradeDialog(upgradeInfo,upgradeDialogListener);
        fragment.show(this.getSupportFragmentManager(),UpgradeFragment.class.getName());
    }

    UpgradeFragment.DialogOnClickListener upgradeDialogListener = v -> {
        ToastUtils.showToast(this.getResources().getString(R.string.app_loading_now));
    };


}