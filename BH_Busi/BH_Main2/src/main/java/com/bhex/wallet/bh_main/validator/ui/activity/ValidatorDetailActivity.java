package com.bhex.wallet.bh_main.validator.ui.activity;

import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.CustomTextView;
import com.bhex.network.base.LoadingStatus;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.DateUtil;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.R2;
import com.bhex.wallet.bh_main.validator.enums.ENTRUST_BUSI_TYPE;
import com.bhex.wallet.bh_main.validator.model.ValidatorInfo;
import com.bhex.wallet.bh_main.validator.viewmodel.ValidatorViewModel;
import com.bhex.wallet.common.config.ARouterConfig;
import com.hjq.toast.ToastUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author zc
 * 2020-4-16
 * 验证节点
 */
@Route(path = ARouterConfig.Validator.Validator_Detail)
public class ValidatorDetailActivity extends BaseActivity {

    @Autowired(name = "validatorInfo")
    ValidatorInfo mValidatorInfo;
    @Autowired(name = "valid")
    int mValid;
    @BindView(R2.id.tv_validator_name)
    AppCompatTextView tv_validator_name;
    @BindView(R2.id.tv_voting_power_proportion)
    CustomTextView tv_voting_power_proportion;
    @BindView(R2.id.tv_self_delegate_proportion)
    CustomTextView tv_self_delegate_proportion;
    @BindView(R2.id.tv_up_time)
    CustomTextView tv_up_time;
    @BindView(R2.id.tv_other_delegate_proportion)
    CustomTextView tv_other_delegate_proportion;
    @BindView(R2.id.tv_update_time)
    CustomTextView tv_update_time;
    @BindView(R2.id.tv_max_rate)
    CustomTextView tv_max_rate;
    @BindView(R2.id.tv_max_change_rate)
    CustomTextView tv_max_change_rate;
    @BindView(R2.id.tv_address_value)
    CustomTextView tv_address_value;
    @BindView(R2.id.tv_website_value)
    AppCompatTextView tv_website_value;
    @BindView(R2.id.tv_detail_value)
    AppCompatTextView tv_detail_value;
    @BindView(R2.id.iv_status)
    AppCompatImageView iv_status;
    @BindView(R2.id.tv_status)
    AppCompatTextView tv_status;

    @BindView(R2.id.refreshLayout)
    SmartRefreshLayout swipeRefresh;

    ValidatorViewModel mValidatorViewModel;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_validator_detail;
    }

    @Override
    protected void initView() {

        ARouter.getInstance().inject(this);
        initValidatorView();

        mValidatorViewModel = ViewModelProviders.of(this).get(ValidatorViewModel.class);
    }


    private void initValidatorView() {
        swipeRefresh.setEnableLoadMore(false);
        if (mValidatorInfo == null)
            return;
        if (mValidatorInfo.getDescription() != null) {
            tv_validator_name.setText(mValidatorInfo.getDescription().getMoniker());
            tv_website_value.setText(mValidatorInfo.getDescription().getWebsite());
            tv_detail_value.setText(mValidatorInfo.getDescription().getDetails());
        }
        if (mValid == BHConstants.VALIDATOR_VALID) {
            iv_status.setImageResource(R.mipmap.icon_validator_valid);
            tv_status.setText(R.string.tab_valid);
            tv_status.setTextColor(getResources().getColor(R.color.color_green));
        } else {
            iv_status.setImageResource(R.mipmap.icon_validator_invalid);
            tv_status.setText(R.string.tab_invalid);
            tv_status.setTextColor(getResources().getColor(R.color.global_secondary_text_color));
        }

        tv_voting_power_proportion.setText(TextUtils.isEmpty(mValidatorInfo.getVoting_power_proportion()) ? "" : mValidatorInfo.getVoting_power_proportion() + "%");
        tv_self_delegate_proportion.setText(TextUtils.isEmpty(mValidatorInfo.getSelf_delegate_proportion()) ? "" : mValidatorInfo.getSelf_delegate_amount());
        tv_other_delegate_proportion.setText(TextUtils.isEmpty(mValidatorInfo.apy) ? "" : mValidatorInfo.apy + "%");
        tv_up_time.setText(TextUtils.isEmpty(mValidatorInfo.getUp_time()) ? "" : mValidatorInfo.getUp_time() + "%");

        String tv_time = DateUtil.transTimeWithPattern(mValidatorInfo.getLast_voted_time() * 1000, "yyyy/MM/dd hh:mm:ss aa z");
        tv_update_time.setText(tv_time);
        if (mValidatorInfo.getCommission() != null) {
            tv_max_rate.setText(TextUtils.isEmpty(mValidatorInfo.getCommission().getMax_rate()) ? "" : mValidatorInfo.getCommission().getMax_rate() + "%");
            tv_max_change_rate.setText(TextUtils.isEmpty(mValidatorInfo.getCommission().getMax_change_rate()) ? "" : mValidatorInfo.getCommission().getMax_change_rate() + "%");
        }
        tv_address_value.setText(addressReplace(mValidatorInfo.getOperator_address()));

    }

    private String addressReplace(String originAddress) {
        if (originAddress == null || TextUtils.isEmpty(originAddress)) {
            return "";
        }
        if(originAddress.startsWith(BHConstants.BHT_TOKEN)){
            //originAddress = originAddress.replaceFirst(BHConstants.BHT_TOKEN,BHConstants.BHT_TOKEN.toUpperCase());
        }
        if (originAddress.length()<21) {
            return originAddress;
        }
        return originAddress.replace(originAddress.substring(10,originAddress.length()-10),"...");
    }
    @Override
    protected void addEvent() {
        mValidatorViewModel.validatorLiveData.observe(this, ldm -> {
            swipeRefresh.finishRefresh();
            if (ldm.loadingStatus == LoadingStatus.SUCCESS) {
                updateRecord(ldm.getData());
            }
        });


        swipeRefresh.setOnRefreshListener(refreshLayout -> {
            getRecord(false);
        });
    }

    private void updateRecord(ValidatorInfo data) {
        mValidatorInfo = data;
        initValidatorView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        getRecord(true);
    }

    @OnClick({R2.id.iv_copy, R2.id.btn_transfer_entrust, R2.id.btn_relieve_entrust, R2.id.btn_do_entrust})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.iv_copy) {
            if (mValidatorInfo==null){
                return;
            }
            String copy_text = mValidatorInfo.getOperator_address();
            if(!TextUtils.isEmpty(copy_text) && copy_text.startsWith(BHConstants.BHT_TOKEN)){
                copy_text = copy_text.replaceFirst(BHConstants.BHT_TOKEN,BHConstants.BHT_TOKEN.toUpperCase());
            }

            ToolUtils.copyText(copy_text, this);
            ToastUtils.show(getResources().getString(R.string.copyed));

        } else if (view.getId() == R.id.btn_transfer_entrust) {
            ARouter.getInstance().build(ARouterConfig.Validator.Do_Entrust)
                    .withObject("validatorInfo", mValidatorInfo)
                    .withInt("bussiType", ENTRUST_BUSI_TYPE.TRANFER_ENTRUS.getTypeId())
                    .navigation();
        } else if (view.getId() == R.id.btn_relieve_entrust) {
            ARouter.getInstance().build(ARouterConfig.Validator.Do_Entrust)
                    .withObject("validatorInfo", mValidatorInfo)
                    .withInt("bussiType", ENTRUST_BUSI_TYPE.RELIEVE_ENTRUS.getTypeId())
                    .navigation();
        } else if (view.getId() == R.id.btn_do_entrust) {
            ARouter.getInstance().build(ARouterConfig.Validator.Do_Entrust)
                    .withObject("validatorInfo", mValidatorInfo)
                    .withInt("bussiType", ENTRUST_BUSI_TYPE.DO_ENTRUS.getTypeId())
                    .navigation();
        }
    }

    private void getRecord(boolean isShowDialog) {
        mValidatorViewModel.getValidatorInfo(this,
                mValidatorInfo.getOperator_address(),isShowDialog);
    }

}
