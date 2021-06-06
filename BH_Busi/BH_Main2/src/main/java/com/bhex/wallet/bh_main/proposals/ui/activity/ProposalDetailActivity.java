package com.bhex.wallet.bh_main.proposals.ui.activity;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.network.base.LoadingStatus;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.DateUtil;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.R2;
import com.bhex.wallet.bh_main.proposals.model.ProposalInfo;
import com.bhex.wallet.bh_main.proposals.viewmodel.ProposalViewModel;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.google.android.material.button.MaterialButton;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author zc
 * 2020-4-22
 * 提案详情
 */
@Route(path = ARouterConfig.Proposal.Proposal_Detail)
public class ProposalDetailActivity extends BaseActivity {

    @Autowired(name = "proposalInfo")
    ProposalInfo mProposalInfo;

    @BindView(R2.id.tv_proposal_name)
    AppCompatTextView tv_proposal_name;
    @BindView(R2.id.tv_status)
    AppCompatTextView tv_status;
    @BindView(R2.id.tv_id)
    AppCompatTextView tv_id;

    @BindView(R2.id.tv_type)
    AppCompatTextView tv_type;
    @BindView(R2.id.tv_validator)
    AppCompatTextView tv_validator;
    @BindView(R2.id.tv_time)
    AppCompatTextView tv_time;
    @BindView(R2.id.tv_pledge)
    AppCompatTextView tv_pledge;
    @BindView(R2.id.tv_veto_time1)
    AppCompatTextView tv_veto_time1;
    @BindView(R2.id.tv_veto_time2)
    AppCompatTextView tv_veto_time2;
    @BindView(R2.id.tv_veto_time_title)
    AppCompatTextView tv_veto_time_title;
    @BindView(R2.id.tv_pledge_title)
    AppCompatTextView tv_pledge_title;
    @BindView(R2.id.ll_veto_detail)
    ConstraintLayout ll_veto_detail;
    @BindView(R2.id.divider_line)
    View divider_line;
    @BindView(R2.id.divider_line1)
    View divider_line1;

    @BindView(R2.id.refreshLayout)
    SmartRefreshLayout swipeRefresh;
    @BindView(R2.id.tv_description_value)
    AppCompatTextView tv_description_value;
    @BindView(R2.id.btn_do_pledge)
    MaterialButton btn_do_pledge;
    @BindView(R2.id.btn_do_veto)
    MaterialButton btn_do_veto;


    @BindView(R2.id.tv_veto_yes_rate)
    AppCompatTextView tv_veto_yes_rate;
    @BindView(R2.id.tv_veto_no_rate)
    AppCompatTextView tv_veto_no_rate;
    @BindView(R2.id.tv_veto_abstain_rate)
    AppCompatTextView tv_veto_abstain_rate;
    @BindView(R2.id.tv_veto_no_with_veto_rate)
    AppCompatTextView tv_veto_no_with_veto_rate;


    ProposalViewModel mProposalViewModel;

    private String mToken = BHConstants.BHT_TOKEN;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_proposal_detail;
    }

    @Override
    protected void initView() {

        ARouter.getInstance().inject(this);
        initProposalView();

        mProposalViewModel = ViewModelProviders.of(this).get(ProposalViewModel.class);
    }


    private void initProposalView() {
        swipeRefresh.setEnableLoadMore(false);

        if (mProposalInfo == null)
            return;

        tv_proposal_name.setText(mProposalInfo.getTitle());

        if (mProposalInfo.getStatus() == 1) {
            tv_status.setText(getString(R.string.proposal_in_payment));
            tv_status.setTextColor(getResources().getColor(R.color.proposal_in_payment));
            btn_do_pledge.setBackgroundColor(ColorUtil.getColor(this, R.color.global_button_bg_color));
            btn_do_pledge.setTextColor(getResources().getColor(R.color.global_button_text_color));
            btn_do_pledge.setEnabled(true);
            tv_pledge.setText(mProposalInfo.getTotal_deposit()+ "/"+mProposalInfo.getDeposit_threshold()+mToken.toUpperCase());
        } else if (mProposalInfo.getStatus() == 2) {
            tv_status.setText(getString(R.string.proposal_voting));
            tv_status.setTextColor(getResources().getColor(R.color.proposal_voting));
            btn_do_veto.setBackgroundColor(ColorUtil.getColor(this, R.color.global_button_bg_color));
            btn_do_veto.setTextColor(getResources().getColor(R.color.global_button_text_color));
            btn_do_veto.setEnabled(true);
            long remainDays = DateUtil.getDaysBetweenDate(System.currentTimeMillis(),mProposalInfo.getVoting_end_time()*1000);

            if (remainDays<0) {
                String content = getString(R.string.days_before,Math.abs(remainDays)+"");
                SpannableString spannableString = new SpannableString(content);
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.proposal_in_payment)), content.indexOf(Math.abs(remainDays)+""),content.indexOf(Math.abs(remainDays)+"")+(Math.abs(remainDays)+"").length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv_veto_time2.setText(spannableString);
            } else {
                String content = getString(R.string.days_later,remainDays+"");
                SpannableString spannableString = new SpannableString(content);
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.proposal_in_payment)), content.indexOf(remainDays+""),content.indexOf(remainDays+"")+(remainDays+"").length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv_veto_time2.setText(spannableString);
            }
            updateVetoResult(mProposalInfo.getResult());

        } else if (mProposalInfo.getStatus() == 3) {
            tv_status.setText(getString(R.string.proposal_passed));
            tv_status.setTextColor(getResources().getColor(R.color.proposal_passed));
            btn_do_veto.setBackgroundColor(ColorUtil.getColor(this, R.color.global_button_enable_false_bg));
            btn_do_veto.setTextColor(getResources().getColor(R.color.global_button_enable_false_text));
            btn_do_veto.setEnabled(false);
            long remainDays = DateUtil.getDaysBetweenDate(System.currentTimeMillis(),mProposalInfo.getVoting_end_time()*1000);

            if (remainDays<0) {
                String content = getString(R.string.days_before,Math.abs(remainDays)+"");
                SpannableString spannableString = new SpannableString(content);
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.proposal_in_payment)), content.indexOf(Math.abs(remainDays)+""),content.indexOf(Math.abs(remainDays)+"")+(Math.abs(remainDays)+"").length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv_veto_time2.setText(spannableString);
            } else {
                String content = getString(R.string.days_later,remainDays+"");
                SpannableString spannableString = new SpannableString(content);
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.proposal_in_payment)), content.indexOf(remainDays+""),content.indexOf(remainDays+"")+(remainDays+"").length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv_veto_time2.setText(spannableString);
            }
            updateVetoResult(mProposalInfo.getResult());

        } else if (mProposalInfo.getStatus() == 4) {
            tv_status.setText(getString(R.string.proposal_reject));
            tv_status.setTextColor(getResources().getColor(R.color.proposal_reject));

            btn_do_veto.setBackgroundColor(ColorUtil.getColor(this, R.color.global_button_enable_false_bg));
            btn_do_veto.setTextColor(getResources().getColor(R.color.global_button_enable_false_text));
            btn_do_veto.setEnabled(false);
            long remainDays = DateUtil.getDaysBetweenDate(System.currentTimeMillis(),mProposalInfo.getVoting_end_time()*1000);

            if (remainDays<0) {
                String content = getString(R.string.days_before,Math.abs(remainDays)+"");
                SpannableString spannableString = new SpannableString(content);
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.proposal_in_payment)), content.indexOf(Math.abs(remainDays)+""),content.indexOf(Math.abs(remainDays)+"")+(Math.abs(remainDays)+"").length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv_veto_time2.setText(spannableString);
            } else {
                String content = getString(R.string.days_later,remainDays+"");
                SpannableString spannableString = new SpannableString(content);
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.proposal_in_payment)), content.indexOf(remainDays+""),content.indexOf(remainDays+"")+(remainDays+"").length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv_veto_time2.setText(spannableString);
            }
            updateVetoResult(mProposalInfo.getResult());
        } else if (mProposalInfo.getStatus() == 5) {
            tv_status.setText(getString(R.string.proposal_failed));
            tv_pledge.setText(mProposalInfo.getTotal_deposit()+ "/"+mProposalInfo.getDeposit_threshold()+mToken.toUpperCase());
            tv_status.setTextColor(getResources().getColor(R.color.proposal_failed));
            btn_do_pledge.setBackgroundColor(ColorUtil.getColor(this, R.color.global_button_enable_false_bg));
            btn_do_pledge.setTextColor(getResources().getColor(R.color.global_button_enable_false_text));
            btn_do_pledge.setEnabled(false);

        }
        updateUIState();
        tv_id.setText(mProposalInfo.getId());
        tv_type.setText(mProposalInfo.getType());
        tv_validator.setText(addressReplace(mProposalInfo.getProposer()));
        String time = DateUtil.transTimeWithPattern(mProposalInfo.getSubmit_time() * 1000, "yyyy/MM/dd hh:mm:ss aa z");
        tv_time.setText(time);
        tv_description_value.setText(mProposalInfo.getDescription());
    }

    private void updateVetoResult(ProposalInfo.ResultBean result) {
        if (result==null) {
            tv_veto_yes_rate.setText("");
            tv_veto_no_rate.setText("");
            tv_veto_abstain_rate.setText("");
            tv_veto_no_with_veto_rate.setText("");
            return;
        }
        double total = NumberUtil.add(result.getYes(),result.getNo());
        total =  NumberUtil.add(total + "",result.getAbstain());
        total =  NumberUtil.add(total + "",result.getNo_with_veto());
        tv_veto_yes_rate.setText(result.getYes()+ "(" + NumberUtil.getPercentFormat(String.valueOf(NumberUtil.divide(result.getYes(),total+"",2)))+")");
        tv_veto_no_rate.setText(result.getNo()+ "(" + NumberUtil.getPercentFormat(String.valueOf(NumberUtil.divide(result.getNo(),total+"",2)))+")");
        tv_veto_abstain_rate.setText(result.getAbstain()+ "(" + NumberUtil.getPercentFormat(String.valueOf(NumberUtil.divide(result.getAbstain(),total+"",2)))+")");
        tv_veto_no_with_veto_rate.setText(result.getNo_with_veto()+ "(" + NumberUtil.getPercentFormat(String.valueOf(NumberUtil.divide(result.getNo_with_veto(),total+"",2)))+")");
    }


    private void updateUIState() {
        if (mProposalInfo.getStatus() == 1 || mProposalInfo.getStatus() == 5) {
            tv_pledge.setVisibility(View.VISIBLE);
            tv_pledge_title.setVisibility(View.VISIBLE);
            tv_veto_time_title.setVisibility(View.GONE);
            tv_veto_time1.setVisibility(View.GONE);
            tv_veto_time2.setVisibility(View.GONE);
            ll_veto_detail.setVisibility(View.GONE);
            divider_line1.setVisibility(View.VISIBLE);
            divider_line.setVisibility(View.GONE);
            btn_do_pledge.setVisibility(View.VISIBLE);
            btn_do_veto.setVisibility(View.GONE);
        } else if (mProposalInfo.getStatus() == 2 || mProposalInfo.getStatus() == 3 || mProposalInfo.getStatus() == 4) {

            tv_pledge.setVisibility(View.GONE);
            tv_pledge_title.setVisibility(View.GONE);

            tv_veto_time_title.setVisibility(View.VISIBLE);
            tv_veto_time1.setVisibility(View.VISIBLE);
            tv_veto_time2.setVisibility(View.VISIBLE);
            ll_veto_detail.setVisibility(View.VISIBLE);
            divider_line1.setVisibility(View.GONE);
            divider_line.setVisibility(View.VISIBLE);
            btn_do_pledge.setVisibility(View.GONE);
            btn_do_veto.setVisibility(View.VISIBLE);
        }
    }

    private String addressReplace(String originAddress) {
        if (originAddress == null || TextUtils.isEmpty(originAddress)) {
            return "";
        }
        if (originAddress.length() < 21) {
            return originAddress;
        }
        return originAddress.replace(originAddress.substring(10, originAddress.length() - 10), "...");
    }

    @Override
    protected void addEvent() {
        mProposalViewModel.proposalInfoLiveData.observe(this, ldm -> {
            swipeRefresh.finishRefresh();
            if (ldm.loadingStatus == LoadingStatus.SUCCESS) {
                updateRecord(ldm.getData());
            }
        });


        swipeRefresh.setOnRefreshListener(refreshLayout1 -> {
            getRecord(false);
        });
    }

    private void updateRecord(ProposalInfo data) {
        mProposalInfo = data;
        initProposalView();
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

    @OnClick({R2.id.btn_do_pledge, R2.id.btn_do_veto})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.btn_do_pledge) {
            ARouter.getInstance().build(ARouterConfig.Proposal.Do_Pledge)
                    .withObject("proposalInfo", mProposalInfo)
                    .navigation();

        } else if (view.getId() == R.id.btn_do_veto) {
            ARouter.getInstance().build(ARouterConfig.Proposal.Do_Veto)
                    .withObject("proposalInfo", mProposalInfo)
                    .navigation();
        }
    }

    private void getRecord(boolean isShowDialog) {
        mProposalViewModel.queryProposalById(this,
                mProposalInfo.getId(), isShowDialog);
    }

}
