package com.bhex.wallet.bh_main.validator.ui.activity;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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

    @BindView(R2.id.tv_token_address)
    AppCompatTextView tv_token_address;

    @BindView(R2.id.tv_wallet_name)
    TextView tv_wallet_name;
    @BindView(R2.id.tv_available_label)
    TextView tv_available_label;
    @BindView(R2.id.tv_available_amount)
    CustomTextView tv_available_amount;
    @BindView(R2.id.tv_entrust_amount)
    CustomTextView tv_entrust_amount;
    @BindView(R2.id.tv_unbonding_amount)
    CustomTextView tv_unbonding_amount;
    @BindView(R2.id.tv_claimed_reward_amount)
    CustomTextView tv_claimed_reward_amount;
    @BindView(R2.id.tv_unclaimed_reward_amount)
    CustomTextView tv_unclaimed_reward_amount;
    @BindView(R2.id.btn_unclaimed_reward)
    TextView btn_unclaimed_reward;
    @BindView(R2.id.iv_qr_code)
    ImageView iv_qr_code;

    //private BHToken mSysmbolToken;//hbc
    private BHWallet mBhWallet;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_validator_index;
    }

    @Override
    protected void initPresenter() {
        mPresenter = new AssetPresenter(this);
    }

    @Override
    protected void initView() {
        ImmersionBar.with(this)
                .transparentStatusBar()
                .statusBarDarkFont(true)
                .navigationBarDarkIcon(true)
                .fitsSystemWindows(false).init();

        mBhWallet = BHUserManager.getInstance().getCurrentBhWallet();

        tv_center_title.setText(getResources().getString(R.string.delegate));
        tv_wallet_name.setText(mBhWallet.name);
        tv_available_label.setText(getString(R.string.available)+BHConstants.BHT_TOKEN.toUpperCase());
        tv_token_address.setTag(mBhWallet.getAddress());


        BHWalletHelper.proccessAddress(tv_token_address,mBhWallet.address);
        //设置背景
        LinearLayout btn_token_address =  findViewById(R.id.btn_token_address);
        GradientDrawable drawable = ShapeUtils.getRoundRectDrawable(PixelUtils.dp2px(this,50),
                ContextCompat.getColor(this,R.color.color_340A1825));
        btn_token_address.setBackground(drawable);
        btn_token_address.setOnClickListener(this::showAddressQR);

        //
        Drawable qr_drawable = ColorUtil.getDrawable(this,R.mipmap.ic_entrust_qr,R.color.white);
        iv_qr_code.setImageDrawable(qr_drawable);
        initTab();
    }

    @Override
    protected void addEvent() {
        mBalanceViewModel = ViewModelProviders.of(MainActivityManager._instance.mainActivity).get(BalanceViewModel.class);
        LiveDataBus.getInstance().with(BHConstants.Label_Account, LoadDataModel.class).observe(this, ldm -> {
            updateAssest(ldm);
        });
        mBalanceViewModel.getAccountInfo(ValidatorIndexActivity.this, null);

        mTransactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);
        mTransactionViewModel.validatorLiveData.observe(this,ldm->{
            updateValidatorAddress(ldm);
        });

        //
        GradientDrawable drawable = ShapeUtils.getRoundRectDrawable(PixelUtils.dp2px(this,20),ColorUtil.getColor(this,R.color.highlight_text_color));
        btn_unclaimed_reward.setBackground(drawable);
        btn_unclaimed_reward.setOnClickListener(v -> {
            mTransactionViewModel.queryValidatorByAddress(this,1);
        });
    }

    private void updateAssest(LoadDataModel ldm) {
        //可用数量
        if(BHUserManager.getInstance().getAccountInfo()==null){
            return;
        }
        String available_value = NumberUtil.dispalyForUsertokenAmount4Level(BHUserManager.getInstance().getAccountInfo() .available );
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
        tv_unclaimed_reward_amount.setText(unclaimed_reward_value);

    }

    private void initTab() {
        List<Pair<String, Fragment>> items = new ArrayList<>();
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

        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return items.size();
            }

            @Override
            public Fragment getItem(int position) {
                return items.get(position).second;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {//添加标题Tab
                return items.get(position).first;
            }


        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //LogUtils.d("ValidatorIndexActivity==","==position=="+position);
                viewPager.resetHeight(position+1);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

        });

        tab.setViewPager(viewPager);

        /*LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)viewPager.getLayoutParams();
        params.height = PixelUtils.dp2px(this,1500);*/
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


            }

            @Override
            public void onPageSelected(int position) {
                //((CustomViewPager)viewPager).resetHeight(position);
                //viewPager.resetHeight(position);

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

    public void toDoWithdrawShare(LoadDataModel ldm){
        List<DelegateValidator> dvList =  (List<DelegateValidator>)ldm.getData();
        //计算所有收益
        double all_reward = mPresenter.calAllReward(dvList);
        String def_all_reward = NumberUtil.dispalyForUsertokenAmount4Level(all_reward+"");
        if(Double.valueOf(def_all_reward)<=0){
            ToastUtils.showToast(getResources().getString(com.bhex.wallet.balance.R.string.no_profit));
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

    //地址二维码
    private void showAddressQR(View view) {
        AddressQRFragment.showFragment(getSupportFragmentManager(),
                AddressQRFragment.class.getSimpleName(),
                BHConstants.BHT_TOKEN,
                tv_token_address.getTag().toString());
    }

    protected  int getStatusColorValue(){
        return BHConstants.STATUS_COLOR_TRANS;
    }

}
