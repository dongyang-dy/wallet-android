package com.bhex.wallet.balance.viewmodel;

import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.bhex.network.app.BaseApplication;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.observer.BHProgressObserver;
import com.bhex.wallet.common.db.AppDataBase;
import com.bhex.wallet.common.db.dao.BHAddressBookDao;
import com.bhex.wallet.common.db.entity.BHAddressBook;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.manager.BHUserManager;

import java.util.List;

/**
 * @author gdy
 * 2021-3-20 12:00:36
 */
public class AddressBookViewModel extends AndroidViewModel {

    BHAddressBookDao addressBookDao;

    public MutableLiveData<LoadDataModel> mutableLiveData  = new MutableLiveData<>();


    public AddressBookViewModel(@NonNull Application application) {
        super(application);
        addressBookDao = AppDataBase.getInstance(BaseApplication.getInstance()).bhAddressBookDao();
    }


    public void loadAddressBookList(Activity activity){
        BHProgressObserver pbo = new BHProgressObserver<List<BHAddressBook>>(activity) {
            @Override
            public void onSuccess(List<BHAddressBook> list) {
                LoadDataModel loadDataModel = new LoadDataModel("");
                mutableLiveData.postValue(loadDataModel);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel loadDataModel = new LoadDataModel(code,errorMsg);
                mutableLiveData.postValue(loadDataModel);
            }
        };
    }
}
