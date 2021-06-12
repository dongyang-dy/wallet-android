package com.bhex.wallet.balance.viewmodel;

import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.bhex.network.RxSchedulersHelper;
import com.bhex.network.app.BaseApplication;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.observer.BHProgressObserver;
import com.bhex.wallet.balance.ui.activity.AddAddressActivity;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.db.AppDataBase;
import com.bhex.wallet.common.db.dao.BHAddressBookDao;
import com.bhex.wallet.common.db.entity.BHAddressBook;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.BHToken;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.util.List;

import io.reactivex.Observable;

/**
 * @author gdy
 * 2021-3-20 12:00:36
 */
public class AddressBookViewModel extends AndroidViewModel {

    BHAddressBookDao addressBookDao;

    public MutableLiveData<LoadDataModel<List<BHAddressBook>>> mutableLiveData  = new MutableLiveData<>();

    public MutableLiveData<LoadDataModel> addLiveData  = new MutableLiveData<>();
    public MutableLiveData<LoadDataModel> deleteLiveData  = new MutableLiveData<>();
    public AddressBookViewModel(@NonNull Application application) {
        super(application);
        addressBookDao = AppDataBase.getInstance(BaseApplication.getInstance()).bhAddressBookDao();
    }

    //查找地址列表
    public void loadAddressBookList(BaseActivity activity, String wallet_address,String chain){
        BHProgressObserver pbo = new BHProgressObserver<List<BHAddressBook>>(activity) {
            @Override
            public void onSuccess(List<BHAddressBook> list) {
                LoadDataModel loadDataModel = new LoadDataModel(list);
                mutableLiveData.postValue(loadDataModel);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel loadDataModel = new LoadDataModel(code,errorMsg);
                mutableLiveData.postValue(loadDataModel);
            }
        };

        Observable.create((emitter)->{
            try{
                List<BHAddressBook> list =   addressBookDao.loadAddressBook(wallet_address,chain);
                emitter.onNext(list);
                emitter.onComplete();
            }catch (Exception e){
                e.printStackTrace();
            }
        }).compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(pbo);
    }

    //保存地址
    public void saveAddress(BaseActivity activity, BHAddressBook addressBook) {
        BHProgressObserver pbo = new BHProgressObserver<Long>(activity) {
            @Override
            public void onSuccess(Long result) {
                LoadDataModel loadDataModel = new LoadDataModel(result);
                addLiveData.postValue(loadDataModel);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel loadDataModel = new LoadDataModel(code,errorMsg);
                addLiveData.postValue(loadDataModel);
            }
        };

        Observable.create((emitter)->{
            try{
                //判断地址是否存在
                BHAddressBook item = addressBookDao.existsAddress(addressBook.address,addressBook.wallet_address,addressBook.chain);
                if(item!=null){
                    emitter.onNext(item.id);
                }else{
                    Long result = addressBookDao.insertSingle(addressBook);
                    emitter.onNext(new Long(0));
                }
                emitter.onComplete();
            }catch (Exception e){
                e.printStackTrace();
                emitter.onNext(1);
                emitter.onComplete();
            }
        }).compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(pbo);
    }

    //保存地址
    public void deleteAddress(BaseActivity activity, BHAddressBook addressBook) {
        BHProgressObserver pbo = new BHProgressObserver<String>(activity) {
            @Override
            public void onSuccess(String result) {
                LoadDataModel loadDataModel = new LoadDataModel("");
                deleteLiveData.postValue(loadDataModel);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel loadDataModel = new LoadDataModel(code,errorMsg);
                deleteLiveData.postValue(loadDataModel);
            }
        };

        Observable.create((emitter)->{
            try{
                addressBookDao.deleteAddress(addressBook.id);
                emitter.onNext("OK");
                emitter.onComplete();
            }catch (Exception e){
                e.printStackTrace();
            }
        }).compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(pbo);
    }
}
