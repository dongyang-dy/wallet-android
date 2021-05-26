package com.bhex.wallet.balance.ui.activity;

import android.text.Editable;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckedTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.lib.uikit.widget.EmptyLayout;
import com.bhex.lib.uikit.widget.RecycleViewExtDivider;
import com.bhex.lib.uikit.widget.editor.SimpleTextWatcher;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.R2;
import com.bhex.wallet.balance.adapter.CoinSearchAdapter;
import com.bhex.wallet.balance.helper.BHTokenHelper;
import com.bhex.wallet.balance.viewmodel.TokenViewModel;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.model.BHToken;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.List;

import butterknife.BindView;

/**
 * @author gongdongyang
 * 2020-4-3 15:27:32
 * 币种搜索
 */
@Route(path = ARouterConfig.Balance.Balance_Search)
public class CoinSearchActivity extends BaseActivity implements OnRefreshListener {

    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;
    @BindView(R2.id.ed_search_content)
    AppCompatEditText ed_search_content;
    @BindView(R2.id.recycler_coin)
    RecyclerView recycler_coin;
    @BindView(R2.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R2.id.empty_layout)
    EmptyLayout empty_layout;

    /*@Autowired(name= BHConstants.CHAIN)
    public String mChain;*/

    CoinSearchAdapter mCoinSearchAdapter;

    private List<BHToken> mTokenList;

    private TokenViewModel mTokenViewModel;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_coin_search;
    }

    @Override
    protected void initView() {
        tv_center_title.setText(getResources().getString(R.string.add_coin));
        ARouter.getInstance().inject(this);

        mTokenList = BHTokenHelper.loadVerifiedToken(BHConstants.BHT_TOKEN);


        recycler_coin.setAdapter(mCoinSearchAdapter = new CoinSearchAdapter(mTokenList));

        RecycleViewExtDivider ItemDecoration = new RecycleViewExtDivider(
                this,LinearLayoutManager.VERTICAL,

                PixelUtils.dp2px(this,68),
                0,
                ColorUtil.getColor(this,R.color.global_divider_color));
        recycler_coin.addItemDecoration(ItemDecoration);

        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setEnableLoadMore(false);
    }

    @Override
    protected void addEvent() {
        mTokenViewModel = ViewModelProviders.of(this).get(TokenViewModel.class);

        mTokenViewModel.searchLiveData.observe(this,ldm -> {
            refreshLayout.finishRefresh();
            updateTokenList(ldm);
        });

        mTokenViewModel.queryLiveData.observe(this,ldm->{
            refreshLayout.finishRefresh();
            updateTokenList(ldm);
        });

        mCoinSearchAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if(view.getId()==R.id.ck_select){
                //
                BHToken bhToken = (BHToken) adapter.getData().get(position);

                CheckedTextView ck = (CheckedTextView) view;
                ck.toggle();
                //添加取消币种
                mTokenViewModel.addOrCancelToken(ck.isChecked(),bhToken);
            }
        });
        ed_search_content.addTextChangedListener(searchTextWatcher);
        ed_search_content.setOnEditorActionListener(onEditorActionListener);
    }

    private void updateTokenList(LoadDataModel ldm) {
        mCoinSearchAdapter.getData().clear();
        if(ldm.getLoadingStatus()==LoadingStatus.SUCCESS){
            if(ldm.getData()==null || ((List<BHToken>)ldm.getData()).size()==0){
                empty_layout.showNoData();
            }else{
                empty_layout.loadSuccess();
                mCoinSearchAdapter.addData((List<BHToken>)ldm.getData());
            }
            mCoinSearchAdapter.notifyDataSetChanged();
        }else{
            empty_layout.showNeterror(null);
            mCoinSearchAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 搜索
     */
    private SimpleTextWatcher searchTextWatcher = new SimpleTextWatcher(){
        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            String search_key = ed_search_content.getText().toString().trim();
            if(!TextUtils.isEmpty(search_key)){
                return;

            }
            mTokenList = BHTokenHelper.loadVerifiedToken(null);
            if(ToolUtils.checkListIsEmpty(mTokenList)){
                empty_layout.showNoData();
            }else {
                empty_layout.loadSuccess();
            }
            mCoinSearchAdapter.getData().clear();
            mCoinSearchAdapter.addData(mTokenList);
            mCoinSearchAdapter.notifyDataSetChanged();
        }
    };


    TextView.OnEditorActionListener onEditorActionListener = (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            searchTokenByName();
            ToolUtils.hintKeyBoard(CoinSearchActivity.this);
            return true;
        }
        return false;
    };

    //搜索节点
    private void searchTokenByName() {
        String search_key = ed_search_content.getText().toString().trim();
        if(!TextUtils.isEmpty(search_key)){
            mCoinSearchAdapter.getData().clear();
            mCoinSearchAdapter.notifyDataSetChanged();
            empty_layout.showProgess();
            mTokenViewModel.search_token(this,search_key.toLowerCase(),BHConstants.BHT_TOKEN);
        }
    }


    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        String search_key = ed_search_content.getText().toString().trim();
        //empty_layout.showProgess();
        if(!TextUtils.isEmpty(search_key)){
            mTokenViewModel.search_token(this,search_key.toLowerCase(),null);
        }else{
            mTokenViewModel.loadVerifiedToken(this,null);
        }
    }
}
