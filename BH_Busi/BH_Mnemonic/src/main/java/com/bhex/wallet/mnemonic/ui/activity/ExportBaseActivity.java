package com.bhex.wallet.mnemonic.ui.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bhex.lib.uikit.widget.GradientTabLayout;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.tools.constants.BHConstants;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
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
 * 2020-5-18 11:31:30
 * 备份Privatekey和Keystore
 */
public abstract class ExportBaseActivity extends BaseActivity {

    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;

    @BindView(R2.id.tab)
    GradientTabLayout tab;

    @BindView(R2.id.viewPager)
    ViewPager viewPager;

    @BindView(R2.id.btn_finish)
    MaterialButton btn_finish;

    @Override
    protected void initView() {
        //LogUtils.d("ExportBaseActivity==>:","flag=="+getFlag());
        List<Pair<String, Fragment>> items = new ArrayList<>();
        //文本导出
        ExportTextFragment exportTextFragment = new ExportTextFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ExportTextFragment.KEY_FLAG,getFlag());
        bundle.putString(BHConstants.INPUT_PASSWORD,getInputPwd());
        bundle.putString(BHConstants.WALLET_ADDRESS,getWalletAddress());
        exportTextFragment.setArguments(bundle);

        //二维码导出
        ExportQRFragment exportQRFragment = new ExportQRFragment();
        exportQRFragment.setArguments(bundle);
        String title = getFlag().equals(BH_BUSI_TYPE.备份私钥.value)?getString(R.string.privatekey):"Keystore";
        items.add(new Pair<>(title,exportTextFragment));
        items.add(new Pair<>(getString(R.string.qr_code),exportQRFragment));

        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return items.get(position).second;
            }

            @Override
            public int getCount() {
                return items.size();
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {//添加标题Tab
                return items.get(position).first;
            }
        });

        tab.setViewPager(viewPager);

        btn_finish.setOnClickListener(v -> {
            finish();
        });
    }

    protected abstract String getFlag();

    protected abstract String getInputPwd();

    protected abstract String getWalletAddress();
}
