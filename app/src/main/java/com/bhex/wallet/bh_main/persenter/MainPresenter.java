package com.bhex.wallet.bh_main.persenter;

import android.Manifest;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;

import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.base.BasePresenter;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.R;
import com.bhex.wallet.app.BHApplication;
import com.bhex.wallet.balance.ui.fragment.BalanceFragment;
import com.bhex.wallet.bh_main.my.ui.fragment.MyFragment;
import com.bhex.wallet.bh_main.proposals.ui.fragment.ProposalFragment;
import com.bhex.wallet.bh_main.ui.activity.MainActivity;
import com.bhex.wallet.bh_main.validator.ui.fragment.ValidatorFragment;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.UpgradeInfo;
import com.bhex.wallet.common.ui.fragment.UpgradeFragment;
import com.bhex.wallet.common.viewmodel.UpgradeViewModel;
import com.bhex.wallet.market.ui.fragment.MarketFragment;
import com.bhex.wallet.mnemonic.ui.fragment.SecureTipsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * created by gongdongyang
 * on 2020/2/26
 */
public class MainPresenter extends BasePresenter {

    public MainPresenter(BaseActivity activity) {
        super(activity);
    }

    private UpgradeViewModel mUpgradeVM;

    private String mTokenId;
    @Override
    public void onCreate(@NotNull LifecycleOwner owner) {
        super.onCreate(owner);
        LogUtils.d("MainPresenter==>:","==onCreate==");
        showIsBackup();
        mUpgradeVM = ViewModelProviders.of(getActivity()).get(UpgradeViewModel.class);
        //升级请求
        mUpgradeVM.upgradeLiveData.observe(getActivity(),ldm->{
            processUpgradeInfo(ldm);
        });
        mUpgradeVM.getUpgradeInfo(getActivity());
    }

    /**
     * 是否备份助记词
     */
    public void showIsBackup(){
        int isBackUp = BHUserManager.getInstance().getCurrentBhWallet().isBackup;
        if(isBackUp== BH_BUSI_TYPE.未备份.getIntValue() && ((MainActivity)getActivity()).isReset){
            BHApplication.getMainHandler().postDelayed(()->{
                SecureTipsFragment.showDialog(getActivity().getSupportFragmentManager(),SecureTipsFragment.class.getName());
                //SecureTipsFragmentExt.showDialog(getActivity().getSupportFragmentManager(),SecureTipsFragmentExt.class.getName());
            },300);
        }
    }

    /**
     * 处理升级请求
     */
    private void processUpgradeInfo(LoadDataModel<UpgradeInfo> ldm) {
        if(ldm.loadingStatus== LoadingStatus.SUCCESS){
            UpgradeInfo upgradeInfo = ldm.getData();
            if(upgradeInfo.needUpdate==1){
                showUpgradeDailog(upgradeInfo);
            }
        }else if(ldm.loadingStatus== LoadingStatus.ERROR){

        }
    }

    /**
     * 显示升级对话框
     */
    private void showUpgradeDailog(UpgradeInfo upgradeInfo){
        //是否强制升级
        /*if(upgradeInfo.needForceUpdate){
            //
            UpgradeFragment fragment = UpgradeFragment.Companion.showUpgradeDialog(upgradeInfo,dialogOnClickListener);
            fragment.show(getActivity().getSupportFragmentManager(),UpgradeFragment.class.getName());
        }else{
            long lastTime = MMKVManager.getInstance().mmkv().decodeLong(BHPhoneInfo.appVersion, 0);
            long diff = DateUtil.getDaysBetweenDate(lastTime,System.currentTimeMillis());
            //LogUtils.d("MainPresenter==>","==="+diff);
            if(diff>1){
                UpgradeFragment fragment = UpgradeFragment.Companion.showUpgradeDialog(upgradeInfo,dialogOnClickListener);
                fragment.show(getActivity().getSupportFragmentManager(),UpgradeFragment.class.getName());
            }
        }*/
        UpgradeFragment fragment = UpgradeFragment.Companion.showUpgradeDialog(upgradeInfo,dialogOnClickListener);
        fragment.show(getActivity().getSupportFragmentManager(),UpgradeFragment.class.getName());
    }

    UpgradeFragment.DialogOnClickListener dialogOnClickListener = v -> {
        //ToastUtils.showToast("=dialogOnClickListener=");
        //下载升级
        ToastUtils.showToast(getActivity().getResources().getString(R.string.app_loading_now));
    };

    public void gotoTarget(BottomNavigationView bnv,String goto_position,String token){

        if(TextUtils.isEmpty(goto_position)){
            return;
        }

        if(!TextUtils.isEmpty(token)){
            mTokenId = token;
        }

        if(goto_position.equals(BH_BUSI_TYPE.市场.value)){
            bnv.setSelectedItemId(bnv.getMenu().getItem(1).getItemId());
        }
    }
    /**
     * 动态权限申请
     */
    public void requestPermissions() {
        RxPermissions rxPermission = new RxPermissions(getActivity());
        rxPermission
                .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            // 用户已经同意该权限
                            LogUtils.d(permission.name + " is granted.");
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                            LogUtils.d(permission.name + " is denied. More info should be provided.");
                        } else {
                            // 用户拒绝了该权限，并且选中『不再询问』
                            LogUtils.d(permission.name + " is denied.");
                        }
                    }
                });
    }


    public void goBalanceFragment(){
        setCurrentFragment(BalanceFragment.class, null);
    }


    public void goMarketFragment() {
        if(!TextUtils.isEmpty(mTokenId)){
            Bundle bundle = new Bundle();
            bundle.putString("go_token",mTokenId);
            setCurrentFragment(MarketFragment.class, bundle);
        }else{
            setCurrentFragment(MarketFragment.class, null);
        }
        //setCurrentFragment(MarketFragment.class, null);
    }

    public void goValidatorFragment() {
        setCurrentFragment(ValidatorFragment.class, null);
    }


    public void goProposalFragment() {
        setCurrentFragment(ProposalFragment.class,null);
    }


    public void goMyFragment() {
        setCurrentFragment(MyFragment.class, null);
    }


    private void setCurrentFragment(Class fragmentClass, Bundle bundle) {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        Fragment dstFragment = manager.findFragmentByTag(fragmentClass.getSimpleName());

        List<Fragment> fragments = manager.getFragments();

        FragmentTransaction transaction = manager.beginTransaction();

        if (fragments != null)
            for (Fragment frg : fragments)
                transaction.hide(frg);

        if (dstFragment == null) {
            dstFragment = Fragment.instantiate(getActivity(), fragmentClass.getName());
            dstFragment.setArguments(bundle);
            transaction.add(R.id.fl_content, dstFragment, fragmentClass.getSimpleName()).commitAllowingStateLoss();
        } else {
            //dstFragment.setArguments(bundle);
            transaction.show(dstFragment).commitAllowingStateLoss();
        }

    }


}
