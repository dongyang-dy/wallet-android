package com.bhex.wallet.common.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;

import com.bhex.network.RxSchedulersHelper;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.network.base.viewmodel.CacheAndroidViewModel;
import com.bhex.network.cache.RxCache;
import com.bhex.network.cache.data.CacheResult;
import com.bhex.network.cache.stategy.CacheStrategy;
import com.bhex.network.cache.stategy.IStrategy;
import com.bhex.network.observer.BHBaseObserver;
import com.bhex.network.observer.SimpleObserver;
import com.bhex.network.utils.JsonUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.common.api.BHttpApi;
import com.bhex.wallet.common.api.BHttpApiInterface;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.cache.RatesCache;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.SequenceManager;
import com.bhex.wallet.common.model.AccountInfo;
import com.bhex.wallet.common.model.BHRates;
import com.bhex.wallet.common.utils.LiveDataBus;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/4/4
 * Time: 0:04
 */
public class BalanceViewModel extends CacheAndroidViewModel implements LifecycleObserver {

    private BaseActivity mContext;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public MutableLiveData<LoadDataModel<AccountInfo>> accountLiveData  = new MutableLiveData<>();

    public BalanceViewModel(@NonNull Application application) {
        super(application);
    }

    //获取资产
    public void getAccountInfo(BaseActivity activity,IStrategy strategy){

        Type type = (new TypeToken<JsonObject>() {}).getType();
        String cache_key = BHUserManager.getInstance().getCurrentBhWallet().address+"_"+BH_BUSI_TYPE.账户资产缓存.value;
        BHBaseObserver<JsonObject> observer = new BHBaseObserver<JsonObject>(false) {
            @Override
            protected void onSuccess(JsonObject jsonObject) {
                //super.onSuccess(jsonObject);
                AccountInfo accountInfo = JsonUtils.fromJson(jsonObject.toString(),AccountInfo.class);
                LoadDataModel loadDataModel = new LoadDataModel(accountInfo);
                if(accountInfo!=null){
                    BHUserManager.getInstance().setAccountInfo(accountInfo);
                    //移除正在生成中的地址
                    SequenceManager.getInstance().removeAddressStatus(accountInfo);
                }
                LiveDataBus.getInstance().with(BHConstants.Label_Account,LoadDataModel.class).postValue(loadDataModel);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel loadDataModel = new LoadDataModel(LoadingStatus.ERROR,"");
                LiveDataBus.getInstance().with(BHConstants.Label_Account,LoadDataModel.class).postValue(loadDataModel);
            }
        };

        BHttpApi.getService(BHttpApiInterface.class)
                .loadAccount(BHUserManager.getInstance().getCurrentBhWallet().address)
                .compose(RxSchedulersHelper.io_main())
                .compose(RxCache.getDefault().transformObservable(cache_key,type,getCacheStrategy(strategy)))
                .map(new CacheResult.MapFunc<JsonObject>())
                //.as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity,Lifecycle.Event.ON_DESTROY)))
                .subscribe(observer);
    }

    //获取资产
    public void getAccountInfoByAddress(BaseActivity activity,String address){

        Type type = (new TypeToken<JsonObject>() {}).getType();
        String cache_key = address+"_"+BH_BUSI_TYPE.账户资产缓存.value;
        BHBaseObserver<JsonObject> observer = new BHBaseObserver<JsonObject>(false) {
            @Override
            protected void onSuccess(JsonObject jsonObject) {
                //super.onSuccess(jsonObject);
                LogUtils.d("getAccountInfoByAddress==",Thread.currentThread().getName()+"==json=="+jsonObject.toString());
                AccountInfo accountInfo = JsonUtils.fromJson(jsonObject.toString(),AccountInfo.class);
                LoadDataModel ldm = new LoadDataModel(accountInfo);
                if(accountInfo!=null){
                    accountLiveData.setValue(ldm);
                }
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel ldm = new LoadDataModel(LoadingStatus.ERROR,"");
                accountLiveData.setValue(ldm);
            }
        };

        BHttpApi.getService(BHttpApiInterface.class)
                .loadAccount(address)
                .compose(RxCache.getDefault().transformObservable(cache_key,type,CacheStrategy.cacheAndRemote()))
                .compose(RxSchedulersHelper.io_main())
                .map(new CacheResult.MapFunc<JsonObject>())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity,Lifecycle.Event.ON_DESTROY)))
                .subscribe(observer);
    }

    public void getRateToken(BaseActivity activity,IStrategy strategy){
        Type type = (new TypeToken<List<BHRates>>() {}).getType();
        String balacne_list = BHUserManager.getInstance().getSymbolList();
        balacne_list = balacne_list.replace("_",",");
        //LogUtils.d("RatesCache====T=>:","==onSuccess=="+balacne_list);
        //LogUtils.d("balacne_list=="+balacne_list);

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("symbols",balacne_list).build();

        BHttpApi.getService(BHttpApiInterface.class).loadRates(body)
                .compose(RxSchedulersHelper.io_main())
                .compose(RxCache.getDefault().transformObservable(RatesCache.CACHE_KEY, type, getCacheStrategy(strategy)))
                .map(new CacheResult.MapFunc<>())
                .subscribe(new BHBaseObserver<List<BHRates>>(false) {
                    @Override
                    protected void onSuccess(List<BHRates> ratelist) {
                        if(ToolUtils.checkListIsEmpty(ratelist)){
                            return;
                        }

                        RatesCache.getInstance().getRatesMap().clear();
                        for (BHRates rate:ratelist){
                            RatesCache.getInstance().getRatesMap().put(rate.getToken().toLowerCase(),rate.getRates());
                        }
                    }

                    @Override
                    protected void onFailure(int code, String errorMsg) {
                        super.onFailure(code, errorMsg);
                    }
                });
    }


    private void beginReloadData() {
        Observable.interval(300,2000L, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                //.as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(mContext, Lifecycle.Event.ON_DESTROY)))
                .subscribe(new SimpleObserver<Long>(){
                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                        if(compositeDisposable==null){
                            compositeDisposable = new CompositeDisposable();
                        }
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        super.onNext(aLong);
                        BalanceViewModel.this.getAccountInfo(mContext, CacheStrategy.onlyRemote());
                        BalanceViewModel.this.getRateToken(mContext,null);
                        //BalanceViewModel.this.queryTransactionDetailExt(mContext);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                });


    }

    //获取资产
    /*public void resetSequence(BaseActivity activity){

        //Type type = (new TypeToken<JsonObject>() {}).getType();
        //String cache_key = BHUserManager.getInstance().getCurrentBhWallet().address+"_"+BH_BUSI_TYPE.账户资产缓存.value;
        BHBaseObserver<JsonObject> observer = new BHBaseObserver<JsonObject>(false) {
            @Override
            protected void onSuccess(JsonObject jsonObject) {
                //super.onSuccess(jsonObject);
                AccountInfo accountInfo = JsonUtils.fromJson(jsonObject.toString(),AccountInfo.class);
                if(accountInfo==null){
                   return;
                }
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel loadDataModel = new LoadDataModel(LoadingStatus.ERROR,"");
                LiveDataBus.getInstance().with(BHConstants.Label_Account,LoadDataModel.class).postValue(loadDataModel);
            }
        };

        BHttpApi.getService(BHttpApiInterface.class)
                .loadAccount(BHUserManager.getInstance().getCurrentBhWallet().address)
                .compose(RxSchedulersHelper.io_main())
                //.as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity,Lifecycle.Event.ON_DESTROY)))
                .subscribe(observer);
    }*/

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate(){
        /*if(mContext.getClass().getName().equals("com.bhex.wallet.bh_main.ui.activity.MainActivity")){
            Fragment frag = mContext.getSupportFragmentManager().findFragmentByTag(BalanceFragment.class.getSimpleName());
            if(frag!=null && frag.getUserVisibleHint() && !frag.isHidden()){
                beginReloadData();
            }
        }else{
            beginReloadData();
        }*/
        beginReloadData();
    }



    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy(){
        if(compositeDisposable!=null && !compositeDisposable.isDisposed()){
            compositeDisposable.dispose();
            compositeDisposable = null;
        }
    }

    public BalanceViewModel build(BaseActivity context){
        mContext = context;
        return this;
    }


}
