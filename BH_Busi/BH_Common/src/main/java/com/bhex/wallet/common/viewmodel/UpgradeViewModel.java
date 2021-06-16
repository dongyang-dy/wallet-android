package com.bhex.wallet.common.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.MutableLiveData;

import com.bhex.network.RxSchedulersHelper;
import com.bhex.network.base.LoadDataModel;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.network.observer.BHBaseObserver;
import com.bhex.network.observer.BHProgressObserver;
import com.bhex.network.utils.JsonUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.common.api.BHttpApi;
import com.bhex.wallet.common.api.BHttpApiInterface;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.BHPhoneInfo;
import com.bhex.wallet.common.model.UpgradeInfo;
import com.bhex.wallet.common.tx.TransactionOrder;
import com.google.gson.JsonObject;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.util.List;

/**
 * @author gongdongyang
 * 版本升级
 * 2020-5-8 22:00:38
 */
public class UpgradeViewModel extends AndroidViewModel {

    public MutableLiveData<LoadDataModel<UpgradeInfo>> upgradeLiveData  = new MutableLiveData<>();


    public UpgradeViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * 获取版本信息
     * @param activity
     */
    public void getUpgradeInfo(BaseActivity activity,boolean isShowDialog){
        BHProgressObserver<JsonObject> observer = new BHProgressObserver<JsonObject>(activity,isShowDialog,false) {
            @Override
            protected void onSuccess(JsonObject jsonObject) {
                //LogUtils.d("UpgradeViewModel==>","jsonObject=="+jsonObject.toString());
                UpgradeInfo upgradeInfo = JsonUtils.fromJson(jsonObject.toString(),UpgradeInfo.class);
                LoadDataModel ldm = new LoadDataModel(upgradeInfo);
                upgradeLiveData.postValue(ldm);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel ldm = new LoadDataModel(code,errorMsg);
                upgradeLiveData.postValue(ldm);
            }
        };


        BHttpApi.getService(BHttpApiInterface.class)
                .getUpgradeInfo(BHPhoneInfo.appId,BHPhoneInfo.appVersion,BHPhoneInfo.deviceType,BHPhoneInfo.deviceVersion)
                .compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(observer);

    }


    public void getUpgradeInfoExt(BaseActivity activity){
        BHProgressObserver<JsonObject> observer = new BHProgressObserver<JsonObject>(activity) {
            @Override
            protected void onSuccess(JsonObject jsonObject) {
                UpgradeInfo upgradeInfo = JsonUtils.fromJson(jsonObject.toString(),UpgradeInfo.class);
                LoadDataModel ldm = new LoadDataModel(upgradeInfo);
                upgradeLiveData.postValue(ldm);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel ldm = new LoadDataModel(code,errorMsg);
                upgradeLiveData.postValue(ldm);
            }
        };


        BHttpApi.getService(BHttpApiInterface.class)
                .getUpgradeInfo(BHPhoneInfo.appId,BHPhoneInfo.appVersion,BHPhoneInfo.deviceType,BHPhoneInfo.deviceVersion)
                .compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(observer);

    }
}
