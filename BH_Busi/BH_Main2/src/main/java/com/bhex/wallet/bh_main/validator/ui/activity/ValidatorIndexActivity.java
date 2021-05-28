package com.bhex.wallet.bh_main.validator.ui.activity;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.lib.uikit.widget.CustomTextView;
import com.bhex.lib.uikit.widget.GradientTabLayout;
import com.bhex.lib.uikit.widget.viewpager.CustomViewPager;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.model.DelegateValidator;
import com.bhex.wallet.balance.presenter.AssetPresenter;
import com.bhex.wallet.balance.ui.fragment.AddressQRFragment;
import com.bhex.wallet.balance.ui.fragment.WithDrawShareFragment;
import com.bhex.wallet.balance.viewmodel.TransactionViewModel;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.R2;
import com.bhex.wallet.bh_main.validator.ui.fragment.ValidatorListFragment;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.helper.BHWalletHelper;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.MainActivityManager;
import com.bhex.wallet.common.tx.BHRawTransaction;
import com.bhex.wallet.common.tx.TransactionMsg;
import com.bhex.wallet.common.tx.TxReq;
import com.bhex.wallet.common.ui.fragment.Password30PFragment;
import com.bhex.wallet.common.utils.LiveDataBus;
import com.bhex.wallet.common.viewmodel.BalanceViewModel;
import com.google.android.material.tabs.TabLayout;
import com.gyf.immersionbar.ImmersionBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @author gongdongyang
 * 2020-9-2 15:50:31
 */
@Route(path = ARouterConfig.Validator.Validator_Index)
public class ValidatorIndexActivity extends BaseActivity<AssetPresenter> {

    BalanceViewModel mBalanceViewModel;
    TransactionViewModel mTransactionViewModel;

    @BindView(R2.id.tab)
    GradientTabLayout tab;
    @BindView(R2.id.viewPager)
    CustomViewPager viewPager;
    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;

    AppCompatTextView tv_available_text;
    AppCompatTextView tv_available_amount;
    AppCompatTextView tv_entrust_amount;
    AppCompatTextView tv_claimed_reward_amount;
    AppCompatTextView tv_unbonding_amount;
    AppCompatTextView tv_unclaimed_reward_amount;

    private BHWallet mBhWallet;

    List<Pair<String, Fragment>> items = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_validator_index;
    }

    @Override
    protected void initView() {
        mPresenter = new AssetPresenter(this);
        ImmersionBar.with(this)
                .transparentStatusBar().statusBarDarkFont(true)
                .navigationBarDarkIcon(true).fitsSystemWindows(false).init();

        tv_available_text = findViewById(R.id.tv_available_text);
        tv_available_amount = findViewById(R.id.tv_available_amount);
        tv_entrust_amount = findViewById(R.id.tv_entrust_amount);
        tv_claimed_reward_amount = findViewById(R.id.tv_claimed_reward_amount);
        tv_unbonding_amount = findViewById(R.id.tv_unbonding_amount);
        tv_unclaimed_reward_amount = findViewById(R.id.tv_unclaimed_reward_amount);

        initTab();
    }

    @Override
    protected void addEvent() {
        mBhWallet = BHUserManager.getInstance().getCurrentBhWallet();
        tv_center_title.setText(getResources().getString(R.string.entrust_relive_entrust));
        mBalanceViewModel = ViewModelProviders.of(MainActivityManager._instance.mainActivity).get(BalanceViewModel.class);

        LiveDataBus.getInstance().with(BHConstants.Label_Account, LoadDataModel.class).observe(this, ldm -> {
            updateAssest();
        });

        mTransactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);
        mTransactionViewModel.validatorLiveData.observe(this,ldm->{
            updateValidatorAddress(ldm);
        });

        updateAssest();

        tv_unclaimed_reward_amount.setOnClickListener(v -> {
            mTransactionViewModel.queryValidatorByAddress(this,1);
        });
    }


    private void updateAssest() {
        //可用数量
        if(BHUserManager.getInstance().getAccountInfo()==null){
            return;
        }

        tv_available_text.setText(getString(R.string.available)+BHConstants.BHT_TOKEN.toUpperCase());

        //可用
        String available_value = NumberUtil.dispalyForUsertokenAmount4Level(BHUserManager.getInstance().getAccountInfo().available );
        tv_available_amount.setText(available_value);

        //委托中
        String bonded_value = NumberUtil.dispalyForUsertokenAmount4Level(BHUserManager.getInstance().getAccountInfo().bonded);
        tv_entrust_amount.setText(bonded_value);

        //赎回中
        String unbonding_value = NumberUtil.dispalyForUsertokenAmount4Level(BHUserManager.getInstance().getAccountInfo().unbonding);
        tv_unbonding_amount.setText(unbonding_value);

        //已收益
        String claimed_reward_value = NumberUtil.dispalyForUsertokenAmount4Level(BHUserManager.getInstance().getAccountInfo().claimed_reward);
        tv_claimed_reward_amount.setText(claimed_reward_value);

        //领取收益
        String unclaimed_reward_value = NumberUtil.dispalyForUsertokenAmount4Level(BHUserManager.getInstance().getAccountInfo().unclaimed_reward);
        String v_unclaimed_reward_value = unclaimed_reward_value.concat(" ").concat(getString(R.string.unclaimed_reward));
        tv_unclaimed_reward_amount.setText(v_unclaimed_reward_value);

    }

    //初始化Tab
    private void initTab(){
        ValidatorListFragment validListFragment = new ValidatorListFragment(viewPager);
        Bundle bundle = new Bundle();
        bundle.putInt(ValidatorListFragment.KEY_VALIDATOR_TYPE, BH_BUSI_TYPE.托管节点.getIntValue());
        validListFragment.setArguments(bundle);

        ValidatorListFragment invalidListFragment = new ValidatorListFragment(viewPager);
        Bundle bundle1 = new Bundle();
        bundle1.putInt(ValidatorListFragment.KEY_VALIDATOR_TYPE, BH_BUSI_TYPE.共识节点.getIntValue());
        invalidListFragment.setArguments(bundle1);


        ValidatorListFragment competingListFragment = new ValidatorListFragment(viewPager);
        Bundle bundle2 = new Bundle();
        bundle2.putInt(ValidatorListFragment.KEY_VALIDATOR_TYPE, BH_BUSI_TYPE.竞争节点.getIntValue());
        competingListFragment.setArguments(bundle2);

        items.add(new Pair<String, Fragment>(getString(R.string.trusteeship_node), validListFragment));
        items.add(new Pair<String, Fragment>(getString(R.string.common_node), invalidListFragment));
        items.add(new Pair<String, Fragment>(getString(R.string.competing_node), competingListFragment));

        viewPager.setAdapter(new ValidatorVPAdapter(getSupportFragmentManager()));
        //tab.setupWithViewPager(viewPager);
        tab.setViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                viewPager.resetHeight(position+1);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private List<DelegateValidator> mRewardList;
    private void updateValidatorAddress(LoadDataModel ldm) {
        if(ldm.loadingStatus==LoadDataModel.SUCCESS){
            toDoWithdrawShare(ldm);
        }else if(ldm.loadingStatus==LoadDataModel.ERROR){
            ToastUtils.showToast(getResources().getString(com.bhex.wallet.balance.R.string.no_profit));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void toDoWithdrawShare(LoadDataModel ldm){
        List<DelegateValidator> dvList =  (List<DelegateValidator>)ldm.getData();
        //计算所有收益
        double all_reward = mPresenter.calAllReward(dvList);
        String def_all_reward = NumberUtil.dispalyForUsertokenAmount4Level(all_reward+"");
        if(Double.valueOf(def_all_reward)<=0){
            ToastUtils.showToast(getResources().getString(R.string.no_profit));
        }else {
            mRewardList = dvList;
            WithDrawShareFragment.showWithDrawShareFragment(getSupportFragmentManager(),
                    WithDrawShareFragment.class.getSimpleName(), itemListener,def_all_reward);
        }
    }


    Password30PFragment.PasswordClickListener withDrawPwdListener = (password, position,way) -> {
        List<TransactionMsg.ValidatorMsg> validatorMsgs = mPresenter.getAllValidator(mRewardList);
        List<TxReq.TxMsg> tx_msg_list = BHRawTransaction.createRewardMsg(validatorMsgs);
        mTransactionViewModel.transferInnerExt(this,password,BHUserManager.getInstance().getDefaultGasFee().displayFee,tx_msg_list);
    };

    //发送提取分红交易
    private WithDrawShareFragment.FragmentItemListener itemListener = (position -> {
        Password30PFragment.showPasswordDialog(getSupportFragmentManager(),Password30PFragment.class.getSimpleName(),
                withDrawPwdListener,1,true);
    });

    //Adapter
    private class ValidatorVPAdapter extends FragmentPagerAdapter{

        public ValidatorVPAdapter(@NonNull FragmentManager fm) {
            super(fm,FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

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

    }

}
