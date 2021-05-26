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

    private List<MyItem> mDatas;
    private AboutusAdapter aboutusAdapter;
    private RecyclerView rec_function;

    private UpgradeViewModel mUpgradeVM;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected void initView() {
        //标题
        AppCompatTextView tv_center_title = findViewById(R.id.tv_center_title);
        tv_center_title.setText(getString(R.string.about_us));
        //当前版本
        AppCompatTextView tv_version = findViewById(R.id.tv_version);
        String v_version = "v"+ PackageUtils.getVersionName(this)+"("+PackageUtils.getVersionCode(this)+")";
        tv_version.setText(v_version);

        rec_function = findViewById(R.id.rec_function);
        //
        mDatas = MyHelper.getAboutUs(this);
        rec_function.setAdapter(aboutusAdapter = new AboutusAdapter(mDatas));
        aboutusAdapter.setOnItemClickListener(this::clickItemAction);

        RecycleViewExtDivider itemDecoration = new RecycleViewExtDivider(
                this, LinearLayoutManager.VERTICAL,
                (int)getResources().getDimension(R.dimen.main_padding_left),0,
                ColorUtil.getColor(this,R.color.global_divider_color));
        rec_function.addItemDecoration(itemDecoration);
    }

    @Override
    protected void addEvent() {
        mUpgradeVM = ViewModelProviders.of(this).get(UpgradeViewModel.class);
        mUpgradeVM.upgradeLiveData.observe(this,ldm->{
            processUpgradeInfo(ldm);
        });

        mUpgradeVM.getUpgradeInfo(this);
    }

    boolean isCheckUpdate = false;

    //点击条目
    private void clickItemAction(BaseQuickAdapter<?,?> baseQuickAdapter, View view, int i) {
        switch (i){
            /*case 0:
                ARouter.getInstance().build(ARouterConfig.Market.market_webview)
                        .withString("url", BH_BUSI_URL.版本更新日志.getGotoUrl(this)).navigation();
                break;*/
            case 0:
                isCheckUpdate = true;
                mUpgradeVM.getUpgradeInfo(this);
                break;
            case 1:
                /*ARouter.getInstance().build(ARouterConfig.Market.market_webview)
                    .withString("url", BH_BUSI_URL.联系我们.getGotoUrl(this)).navigation();*/
                ToolUtils.copyText(BHConstants.EMAIL,this);
                ToastUtils.showToast(getString(R.string.copyed));
                break;
        }
    }

    private void processUpgradeInfo(LoadDataModel<UpgradeInfo> ldm) {
        MyItem myItem = mDatas.get(0);
        if(ldm.loadingStatus== LoadingStatus.SUCCESS){
            UpgradeInfo upgradeInfo = ldm.getData();
            if(!isCheckUpdate ){
                String v_last_version = getString(R.string.latest_version)+(upgradeInfo.apkVersion);
                myItem.rightTxt = (upgradeInfo.needUpdate==1)?(v_last_version):getString(R.string.app_up_to_minute_version);
                aboutusAdapter.notifyDataSetChanged();
            }else{
                if(upgradeInfo.needUpdate==1){
                    showUpgradeDailog(upgradeInfo);
                }else{
                    ToastUtils.showToast(getString(R.string.app_up_to_minute_version));
                }
            }
        }else if(ldm.loadingStatus== LoadingStatus.ERROR){
            myItem.rightTxt =  getString(R.string.app_up_to_minute_version);
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

    //
    public class AboutusAdapter extends BaseQuickAdapter<MyItem, BaseViewHolder>{

        public AboutusAdapter( @Nullable List<MyItem> data) {
            super(R.layout.item_my, data);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder viewHolder, MyItem myItem) {
            viewHolder.setText(R.id.tv_title,myItem.title);
            if(!TextUtils.isEmpty(myItem.rightTxt)){
                viewHolder.setText(R.id.tv_right_txt,myItem.rightTxt);
                viewHolder.setVisible(R.id.tv_right_txt, true);
            }
        }
    }
}