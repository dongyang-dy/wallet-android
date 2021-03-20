package com.bhex.wallet.bh_main.my.ui.fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.callback.NavigationCallback;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.EmptyLayout;
import com.bhex.lib.uikit.widget.MsgView;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.tools.constants.BHConstants;
import com.bhex.wallet.common.base.BaseFragment;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.R2;
import com.bhex.wallet.bh_main.my.adapter.MessageAdapter;
import com.bhex.wallet.bh_main.my.model.BHMessage;
import com.bhex.wallet.bh_main.my.ui.activity.MessageActivity;
import com.bhex.wallet.bh_main.my.viewmodel.MessageViewModel;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.model.BHPage;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @author gongdongyang
 * 2020-5-21 18:22:29
 * 消息列表
 */
public class MessageListFragment extends BaseFragment implements OnRefreshLoadMoreListener {

    public final static String Message_Type = "messageType";
    private MessageAdapter messageAdapter;
    private List<BHMessage> mList;

    @BindView(R2.id.recycler_message)
    RecyclerView recycler_message;
    @BindView(R2.id.empty_layout)
    EmptyLayout empty_layout;
    @BindView(R2.id.refreshLayout)
    SmartRefreshLayout refreshLayout;


    private int mCurrentPage = 1;
    private MessageViewModel messageViewModel;
    private String mType;
    private int mClickPosition = -1;

    @Override
    protected void addEvent() {
        messageViewModel.messageLiveData.observe(this, ldm -> {
            updateMessageList(ldm);
        });
        messageViewModel.loadMessageByAddress(this, mCurrentPage, mType);
        messageAdapter.setOnItemClickListener((adapter, view, position) -> {
            BHMessage bhm = mList.get(position);
            Postcard postcard = ARouter.getInstance().build(ARouterConfig.Balance.Balance_transcation_view)
                    .withString("transactionId",bhm.tx_hash);
            LogisticsCenter.completion(postcard);
            Intent intent = new Intent(getActivity(), postcard.getDestination());
            intent.putExtras(postcard.getExtras());
            //intent.putExtra("position",position);
            mClickPosition = position;
            startActivityForResult(intent,100);
            if(!bhm.read){
                messageViewModel.updateMessageStatus(this,String.valueOf(bhm.id),mType);
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_message_list;
    }

    @Override
    protected void initView() {
        mList = new ArrayList<>();
        messageViewModel = ViewModelProviders.of(this).get(MessageViewModel.class);

        mType = getArgumentValue(MessageListFragment.Message_Type);

        LinearLayoutManager llm = new LinearLayoutManager(getYActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recycler_message.setLayoutManager(llm);

        messageAdapter = new MessageAdapter(mList,mType);
        recycler_message.setAdapter(messageAdapter);

        refreshLayout.setOnRefreshLoadMoreListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100 && mClickPosition>=0){
            //更新
            BHMessage bhm = mList.get(mClickPosition);
            if(bhm.read){
                return;
            }
            bhm.read = true;
            messageAdapter.notifyItemChanged(mClickPosition);
            //更新消息未读数量
            MsgView msgView = ((MessageActivity)getYActivity()).getTab().getMsgView(Integer.valueOf(mType)-1);
            String text = msgView.getText().toString();

            if(!TextUtils.isEmpty(text)){
                if(Integer.valueOf(text)>1){
                    ((MessageActivity)getYActivity()).getTab().showMsg(Integer.valueOf(mType)-1,Integer.valueOf(text)-1);
                }else{
                    ((MessageActivity)getYActivity()).getTab().hideMsg(Integer.valueOf(mType)-1);
                }
            }



        }
    }

    /**
     * 更新message列表
     *
     * @param ldm
     */
    BHPage<BHMessage> page = null;
    private void updateMessageList(LoadDataModel ldm) {
        if(ldm.getLoadingStatus() == LoadingStatus.SUCCESS){
            page = (BHPage) ldm.getData();
            if(mCurrentPage==1){
                messageAdapter.getData().clear();
                mList.clear();
                mList = page.items;
            }
            if(page.items!=null && page.items.size()>0){
                //mList.addAll(page.notifications);
                messageAdapter.addData(page.items);
                mCurrentPage++;
            }


            if(messageAdapter.getData()==null || messageAdapter.getData().size()==0){
                empty_layout.showNoData();
            }else{
                empty_layout.loadSuccess();
                if(messageAdapter.getData().size()< BHConstants.PAGE_SIZE){
                    refreshLayout.setEnableLoadMore(false);
                }else{
                    refreshLayout.setEnableLoadMore(true);
                }
            }
            if(page.unread>0){
                ((MessageActivity)getYActivity()).getTab().showMsg(Integer.valueOf(mType)-1,page.unread);
            }

            refreshLayout.finishLoadMore(1000);
            refreshLayout.finishRefresh();
        }else{
            empty_layout.showNeterror(null);
        }
    }

    private String getArgumentValue(String key){
        String result = "";
        if(getArguments()!=null){
            result = getArguments().getString(key,"0");
        }
        return result;
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        messageViewModel.loadMessageByAddress(this, mCurrentPage, mType);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        mCurrentPage = 1;
        messageViewModel.loadMessageByAddress(this, mCurrentPage, mType);

    }
}
