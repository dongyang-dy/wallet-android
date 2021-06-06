package com.bhex.wallet.bh_main.proposals.ui.activity;

import android.text.InputType;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.editor.WithDrawInput;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.wallet.balance.viewmodel.TransactionViewModel;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.R2;
import com.bhex.wallet.bh_main.proposals.model.ProposalInfo;
import com.bhex.wallet.bh_main.proposals.presenter.DoVetoPresenter;
import com.bhex.wallet.bh_main.proposals.viewmodel.ProposalViewModel;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.AccountInfo;
import com.bhex.wallet.common.tx.BHRawTransaction;
import com.bhex.wallet.common.tx.TxReq;
import com.bhex.wallet.common.ui.fragment.Password30PFragment;
import com.bhex.wallet.common.utils.LiveDataBus;
import com.google.android.material.button.MaterialButton;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author zc
 * 2020-4-16
 * 投票
 */
@Route(path = ARouterConfig.Proposal.Do_Veto)
public class DoVetoActivity extends BaseActivity<DoVetoPresenter> implements Password30PFragment.PasswordClickListener  {

    @Autowired(name = "proposalInfo")
    ProposalInfo mProposalInfo;


    @BindView(R2.id.tv_tips)
    AppCompatTextView tv_tips;

    @BindView(R2.id.tv_available_amount)
    AppCompatTextView tv_available_amount;


    @BindView(R2.id.ed_fee)
    WithDrawInput ed_fee;
    @BindView(R2.id.iv_option_yes)
    ImageView iv_option_yes;
    @BindView(R2.id.tv_option_yes)
    AppCompatTextView tv_option_yes;
    @BindView(R2.id.iv_option_no)
    ImageView iv_option_no;
    @BindView(R2.id.tv_option_no)
    AppCompatTextView tv_option_no;
    @BindView(R2.id.iv_option_abstain)
    ImageView iv_option_abstain;
    @BindView(R2.id.tv_option_abstain)
    AppCompatTextView tv_option_abstain;

    @BindView(R2.id.iv_option_with_no_veto)
    ImageView iv_option_with_no_veto;
    @BindView(R2.id.tv_option_no_with_veto)
    AppCompatTextView tv_option_no_with_veto;

    @BindView(R2.id.btn_do_veto)
    MaterialButton btn_do_veto;
    @BindView(R2.id.refreshLayout)
    SmartRefreshLayout refreshLayout;

    @BindView(R2.id.rl_yes_option)
    RelativeLayout rl_yes_option;
    @BindView(R2.id.rl_no_option)
    RelativeLayout rl_no_option;
    @BindView(R2.id.rl_abstain_option)
    RelativeLayout rl_abstain_option;
    @BindView(R2.id.rl_no_with_veto_option)
    RelativeLayout rl_no_with_veto_option;

    private String token = BHConstants.BHT_TOKEN;

    String mAvailabelTitle = "";

    private String available_amount;

    ProposalViewModel mProposalViewModel;
    TransactionViewModel transactionViewModel;
    private String mOption = "";

    @Override
    protected void initPresenter() {
        mPresenter = new DoVetoPresenter(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_do_veto;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        initUI();
    }


    private void initUI() {
        refreshLayout.setEnableLoadMore(false);
        ed_fee.btn_right_text.setText(token.toUpperCase());
        ed_fee.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        tv_available_amount.setText(getString(R.string.available_format,getString(R.string.string_placeholder) + token.toUpperCase()));
    }

    @Override
    protected void addEvent() {
        mProposalViewModel = ViewModelProviders.of(this).get(ProposalViewModel.class);
        mProposalViewModel.doVetoLiveData.observe(this, ldm -> {
            updateDoVetoStatus(ldm);
        });

        transactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);
        transactionViewModel.mutableLiveData.observe(this,ldm -> {
            updateDoVetoStatus(ldm);
        });

        LiveDataBus.getInstance().with(BHConstants.Label_Account, LoadDataModel.class).observe(this, ldm->{
            refreshLayout.finishRefresh();
            if(ldm.loadingStatus==LoadingStatus.SUCCESS){
                updateAssets((AccountInfo) ldm.getData());
            }
        });
        queryAssetInfo(true);

        refreshLayout.setOnRefreshListener(refreshLayout1 -> {
            queryAssetInfo(false);
        });
    }

    private void queryAssetInfo(boolean isShowProgressDialog) {
        mProposalViewModel.getAccountInfo(this, isShowProgressDialog);
    }


    private void updateDoVetoStatus(LoadDataModel ldm) {
        if (ldm.loadingStatus == LoadingStatus.SUCCESS) {
            ToastUtils.showToast(getString(R.string.do_veto_succeed));
            finish();
        } else {
            ToastUtils.showToast(getString(R.string.do_veto_failed));
        }
    }

    @OnClick({R2.id.btn_do_veto, R2.id.rl_yes_option, R2.id.rl_no_option, R2.id.rl_abstain_option, R2.id.rl_no_with_veto_option})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.btn_do_veto) {
            sendSubmit();
        } else if (view.getId() == R.id.rl_yes_option) {
            updateCurrentOption(BHConstants.VETO_OPTION_YES);
        } else if (view.getId() == R.id.rl_no_option) {
            updateCurrentOption(BHConstants.VETO_OPTION_NO);
        } else if (view.getId() == R.id.rl_abstain_option) {
            updateCurrentOption(BHConstants.VETO_OPTION_ABSTAIN);
        } else if (view.getId() == R.id.rl_no_with_veto_option) {
            updateCurrentOption(BHConstants.VETO_OPTION_NOWITHVETO);
        }
    }

    private void updateCurrentOption(String vetoOption) {
        mOption = vetoOption;
        if (vetoOption == BHConstants.VETO_OPTION_YES) {
            iv_option_yes.setVisibility(View.GONE);
            rl_yes_option.setBackgroundResource(R.drawable.bg_green_corners);
            tv_option_yes.setTextColor(getResources().getColor(R.color.vote_option_selected_text));
        } else {
            iv_option_yes.setVisibility(View.VISIBLE);
            rl_yes_option.setBackgroundResource(R.drawable.input_bg_gray_corner);
            tv_option_yes.setTextColor(getResources().getColor(R.color.vote_option_text));
        }

        if (vetoOption == BHConstants.VETO_OPTION_NO) {
            iv_option_no.setVisibility(View.GONE);
            rl_no_option.setBackgroundResource(R.drawable.bg_red_corners);
            tv_option_no.setTextColor(getResources().getColor(R.color.vote_option_selected_text));
        } else {
            iv_option_no.setVisibility(View.VISIBLE);
            rl_no_option.setBackgroundResource(R.drawable.input_bg_gray_corner);
            tv_option_no.setTextColor(getResources().getColor(R.color.vote_option_text));
        }

        if (vetoOption == BHConstants.VETO_OPTION_ABSTAIN) {
            iv_option_abstain.setVisibility(View.GONE);
            rl_abstain_option.setBackgroundResource(R.drawable.bg_dark_blue_corners);
            tv_option_abstain.setTextColor(getResources().getColor(R.color.vote_option_selected_text));
        } else {
            iv_option_abstain.setVisibility(View.VISIBLE);
            rl_abstain_option.setBackgroundResource(R.drawable.input_bg_gray_corner);
            tv_option_abstain.setTextColor(getResources().getColor(R.color.vote_option_text));
        }

        if (vetoOption == BHConstants.VETO_OPTION_NOWITHVETO) {
            iv_option_with_no_veto.setVisibility(View.GONE);
            rl_no_with_veto_option.setBackgroundResource(R.drawable.bg_proposal_voting_corners);
            tv_option_no_with_veto.setTextColor(getResources().getColor(R.color.vote_option_selected_text));
        } else {
            iv_option_with_no_veto.setVisibility(View.VISIBLE);
            rl_no_with_veto_option.setBackgroundResource(R.drawable.input_bg_gray_corner);
            tv_option_no_with_veto.setTextColor(getResources().getColor(R.color.vote_option_text));
        }
    }

    /**
     * 发送交易
     */
    private void sendSubmit() {
        boolean flag = mPresenter.checkDoVeto(mProposalInfo, mOption,
                String.valueOf(available_amount),ed_fee.getInputString());

        if (!flag) {
            return;
        }

        Password30PFragment.showPasswordDialog(getSupportFragmentManager(),
                Password30PFragment.class.getName(),
                this,0,true);
    }

    private void updateAssets(AccountInfo data) {
        if (data == null) {
            return;
        }
        List<AccountInfo.AssetsBean> list = data.assets;
        if (list == null || list.size() == 0) {
            return;
        }
        for (AccountInfo.AssetsBean item : list) {
            if (item.symbol.equalsIgnoreCase(token)) {
                available_amount = mPresenter.getAmountForUser(item.amount, item.frozen_amount, token);
                tv_available_amount.setText(getString(R.string.available_format,available_amount + token.toUpperCase()));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void confirmAction(String password, int position,int way) {
        String delegator_address = BHUserManager.getInstance().getCurrentBhWallet().getAddress();
        String feeAmount = ed_fee.getInputString();

       /* BHTransactionManager.loadSuquece(suquece -> {
            BHSendTranscation bhSendTranscation = BHTransactionManager.doVeto(delegator_address, mProposalInfo.getId(), mOption, feeAmount,
                    gasPrice,password, suquece, token);
            mProposalViewModel.sendDoVeto(this, bhSendTranscation);
            return 0;
        });*/

        List<TxReq.TxMsg> tx_msg_list = BHRawTransaction.createVoteMsg(delegator_address,mOption,mProposalInfo.getId());
        transactionViewModel.transferInnerExt(this,password,feeAmount,tx_msg_list);
    }
}
