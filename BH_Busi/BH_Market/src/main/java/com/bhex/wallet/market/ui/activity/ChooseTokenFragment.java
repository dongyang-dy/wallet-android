package com.bhex.wallet.market.ui.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bhex.lib.uikit.widget.RecycleViewDivider;
import com.bhex.lib.uikit.widget.editor.SimpleTextWatcher;
import com.bhex.network.mvx.base.BaseBottomSheetDialog;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.model.BHTokenMapping;
import com.bhex.wallet.market.R;
import com.bhex.wallet.market.R2;
import com.bhex.wallet.market.adapter.ChooseTokenAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @author gongdongyang
 * 2020-10-10 16:20:03
 */
public class ChooseTokenFragment extends BaseBottomSheetDialog {

    @BindView(R2.id.rec_token_list)
    RecyclerView rec_token_list;
    @BindView(R2.id.iv_close)
    AppCompatImageView iv_close;
    @BindView(R2.id.ed_search_content)
    AppCompatEditText ed_search_content;

    ChooseTokenAdapter mChooseTokenAdapter;

    private List<BHTokenMapping> mDatas;
    private List<BHTokenMapping> mSearchDatas;

    private String mIssueSymbol;

    public String mTargetSymbol;

    private ChooseTokenListener mChooseTokenListener;

    private int mOrigin;

    @Override
    public int getLayout() {
        return R.layout.fragment_choose_token;
    }

    @Override
    public void onStart() {
        super.onStart();
        setStyle(DialogFragment.STYLE_NO_TITLE, STYLE_NO_TITLE);

        Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getAttributes().windowAnimations = R.style.bottomDialogStyle;

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;

        window.setAttributes(params);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mOrigin==0){
            mDatas = CacheCenter.getInstance().getTokenMapCache().getTokenMappings();
            mChooseTokenAdapter = new ChooseTokenAdapter(mDatas,mIssueSymbol,mOrigin);
        }else{
            mDatas = CacheCenter.getInstance().getTokenMapCache().getTokenMapping(mIssueSymbol);
            mChooseTokenAdapter = new ChooseTokenAdapter(mDatas,mTargetSymbol,mOrigin);
        }
        mSearchDatas = new ArrayList<>(mDatas);

        /*LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rec_token_list.setLayoutManager(llm);*/

        RecycleViewDivider itemDecoration = new RecycleViewDivider(
                getContext(),LinearLayoutManager.VERTICAL,
                PixelUtils.dp2px(getContext(),1),
                ColorUtil.getColor(getContext(), R.color.global_divider_color));
        rec_token_list.addItemDecoration(itemDecoration);

        rec_token_list.setAdapter(mChooseTokenAdapter);

        iv_close.setOnClickListener(v -> {
            dismissAllowingStateLoss();
        });

        mChooseTokenAdapter.setOnItemClickListener((adapter, itemView, position) -> {
            dismissAllowingStateLoss();
            if(mChooseTokenListener==null){
                return;
            }
            BHTokenMapping tokenMapping = mDatas.get(position);
            mChooseTokenListener.clickTokenPosition(tokenMapping);
        });

        ed_search_content.addTextChangedListener(new SimpleTextWatcher(){
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                String keys = ed_search_content.getText().toString().trim();
                if(!TextUtils.isEmpty(keys)){
                    List<BHTokenMapping> resList = ChooseTokenFragment.this.filterKeysWord(keys);
                    ChooseTokenFragment.this.mChooseTokenAdapter.getData().clear();
                    mChooseTokenAdapter.addData(resList);
                }else{
                    mChooseTokenAdapter.getData().clear();
                    mDatas = new ArrayList<>(mSearchDatas);
                    mChooseTokenAdapter.addData(mDatas);
                }
            }
        });
    }

    public static ChooseTokenFragment showDialog(String symbol,int origin,ChooseTokenListener listener) {
        ChooseTokenFragment fragment = new ChooseTokenFragment();
        fragment.mIssueSymbol = symbol;
        fragment.mOrigin = origin;
        fragment.mChooseTokenListener = listener;
        return fragment;
    }

    public interface ChooseTokenListener{
        void clickTokenPosition(BHTokenMapping position);
    }

    public List<BHTokenMapping> filterKeysWord(String keys){
        List<BHTokenMapping> resList = new ArrayList<>();
        for(BHTokenMapping item: mSearchDatas){
            if(mOrigin==0){
                if(item.coin_symbol.toLowerCase().contains(keys.toLowerCase())){
                    resList.add(item);
                }
            }else{
                if(item.target_symbol.toLowerCase().contains(keys.toLowerCase())){
                    resList.add(item);
                }
            }

        }
        return resList;
    }

}