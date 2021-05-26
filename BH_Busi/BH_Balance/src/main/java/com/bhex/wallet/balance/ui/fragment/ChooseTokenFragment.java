package com.bhex.wallet.balance.ui.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.lib.uikit.widget.EmptyLayout;
import com.bhex.lib.uikit.widget.RecycleViewExtDivider;
import com.bhex.lib.uikit.widget.editor.SimpleTextWatcher;
import com.bhex.network.mvx.base.BaseDialogFragment;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.adapter.ChooseTokenAdapter;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.balance.helper.BHTokenHelper;
import com.bhex.wallet.common.model.BHToken;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToDoubleFunction;

import java8.util.stream.Collectors;
import java8.util.stream.Stream;
import java8.util.stream.StreamSupport;

/**
 * 选择币种
 * 2020-12-4 09:36:15
 */
public class ChooseTokenFragment extends BaseDialogFragment {

    private List<BHToken> mDatas;
    private String mSymbol;
    //private String mOrigin;
    private ChooseTokenAdapter mChooseTokenAdapter;
    private RecyclerView rec_token_list;
    AppCompatImageView iv_close;
    AppCompatEditText input_search_content;
    EmptyLayout empty_layout;
    AppCompatImageView btn_sort;
    private OnChooseTokenListener mOnChooseItemListener;
    @Override
    public int getLayout() {
        return R.layout.balance_fragment_choose_token;
    }

    @Override
    public void initStart() {
        setStyle(DialogFragment.STYLE_NO_TITLE, STYLE_NO_TITLE);
        Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getAttributes().windowAnimations = R.style.bottomDialogStyle;

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = dm.heightPixels-PixelUtils.dp2px(getContext(),120);
        window.setAttributes(params);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rec_token_list = mRootView.findViewById(R.id.rec_token_list);
        //mDatas = BHBalanceHelper.loadTokenList(mSymbol,mOrigin);
        mDatas = BHTokenHelper.loadTokenByChain(BHConstants.BHT_TOKEN);
        rec_token_list.setAdapter(mChooseTokenAdapter = new ChooseTokenAdapter(mDatas,mSymbol));

        RecycleViewExtDivider ItemDecoration = new RecycleViewExtDivider(
                getContext(), LinearLayoutManager.VERTICAL,
                PixelUtils.dp2px(getActivity(),65),0,
                ColorUtil.getColor(getContext(),R.color.global_divider_color));

        rec_token_list.addItemDecoration(ItemDecoration);

        mChooseTokenAdapter.setOnItemClickListener(this::onItemClick);
    }

    private void onItemClick(BaseQuickAdapter<?,?> baseQuickAdapter, View view, int position) {
        BHToken item = mChooseTokenAdapter.getData().get(position);
        if(getDialog()!=null && getDialog().isShowing()){
            dismissAllowingStateLoss();
        }
        if(mOnChooseItemListener==null){
            return;
        }
        mOnChooseItemListener.onChooseClickListener(item.symbol,position);
    }

    boolean isAsc = true;
    @Override
    protected void initView() {
        //设置背景色
        GradientDrawable bg_drawable = ShapeUtils.getRoundRectTopDrawable(PixelUtils.dp2px(getActivity(),16),
                ColorUtil.getColor(getActivity(),R.color.dialog_fragment_background),true,0);
        mRootView.setBackground(bg_drawable);

        iv_close = mRootView.findViewById(R.id.iv_close);
        empty_layout = mRootView.findViewById(R.id.empty_layout);
        input_search_content = mRootView.findViewById(R.id.input_search_content);
        iv_close.setOnClickListener(v -> {
            dismissAllowingStateLoss();
        });
        input_search_content.addTextChangedListener(mTextWatcher);
        btn_sort = mRootView.findViewById(R.id.btn_sort);
        btn_sort.setOnClickListener(v -> {
            List<BHToken> result = new ArrayList<>(mDatas);
            isAsc=!isAsc;
            if(isAsc){
                btn_sort.setImageResource(R.mipmap.ic_sort_asc);
            }else{
                btn_sort.setImageResource(R.mipmap.ic_sort_desc);
            }
            sortResult(isAsc,result);
            mChooseTokenAdapter.setNewInstance(result);
        });
    }

    public static ChooseTokenFragment showFragment(String symbol,OnChooseTokenListener listener){
        ChooseTokenFragment fragment = new ChooseTokenFragment();
        fragment.mOnChooseItemListener = listener;
        //fragment.mOrigin = origin;
        fragment.mSymbol = symbol;
        return  fragment;
    }

    //设置点击
    private SimpleTextWatcher mTextWatcher = new SimpleTextWatcher(){
        @Override
        public void afterTextChanged(Editable s) {
            String search_key = input_search_content.getText().toString().trim();
            if(TextUtils.isEmpty(search_key)){
                sortResult(isAsc,mDatas);
                mChooseTokenAdapter.setNewData(mDatas);
               return;
            }
            List<BHToken> result = StreamSupport.stream(mDatas).filter(item->
                    item.symbol.toLowerCase().contains(search_key.toLowerCase())
            ).collect(Collectors.toList());

            sortResult(isAsc,result);

            if(ToolUtils.checkListIsEmpty(result)){
                empty_layout.showNoData();
                mChooseTokenAdapter.setNewData(result);
                return;
            }

            empty_layout.loadSuccess();
            mChooseTokenAdapter.setNewData(result);
        }
    };

    //是否升序
    private void sortResult(boolean isAsc,List<BHToken> result){
        if(isAsc){
            Collections.sort(result,((o1, o2) -> {
                String n1 =  o1.name;
                String n2 =  o2.name;
                return n1.compareTo(n2);
            }));
        }else{
            Collections.sort(result,((o1, o2) -> {
                String n1 =  o1.name;
                String n2 =  o2.name;
                return n2.compareTo(n1);
            }));
        }
    }
    public interface OnChooseTokenListener{
        void onChooseClickListener(String symbol,int position);
    }


    public void setOnChooseItemListener(OnChooseTokenListener mOnChooseItemListener) {
        this.mOnChooseItemListener = mOnChooseItemListener;
    }
}