package com.bhex.wallet.bh_main.proposals.ui.activity;

import android.text.InputType;
import android.text.TextUtils;
import android.view.View;

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
import com.bhex.tools.utils.NumberUtil;
import com.bhex.wallet.balance.viewmodel.TransactionViewModel;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.R2;
import com.bhex.wallet.bh_main.proposals.model.ProposalInfo;
import com.bhex.wallet.bh_main.proposals.presenter.DoPledgePresenter;
import com.bhex.wallet.bh_main.proposals.viewmodel.ProposalViewModel;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
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
 * 质押
 */
@Route(path = ARouterConfig.Proposal.Do_Pledge)
public class DoPledgeActivity extends BaseActivity<DoPledgePresenter>  implements Password30PFragment.PasswordClickListener {

    @Autowired(name = "proposalInfo")
    ProposalInfo mProposalInfo;


    @BindView(R2.id.tv_tips)
    AppCompatTextView tv_tips;

    @BindView(R2.id.tv_available_amount)
    AppCompatTextView tv_available_amount;

    @BindView(R2.id.ed_pledge_amount)
    WithDrawInput ed_pledge_amount;

    @BindView(R2.id.ed_fee)
    WithDrawInput ed_fee;

    @BindView(R2.id.ed_real_amount)
    WithDrawInput ed_real_amount;

    @BindView(R2.id.tv_tips_1)
    AppCompatTextView tv_tips_1;
    @BindView(R2.id.tv_tips_2)
    AppCompatTextView tv_tips_2;
    @BindView(R2.id.tv_tips_3)
    AppCompatTextView tv_tips_3;

    @BindView(R2.id.btn_do_pledge)
    MaterialButton btn_do_pledge;
    @BindView(R2.id.refreshLayout)
    SmartRefreshLayout refreshLayout;

    private String token = BHConstants.BHT_TOKEN;

    private String available_amount;

    ProposalViewModel mProposalViewModel;
    TransactionViewModel mTransactionViewModel;
    @Override
    protected void initPresenter() {
        mPresenter = new DoPledgePresenter(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_do_pledge;
    }

    @Override
    protected void initView() {

        ARouter.getInstance().inject(this);
        initUI();
    }


    private void initUI() {
        refreshLayout.setEnableLoadMore(false);
        ed_pledge_amount.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        ed_real_amount.btn_right_text.setText(token.toUpperCase());
        ed_fee.btn_right_text.setText(token.toUpperCase());
        ed_fee.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        tv_available_amount.setText(getString(R.string.available_format,getString(R.string.string_placeholder) + token.toUpperCase()));
    }

    @Override
    protected void addEvent() {
        mProposalViewModel = ViewModelProviders.of(this).get(ProposalViewModel.class);
        mProposalViewModel.doPledgeLiveData.observe(this, ldm -> {
            updateDoPledgeStatus(ldm);
        });

        mTransactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);
        mTransactionViewModel.mutableLiveData.observe(this,ldm -> {
            updateDoPledgeStatus(ldm);
        });
        ed_pledge_amount.btn_right_text.setOnClickListener(allListener);

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


    private void updateDoPledgeStatus(LoadDataModel ldm) {
        if (ldm.loadingStatus == LoadingStatus.SUCCESS) {
            ToastUtils.showToast(getString(R.string.do_pledge_succeed));
            finish();
        } else {
            ToastUtils.showToast(getString(R.string.do_pledge_failed));
        }
    }

    @OnClick({R2.id.btn_do_pledge})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.btn_do_pledge) {
            sendSubmit();
        }
    }

    /**
     * 发送交易
     */
    private void sendSubmit() {
        boolean flag = mPresenter.checkDoPledge(mProposalInfo,
                ed_pledge_amount.getInputString(),
                String.valueOf(available_amount),
                ed_fee.getInputString()
        );
        if (!flag) {
            return;
        }

        Password30PFragment.showPasswordDialog(getSupportFragmentManager(),
                Password30PFragment.class.getName(),
                this,0,true);
    }


    public View.OnClickListener allListener = v -> {
        if (available_amount == null || TextUtils.isEmpty(available_amount)) {
            ed_pledge_amount.getEditText().setText("");
            return;
        }
        String fee = ed_fee.getInputString();
        String all_count = NumberUtil.sub(String.valueOf(available_amount), fee);
        ed_pledge_amount.getEditText().setText(all_count);
    };

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

        //String delegator_address = BHUserManager.getInstance().getCurrentBhWallet().getAddress();
        //BigInteger gasPrice = BigInteger.valueOf((long) (BHConstants.BHT_GAS_PRICE));
        String pledge_amount = ed_pledge_amount.getInputString();
        String feeAmount = ed_fee.getInputString();


       /* BHTransactionManager.loadSuquece(suquece -> {
            BHSendTranscation bhSendTranscation = BHTransactionManager.doPledge(delegator_address,mProposalInfo.getId(), pledge_amount, feeAmount,
                    gasPrice, password, suquece, token);
            mProposalViewModel.sendDoPledge(this, bhSendTranscation);
            return 0;
        });*/

        List<TxReq.TxMsg> tx_msg_list = BHRawTransaction.createPledgeMsg(mProposalInfo.getId(),pledge_amount,BHConstants.BHT_TOKEN);
        mTransactionViewModel.transferInnerExt(this,password,feeAmount,tx_msg_list);
    }
}
