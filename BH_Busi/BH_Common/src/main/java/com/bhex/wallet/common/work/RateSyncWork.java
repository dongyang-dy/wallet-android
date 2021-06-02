package com.bhex.wallet.common.work;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.ListenableWorker;
import androidx.work.RxWorker;
import androidx.work.WorkerParameters;

import com.bhex.network.utils.HUtils;
import com.bhex.network.utils.JsonUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.common.api.BHttpApi;
import com.bhex.wallet.common.api.BHttpApiInterface;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.BHRates;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * @author gongdongyang
 * 2020-6-12 19:31:05
 */
public class RateSyncWork extends RxWorker {

    public RateSyncWork(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Single<ListenableWorker.Result> createWork() {
        Type type = (new TypeToken<List<BHRates>>() {}).getType();
        String balacne_list = BHUserManager.getInstance().getSymbolList();
        balacne_list = balacne_list.replace("_",",");

        RequestBody txBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("symbols",balacne_list).build();

        return Single.fromObservable(BHttpApi.getService(BHttpApiInterface.class).loadRates(txBody)).flatMap(new Function<List<BHRates>, SingleSource<? extends Result>>() {
            @Override
            public SingleSource<? extends Result> apply(List<BHRates> bhRates) throws Exception {
                LogUtils.d("RateSyncWork===>:","==bhRates=");
                return null;
            }
        }).onErrorReturn(new Function<Throwable, Result>() {
            @Override
            public Result apply(Throwable throwable) throws Exception {
                return null;
            }
        });
    }
}
