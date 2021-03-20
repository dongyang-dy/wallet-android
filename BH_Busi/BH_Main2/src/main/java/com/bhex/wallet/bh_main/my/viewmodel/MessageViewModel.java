package com.bhex.wallet.bh_main.my.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.MutableLiveData;

import com.bhex.network.RxSchedulersHelper;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.viewmodel.CacheAndroidViewModel;
import com.bhex.network.cache.RxCache;
import com.bhex.network.cache.data.CacheResult;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.common.base.BaseFragment;
import com.bhex.network.observer.BHBaseObserver;
import com.bhex.network.utils.JsonUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.wallet.bh_main.my.model.BHMessage;
import com.bhex.wallet.common.api.BHttpApi;
import com.bhex.wallet.common.api.BHttpApiInterface;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.MMKVManager;
import com.bhex.wallet.common.model.BHPage;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.lang.reflect.Type;

/**
 * @author gongdongyang
 * 2020-5-21 14:21:15
 * 消息
 */
public class MessageViewModel extends CacheAndroidViewModel {

    public  MutableLiveData<LoadDataModel> messageLiveData  = new MutableLiveData<>();

    public MessageViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * 获取消息
     * @param activity
     * @param page
     * @param type 消息类型
     */
    public void loadMessageByAddress(BaseFragment activity, int page, String type){
        String address = BHUserManager.getInstance().getCurrentBhWallet().address;
        Type class_type = (new TypeToken<JsonObject>() {}).getType();
        String key = address+"_"+type+"_"+ BH_BUSI_TYPE.消息缓存.value;

        BHBaseObserver<JsonObject> observer = new BHBaseObserver<JsonObject>() {
            @Override
            protected void onSuccess(JsonObject jsonObject) {
                Type type = new TypeToken<BHPage<BHMessage>>() {}.getType();
                BHPage<BHMessage> page = JsonUtils.fromJson(jsonObject.toString(),type);
                LoadDataModel ldm = new LoadDataModel(page);
                messageLiveData.postValue(ldm);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel ldm = new LoadDataModel(code,errorMsg);
                messageLiveData.postValue(ldm);
            }
        };
        BHttpApi.getService(BHttpApiInterface.class)
                .queryNotificationByAddress(address, page,BHConstants.PAGE_SIZE,type)
                .compose(RxSchedulersHelper.io_main())
                //.compose(RxCache.getDefault().<JsonObject>transformObservable(key, class_type, getCacheStrategy()))
                //.map(new CacheResult.MapFunc<JsonObject>())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(observer);

    }

    public void updateMessageStatus(BaseFragment activity,String id,String type){
        String address = BHUserManager.getInstance().getCurrentBhWallet().address;
        String key = address+"_"+type+"_"+ BH_BUSI_TYPE.已读消息.value;

        String v_read_msg_id = MMKVManager.getInstance().mmkv().decodeString(key,"");
        if(!v_read_msg_id.contains(key)){
            v_read_msg_id = v_read_msg_id.concat(id).concat("|");
            MMKVManager.getInstance().mmkv().encode(key,v_read_msg_id);
        }

       /* String address = BHUserManager.getInstance().getCurrentBhWallet().address;

        BHBaseObserver<JsonObject> observer = new BHBaseObserver<JsonObject>() {
            @Override
            protected void onSuccess(JsonObject jsonObject) {
                LoadDataModel ldm = new LoadDataModel();
                //messageLiveData.postValue(ldm);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);

            }
        };

        BHttpApi.getService(BHttpApiInterface.class)
                .updateNotificationStatus(address, id)
                .compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity, Lifecycle.Event.ON_DESTROY)))
                .subscribe(observer);*/
    }
}
