package com.bhex.wallet.bh_main.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.RefreshLayoutManager;
import com.bhex.lib.uikit.util.BottomNavigationViewUtil;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.R;
import com.bhex.wallet.balance.ui.fragment.BalanceFragment;
import com.bhex.wallet.bh_main.persenter.MainPresenter;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.TRANSCATION_BUSI_TYPE;
import com.bhex.wallet.common.event.AccountEvent;
import com.bhex.wallet.common.event.LanguageEvent;
import com.bhex.wallet.common.event.NightEvent;
import com.bhex.wallet.common.manager.AppStatusManager;
import com.bhex.wallet.common.manager.MainActivityManager;
import com.bhex.wallet.common.manager.SequenceManager;
import com.bhex.wallet.common.ui.activity.BHQrScanActivity;
import com.bhex.wallet.common.viewmodel.BalanceViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.gyf.immersionbar.ImmersionBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;

/**
 * created by gongdongyang
 * on 2020/2/24
 */

@Route(path= ARouterConfig.Main.main_mainindex)
public class MainActivity extends BaseActivity<MainPresenter> {

    @BindView(R.id.main_bottom)
    BottomNavigationView mBottomNavigationView;

    private long mExitTime = 0L;
    private int mCurrentCheckId = 0;
    //是否复位
    public static boolean isReset = true;

    private BalanceViewModel balanceViewModel;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        MainActivityManager._instance.mainActivity = this;
        SequenceManager.getInstance().initSequence();
        RefreshLayoutManager.init();
        TRANSCATION_BUSI_TYPE.init(this);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState!=null && !isReset){
            mCurrentCheckId = savedInstanceState.getInt("index",0);
        }
        //LogUtils.d("MainActivity===>:","==onRestoreInstanceState==mCurrentCheckId=="+mCurrentCheckId+"=isReset="+isReset);
        mBottomNavigationView.setSelectedItemId(mBottomNavigationView.getMenu().getItem(mCurrentCheckId).getItemId());
    }



    @Override
    protected void addEvent() {
        balanceViewModel = ViewModelProviders.of(MainActivity.this).get(BalanceViewModel.class).build(MainActivity.this);
        getLifecycle().addObserver(balanceViewModel);

        EventBus.getDefault().register(this);
        mBottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.tab_balance:
                    mCurrentCheckId = 0;
                    getPresenter().goBalanceFragment();
                    return true;
                case R.id.tab_validator:
                    //mCurrentCheckId = 1;
                    ARouter.getInstance().build(ARouterConfig.Market.market_hdex).navigation();
                    //getPresenter().goMarketFragment();
                    return false;
                /*case R.id.tab_proposals:
                    mCurrentCheckId = 2;
                    getPresenter().goProposalFragment();
                    return true;*/
                case R.id.tab_my:
                    mCurrentCheckId = 2;
                    getPresenter().goMyFragment();
                    return true;
            }
            return false;
        });
        mBottomNavigationView.setSelectedItemId(mBottomNavigationView.getMenu().getItem(0).getItemId());
        BottomNavigationViewUtil.hideToast(mBottomNavigationView);
        //SequenceManager.getInstance().timerTranscation(this);
    }

    @Override
    protected void initPresenter() {
        mPresenter = new MainPresenter(this);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(System.currentTimeMillis()-this.mExitTime>2000L){
            ToastUtils.showToast(getString(R.string.exit_app));
            this.mExitTime = System.currentTimeMillis();
            return;
        }
        MainActivityManager.getInstance().mainActivity = null;
        finish();
        System.exit(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode= ThreadMode.MAIN)
    public void changeLanguage(LanguageEvent language){
        isReset = false;
        recreate();
    }

    @Subscribe(threadMode= ThreadMode.MAIN)
    public void changeAccount(AccountEvent accountEvent){
        /*mBottomNavigationView.setSelectedItemId(mBottomNavigationView.getMenu().getItem(0).getItemId());
        getPresenter().showIsBackup();
        SequenceManager.getInstance().initSequence();
        getWindow().setWindowAnimations(R.style.WindowAnimationFadeInOut);
        startActivity(new Intent(this,MainActivity.class));
        isReset = true;*/
        recreate();
        isReset = true;

    }

    @Subscribe(threadMode= ThreadMode.MAIN)
    public void changeModel(NightEvent nightEvent){
        isReset = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int action = intent.getIntExtra(AppStatusManager.KEY_HOME_ACTION, AppStatusManager.ACTION_BACK_TO_HOME);
        if(action==AppStatusManager.ACTION_RESTART_APP){
            ARouter.getInstance().build(ARouterConfig.Main.main_splash).navigation();
            finish();
        }
        String go_position = intent.getStringExtra("go_position");
        String go_token  = intent.getStringExtra("go_token");
        getPresenter().gotoTarget(mBottomNavigationView,go_position,go_token);
    }

    @Override
    protected void setStatusColor() {
        if(!isNight()){
            ImmersionBar.with(this).transparentStatusBar().statusBarDarkFont(true).navigationBarDarkIcon(true).init();
        }else{
            ImmersionBar.with(this).transparentStatusBar().statusBarDarkFont(false).navigationBarDarkIcon(false).init();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("index",mCurrentCheckId);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //LogUtils.d("BalanceFragment====>:","==onActivityResult==");

        FragmentManager manager = getSupportFragmentManager();
        Fragment dstFragment = manager.findFragmentByTag(BalanceFragment.class.getSimpleName());
        dstFragment.onActivityResult(requestCode, resultCode, data);
    }
}
