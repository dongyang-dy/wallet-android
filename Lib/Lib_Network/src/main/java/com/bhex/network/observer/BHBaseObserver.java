package com.bhex.network.observer;

import android.content.Context;

import com.bhex.network.R;
import com.bhex.network.exception.ApiException;
import com.bhex.network.exception.ExceptionEngin;
import com.bhex.network.utils.ToastUtils;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/4/6
 * Time: 19:22
 */
public abstract class BHBaseObserver<T>  implements Observer<T> {

    public Context context;

    public boolean isNeedShowtoast = true;

    public BHBaseObserver() {
    }

    public BHBaseObserver(boolean isNeedShowtoast) {
        this.isNeedShowtoast = isNeedShowtoast;
    }

    @Override
    public void onNext(T t) {
        onSuccess(t);
    }


    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
        try{
            ApiException apiException = ExceptionEngin.handleException(e);
            onFailure(apiException.getCode(), apiException.getDisplayMessage());
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    protected abstract void onSuccess(T t);

    protected  void onFailure(int code, String errorMsg){
        //if (this.isNeedShowtoast && code != 1000){
        if (this.isNeedShowtoast){
            ToastUtils.showToast(errorMsg);
        }
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onSubscribe(Disposable d) {
    }
}
