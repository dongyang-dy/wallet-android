package com.bhex.wallet.bh_main.my.ui.fragment;


import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.network.base.LoadingStatus;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.R2;
import com.bhex.wallet.bh_main.my.adapter.MyAdapter;
import com.bhex.wallet.bh_main.my.enums.BUSI_MY_TYPE;
import com.bhex.wallet.bh_main.my.helper.MyHelper;
import com.bhex.wallet.bh_main.my.model.BHMessage;
import com.bhex.wallet.bh_main.my.ui.decoration.MyRecyclerViewDivider;
import com.bhex.wallet.bh_main.my.ui.item.MyItem;
import com.bhex.wallet.bh_main.my.viewmodel.MessageViewModel;
import com.bhex.wallet.common.base.BaseFragment;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.enums.BH_BUSI_URL;
import com.bhex.wallet.common.helper.BHWalletHelper;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.BHPage;
import com.bhex.wallet.common.utils.BHTestUtil;

import java.util.List;

import butterknife.BindView;

/**
 * @author gdy
 * 2020-3-12
 * 我的
 */
public class MyFragment extends BaseFragment  {

    @BindView(R2.id.rec_my_function)
    RecyclerView rec_my_function;

    @BindView(R2.id.tv_username)
    AppCompatTextView tv_username;

    @BindView(R2.id.tv_address)
    AppCompatTextView tv_address;
    //
    private List<MyItem> mItems;
    private MyAdapter mMyAdapter;
    private BHWallet mCurrentWallet;

    private MessageViewModel msgViewModel;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_my;
    }

    @Override
    protected void initView() {
        msgViewModel = ViewModelProviders.of(this).get(MessageViewModel.class);

        mItems = MyHelper.getAllItems(getYActivity());
        rec_my_function.setNestedScrollingEnabled(false);

        MyRecyclerViewDivider myRecyclerDivider = new MyRecyclerViewDivider(getContext(),
                ColorUtil.getColor(getContext(),R.color.page_bg_color),
                getResources().getDimension(R.dimen.item_large_divider_height),
                new int[]{1,3});
        rec_my_function.addItemDecoration(myRecyclerDivider);
        rec_my_function.setAdapter(mMyAdapter = new MyAdapter( mItems));

    }

    @Override
    protected void addEvent() {
        mMyAdapter.setOnItemClickListener((adapter, view, position) -> {
            MyItem item = mItems.get(position);
            switch (BUSI_MY_TYPE.getType(item.id)){
                case 账户与安全:
                    //
                    ARouter.getInstance().build(ARouterConfig.My.My_Account_Security).navigation();
                    break;
                case 备份导出:
                    ARouter.getInstance().build(ARouterConfig.My.My_Account_Backup).navigation();
                    break;
                case 公告:
                    ARouter.getInstance().build(ARouterConfig.Market.market_webview)
                            .withString("url",BH_BUSI_URL.公告.getGotoUrl(getContext()))
                            .navigation();
                    break;
                case 消息中心:
                    ARouter.getInstance().build(ARouterConfig.My.My_Message).navigation();
                    break;
                case 设置:
                    ARouter.getInstance().build(ARouterConfig.My.My_Account_Setting).navigation();
                    break;
                case 帮助中心:
                    ARouter.getInstance().build(ARouterConfig.Market.market_webview)
                            .withString("url",BH_BUSI_URL.帮助中心.getGotoUrl(getContext()))
                            .navigation();
                    break;
                case 关于我们:
                    ARouter.getInstance().build(ARouterConfig.My.My_About).navigation();
                    break;
            }
        });

        //复制地址
        mRootView.findViewById(R.id.btn_copy).setOnClickListener(v->{
            ToolUtils.copyText(mCurrentWallet.getAddress(),getYActivity());
            ToastUtils.showToast(getResources().getString(R.string.copyed));
        });

        //复制公钥
        mRootView.findViewById(R.id.btn_copy).setOnLongClickListener(v -> {
            ToolUtils.copyText(mCurrentWallet.publicKey,getYActivity());
            ToastUtils.showToast(getResources().getString(R.string.public_key_copy));
            return true;
        });
        //跳转交易记录
        mRootView.findViewById(R.id.layout_index_0).setOnClickListener(v->{
            ARouter.getInstance().build(ARouterConfig.Market.market_webview).withString("url",getTranscationUrl()).navigation();
        });
        //跳转账户管理
        mRootView.findViewById(R.id.layout_index_1).setOnClickListener(v->{
            ARouter.getInstance().build(ARouterConfig.ACCOUNT_MANAGER_PAGE).navigation();
        });

        //消息查询
        msgViewModel.messageLiveData.observe(this,ldm->{
            if(ldm.getLoadingStatus()== LoadingStatus.SUCCESS){
                BHPage<BHMessage> page =  (BHPage<BHMessage>)ldm.getData();
                mMyAdapter.changeMessageCount(page.unread);
            }
        });

        /*mRootView.findViewById(R.id.iv_default_man).setOnClickListener(v -> {
            BHTestUtil.助记词到地址(getYActivity());
        });*/
    }


    private String getTranscationUrl(){
        String v_local_display = ToolUtils.getLocalString(getYActivity());
        String url = BHConstants.API_BASE_URL
                .concat("account/")
                .concat(BHUserManager.getInstance().getCurrentBhWallet().address)
                .concat("?type=transactions").concat("&lang=").concat(v_local_display);
        LogUtils.d("MyFragement==>:","url=="+url);
        return url;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            msgViewModel.loadMessageByAddress(this,1,null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mCurrentWallet = BHUserManager.getInstance().getCurrentBhWallet();
        tv_username.setText(mCurrentWallet.getName());
        BHWalletHelper.proccessAddress(tv_address,mCurrentWallet.getAddress());

        mMyAdapter.notifyItemChanged(3);
        msgViewModel.loadMessageByAddress(this,1,null);
    }
}
