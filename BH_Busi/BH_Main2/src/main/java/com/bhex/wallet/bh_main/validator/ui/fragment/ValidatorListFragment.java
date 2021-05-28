package com.bhex.wallet.bh_main.validator.ui.fragment;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.EmptyLayout;
import com.bhex.lib.uikit.widget.editor.SimpleTextWatcher;
import com.bhex.lib.uikit.widget.viewpager.CustomViewPager;
import com.bhex.network.base.LoadingStatus;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.R2;
import com.bhex.wallet.bh_main.validator.adapter.ValidatorAdapter;
import com.bhex.wallet.bh_main.validator.model.ValidatorInfo;
import com.bhex.wallet.bh_main.validator.presenter.ValidatorListFragmentPresenter;
import com.bhex.wallet.bh_main.validator.viewmodel.ValidatorViewModel;
import com.bhex.wallet.common.base.BaseFragment;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;


public class ValidatorListFragment extends BaseFragment<ValidatorListFragmentPresenter> {

    public static String KEY_VALIDATOR_TYPE = "validator_type";
    private int mValidatorType = 1;
    ValidatorAdapter mValidatorAdapter;
    @BindView(R2.id.ed_search_content)
    AppCompatEditText ed_search_content;
    @BindView(R2.id.recycler_validator)
    RecyclerView recycler_validator;

    //CustomViewPager viewPager;

    /*@BindView(R2.id.swipeRefresh)
    SmartRefreshLayout swipeRefresh;*/

    @BindView(R2.id.empty_layout)
    EmptyLayout empty_layout;

    List<ValidatorInfo> mOriginValidatorInfoList;
    List<ValidatorInfo> mValidatorInfoList;

    ValidatorViewModel mValidatorViewModel;
    CustomViewPager mVp;

    public ValidatorListFragment() {
    }

    public ValidatorListFragment(CustomViewPager viewPager) {
        mVp = viewPager;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_validator_list;
    }

    @Override
    protected void initView() {
        //swipeRefresh.setEnableLoadMore(true);
        recycler_validator.setNestedScrollingEnabled(true);
        recycler_validator.setHasFixedSize(true);//固定自身size不受adapter变化影响
        Bundle arguments = getArguments();
        if (arguments != null) {
            mValidatorType = arguments.getInt(KEY_VALIDATOR_TYPE, 0);
        }

        /*if(mValidatorType== BHConstants.VALIDATOR_VALID){
            viewPager.setObjectForPosition(mRootView,0);
        }else {
            viewPager.setObjectForPosition(mRootView,1);
        }*/
        mValidatorViewModel = ViewModelProviders.of(this).get(ValidatorViewModel.class);
        recycler_validator.setAdapter(mValidatorAdapter = new ValidatorAdapter(mValidatorType, mValidatorInfoList));
    }

    @Override
    protected void addEvent() {
        empty_layout.showProgess();
        mVp.setObjectForPosition(mValidatorType,mRootView);
        mValidatorViewModel.validatorsLiveData.observe(this, ldm -> {
            //updateRecord(ldm.getData());
            if (ldm.loadingStatus == LoadingStatus.SUCCESS) {
                empty_layout.loadSuccess();
                updateRecord(ldm.getData());
            } else {
                if(mOriginValidatorInfoList==null || mOriginValidatorInfoList.size()==0){
                    empty_layout.showNeterror(view -> {
                        getRecord(true);
                    });
                }
            }
        });
        /*swipeRefresh.setOnRefreshListener(refreshLayout1 -> {
            getRecord(false);
        });*/

        ed_search_content.addTextChangedListener(ValidatorTextWatcher);
        //点击事件
        mValidatorAdapter.setOnItemClickListener((adapter, view, position) -> {
            ValidatorInfo item = mValidatorAdapter.getData().get(position);
            ARouter.getInstance().build(ARouterConfig.Validator.Validator_Detail)
                    .withObject("validatorInfo", item)
                    .withInt("valid", mValidatorType)
                    .navigation();
        });

        getRecord(mValidatorAdapter==null || mValidatorAdapter.getData()==null || mValidatorAdapter.getData().size()<1);

    }

    private void getRecord(boolean showDialog) {
        if (showDialog) {
            empty_layout.showProgess();
        }
        mValidatorViewModel.getValidatorInfos(getYActivity(),BH_BUSI_TYPE.有效节点.getIntValue());
    }

    @Override
    public void onResume() {
        super.onResume();
        //ToolUtils.hintKeyBoard(getYActivity());
    }

    public void updateRecord(List<ValidatorInfo> datas) {
        //datas.get(0).is_elected =false;
        mOriginValidatorInfoList = datas;
        //
        List<ValidatorInfo> result = StreamSupport.stream(datas).filter(validatorInfo -> {
            if(mValidatorType== BH_BUSI_TYPE.托管节点.getIntValue()){
                return validatorInfo.is_key_node;
            }else if(mValidatorType== BH_BUSI_TYPE.共识节点.getIntValue()){
                return validatorInfo.is_elected && !validatorInfo.is_key_node;
            }else if(mValidatorType== BH_BUSI_TYPE.竞争节点.getIntValue()){
                return !validatorInfo.is_elected && !validatorInfo.is_key_node;
            }else{
                return false;
            }

        }).collect(Collectors.toList());


        if (!ToolUtils.checkListIsEmpty(result)) {
            empty_layout.loadSuccess();
        } else {
            empty_layout.showNoData();
        }
        //mValidatorAdapter.getData().clear();
        mValidatorAdapter.setNewData(result);
    }

    @Override
    protected void initPresenter() {
        mPresenter = new ValidatorListFragmentPresenter(getYActivity());
    }

    private SimpleTextWatcher ValidatorTextWatcher = new SimpleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            if(ToolUtils.checkListIsEmpty(mOriginValidatorInfoList)){
                return;
            }
            String searchContent = ed_search_content.getText().toString().trim();
            List<ValidatorInfo> result = null;
            if(TextUtils.isEmpty(searchContent)){
                result = StreamSupport.stream(mOriginValidatorInfoList).filter(validatorInfo -> {
                    if(mValidatorType== BH_BUSI_TYPE.托管节点.getIntValue()){
                        return validatorInfo.is_key_node;
                    }else if(mValidatorType== BH_BUSI_TYPE.共识节点.getIntValue()){
                        return validatorInfo.is_elected && !validatorInfo.is_key_node;
                    }else if(mValidatorType== BH_BUSI_TYPE.竞争节点.getIntValue()) {
                        return !validatorInfo.is_elected && !validatorInfo.is_key_node;
                    }else {
                        return false;
                    }

                }).collect(Collectors.toList());
            }else{
                result = StreamSupport.stream(mOriginValidatorInfoList).filter(item -> {
                    return item.getDescription() != null && item.getDescription().getMoniker().toLowerCase().contains(searchContent.toLowerCase());
                }).collect(Collectors.toList());
            }

            if (result.size() > 0) {
                empty_layout.loadSuccess();
            } else {
                empty_layout.showNoData();
            }

            mValidatorAdapter.getData().clear();
            mValidatorAdapter.addData(result);
        }
    };

}
