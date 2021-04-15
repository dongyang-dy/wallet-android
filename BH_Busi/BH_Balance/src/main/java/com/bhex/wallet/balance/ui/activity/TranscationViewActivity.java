package com.bhex.wallet.balance.ui.activity;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.tools.constants.BHConstants;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.R2;
import com.bhex.wallet.balance.adapter.TranscationAdapter;
import com.bhex.wallet.balance.helper.TransactionHelper;
import com.bhex.wallet.balance.viewmodel.TransactionViewModel;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.tx.TransactionOrder;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.util.List;

import butterknife.BindView;

/**
 *
 * 提取收益详情
 * @author gongdongyang
 * 2020-4-27 17:44:23
 * */

@Route(path = ARouterConfig.Balance.Balance_transcation_view,name="交易详情")
public class TranscationViewActivity extends TxBaseActivity {

    @Autowired(name = "transactionId")
    public String transactionId;
    @Autowired(name = BHConstants.SYMBOL)
    public String mSymbol;

    @BindView(R2.id.refreshLayout)
    SmartRefreshLayout refreshLayout;

    List<TransactionOrder.ActivitiesBean> mList;
    TranscationAdapter mdvAdapter;

    TransactionViewModel transactionViewModel;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_transcation_view;
    }

    @Override
    protected void initView() {
        super.initView();
        ARouter.getInstance().inject(this);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        recycler_reward.setLayoutManager(lm);

        recycler_reward.setNestedScrollingEnabled(false);
        mdvAdapter = new TranscationAdapter(mList);
        recycler_reward.setAdapter(mdvAdapter);
        refreshLayout.setEnableLoadMore(false);
    }

    @Override
    protected void addEvent() {
        super.addEvent();
        transactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);
        transactionViewModel.transLiveData.observe(this,ldm->{
            //更新交易详情
            updateTranscation(ldm);
        });

        refreshLayout.setOnRefreshListener(refreshLayout1 -> {
            transactionViewModel.queryTransactionDetail(this,transactionId);
        });
        refreshLayout.autoRefresh();
    }

    /**
     * 更新交易详情
     */
    private void updateTranscation(LoadDataModel ldm){
        refreshLayout.finishRefresh();
        if(ldm.loadingStatus == LoadingStatus.SUCCESS){
            mtxo = (TransactionOrder) ldm.getData();
            initBaseData();
            mList = mtxo.activities;
            mdvAdapter.getData().clear();
            mdvAdapter.addData(mtxo.activities);
            //TransactionHelper.displayTranscationAmount( tv_tranction_amount,mSymbol,mtxo);
        }else if(ldm.loadingStatus == LoadingStatus.ERROR){

        }
    }
}
