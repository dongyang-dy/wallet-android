package com.bhex.network.mvx.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bhex.network.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/8
 * Time: 20:58
 */
public abstract class BaseDialogFragment extends DialogFragment {

    public View mRootView;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(mRootView==null){
            mRootView = inflater.inflate(getLayout(), container, false);
            unbinder = ButterKnife.bind(this,mRootView);
        }
        initView();
        return mRootView;
    }

    protected void initView(){

    }
    public abstract  int getLayout();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(unbinder!=null){
            unbinder.unbind();;
        }
    }

    @Override
    public int getTheme() {
        return R.style.basedialog_anim_style;
    }
}
