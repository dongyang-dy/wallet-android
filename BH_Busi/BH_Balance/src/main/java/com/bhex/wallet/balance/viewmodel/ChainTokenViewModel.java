package com.bhex.wallet.balance.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.bhex.network.RxSchedulersHelper;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.observer.BHBaseObserver;
import com.bhex.network.observer.BHProgressObserver;
import com.bhex.network.utils.ToastUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.balance.helper.BHTokenHelper;
import com.bhex.wallet.balance.model.BHTokenItem;
import com.bhex.wallet.common.api.BHttpApi;
import com.bhex.wallet.common.api.BHttpApiInterface;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.BHToken;
import com.google.gson.JsonObject;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 *
 */
public class ChainTokenViewModel extends AndroidViewModel {

    public MutableLiveData<LoadDataModel> mutableLiveData  = new MutableLiveData<>();

    public ChainTokenViewModel(@NonNull Application application) {
        super(application);
    }


    //申请测试币
    public void send_test_token(FragmentActivity context, String demon,String demon2){
        String address = BHUserManager.getInstance().getCurrentBhWallet().address;
        BHProgressObserver<JsonObject> observer = new BHProgressObserver<JsonObject>(context) {
            @Override
            protected void onSuccess(JsonObject jsonObject) {
                super.onSuccess(jsonObject);
                LoadDataModel ldm = new LoadDataModel();
                mutableLiveData.postValue(ldm);
                ToastUtils.showToast(context.getResources().getString(R.string.string_apply_success));
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel ldm = new LoadDataModel(code,errorMsg);
                mutableLiveData.postValue(ldm);
            }
        };
        BHttpApi.getService(BHttpApiInterface.class).send_test_token(address,demon)
                .observeOn(Schedulers.io())
                .flatMap(new Function<JsonObject, ObservableSource<JsonObject>>() {
                    @Override
                    public ObservableSource<JsonObject> apply(JsonObject jsonObject) throws Exception {
                        return BHttpApi.getService(BHttpApiInterface.class).send_test_token(address,demon2);
                    }
                })
                .compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(context)))
                .subscribe(observer);
    }

    public  void loadBalanceByChain(FragmentActivity activity,String chainName){
        BHBaseObserver<List<BHTokenItem>> pbo = new BHBaseObserver<List<BHTokenItem>>() {
            @Override
            protected void onSuccess(List<BHTokenItem> bhBalances) {
                LoadDataModel loadDataModel = new LoadDataModel(bhBalances);
                mutableLiveData.setValue(loadDataModel);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
            }
        };

        Observable.create((ObservableOnSubscribe<List<BHTokenItem>>) emitter -> {
            List<BHTokenItem> list = new ArrayList<>();

            List<BHToken> tokenList =  BHTokenHelper.loadTokenByChain(chainName);

            //排序 字母排序
            Collections.sort(tokenList,((o1, o2) -> {
                String n1 =  o1.name;
                String n2 =  o2.name;
                return n1.compareTo(n2);
            }));

            //找出主链币的位置
            int position = -1;
            for(BHToken item:tokenList){
                position++;
                if(!item.symbol.equalsIgnoreCase(chainName)){
                    continue;
                }
                break;
            }
            if(position>-1 && position<tokenList.size()){
                BHToken item = tokenList.get(position);
                tokenList.remove(position);
                tokenList.add(0,item);
            }

            int index = 0;
            for(BHToken item:tokenList){
                BHTokenItem bhTokenItem = new BHTokenItem(item);
                bhTokenItem.index = index++;
                list.add(bhTokenItem);
            }


            emitter.onNext(list);
            emitter.onComplete();

        }).compose(RxSchedulersHelper.io_main())
          .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
          .subscribe(pbo);


    }


    public  void loadBalanceByChain(Fragment fragment, String chainName){
        BHBaseObserver<List<BHTokenItem>> pbo = new BHBaseObserver<List<BHTokenItem>>() {
            @Override
            protected void onSuccess(List<BHTokenItem> bhBalances) {
                LoadDataModel loadDataModel = new LoadDataModel(bhBalances);
                mutableLiveData.setValue(loadDataModel);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
            }
        };

        Observable.create((ObservableOnSubscribe<List<BHTokenItem>>) emitter -> {
            List<BHTokenItem> list = new ArrayList<>();

            List<BHToken> tokenList =  BHTokenHelper.loadTokenByChain(chainName);




            emitter.onNext(list);
            emitter.onComplete();

        }).compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(fragment)))
                .subscribe(pbo);


    }
}
