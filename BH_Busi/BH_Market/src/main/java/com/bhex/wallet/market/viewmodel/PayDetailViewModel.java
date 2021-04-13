package com.bhex.wallet.market.viewmodel;

import android.text.TextUtils;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bhex.network.app.BaseApplication;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.observer.BHProgressObserver;
import com.bhex.network.utils.JsonUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.common.api.BHttpApi;
import com.bhex.wallet.common.api.BHttpApiInterface;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.db.AppDataBase;
import com.bhex.wallet.common.db.dao.BHTokenDao;
import com.bhex.wallet.common.model.BHToken;
import com.google.gson.JsonObject;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.util.List;

import io.reactivex.schedulers.Schedulers;
import java8.util.stream.StreamSupport;

/**
 * @author gongdongyang
 * 支付信息-获取币种信息
 */
public class PayDetailViewModel extends ViewModel {

    public MutableLiveData<LoadDataModel> tokenLiveData  = new MutableLiveData<>();
    private BHTokenDao mBhTokenDao = AppDataBase.getInstance(BaseApplication.getInstance()).bhTokenDao();

    public void queryTokenDetail(Fragment fragment, List<String> symbol_list){
        //
        StringBuffer tokens = new StringBuffer("");
        StreamSupport.stream(symbol_list).forEach(v->{
            tokens.append(v).append(",");
        });


        BHProgressObserver<JsonObject> bpo = new BHProgressObserver<JsonObject>(fragment.getContext()) {
            @Override
            protected void onSuccess(JsonObject jsonObject) {
                super.onSuccess(jsonObject);
                for(String str:symbol_list){
                    JsonObject json = jsonObject.getAsJsonObject(str);
                    if(json==null || TextUtils.isEmpty(json.toString())){
                        continue;
                    }
                    BHToken bhToken = JsonUtils.fromJson(json.toString(),BHToken.class);
                    SymbolCache.getInstance().putBHToken(bhToken);
                    mBhTokenDao.insertSingle(bhToken);
                }
                LoadDataModel ldm = new LoadDataModel();
                tokenLiveData.postValue(ldm);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel ldm = new LoadDataModel(code,errorMsg);
                tokenLiveData.postValue(ldm);
            }
        };

        BHttpApi.getService(BHttpApiInterface.class)
                .batchQueryToken(tokens.toString())
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.io())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(fragment)))
                .subscribe(bpo);
    }

}
