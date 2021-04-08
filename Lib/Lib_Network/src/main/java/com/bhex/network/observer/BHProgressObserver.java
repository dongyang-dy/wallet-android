package com.bhex.network.observer;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.bhex.network.R;
import com.bhex.network.cache.utils.LogUtils;

import io.reactivex.disposables.Disposable;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/4/6
 * Time: 20:39
 */
public abstract class BHProgressObserver<T> extends BHBaseObserver<T> {

    private String loadingText;

    private ProgressDialog loadingDialog = null;

    private boolean isShowDialog = true;

    public BHProgressObserver(Context context) {
        this(context, context.getString(R.string.http_loading));
    }

    public BHProgressObserver(Context context,boolean isShowDialog) {
        this(context, context.getString(R.string.http_loading));
        this.isShowDialog = isShowDialog;
    }

    public BHProgressObserver(Context context,boolean isShowDialog,boolean isNeedToast) {
        this(context, context.getString(R.string.http_loading));
        this.isShowDialog = isShowDialog;
        this.isNeedShowtoast = isNeedToast;
    }

    public BHProgressObserver(Context context, String hint) {
        this.context = context;
        this.loadingText = hint;
    }

    @Override
    protected void onSuccess(T t) {
        closeLoading();
    }

    @Override
    public void onError(Throwable e) {
        super.onError(e);
        closeLoading();
    }

    @Override
    public void onSubscribe(Disposable d) {
        showLoading();
    }

    @Override
    public void onComplete() {
        closeLoading();
    }


    public void showLoading(){
        if(!isShowDialog){
            return;
        }
        if (loadingDialog == null){
            loadingDialog = new ProgressDialog(this.context,R.style.AlertDialogCustom);
        }
        loadingDialog.setMessage(this.loadingText);
        loadingDialog.show();
        //loadingDialog.setCanceledOnTouchOutside();

        ProgressBar progressBar = loadingDialog.findViewById(android.R.id.progress);
        progressBar.getIndeterminateDrawable().setColorFilter(context.getResources().getColor(R.color.progress_color), PorterDuff.Mode.SRC_IN);
    }

    public void closeLoading() {
        if (this.context != null && loadingDialog!=null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

}
