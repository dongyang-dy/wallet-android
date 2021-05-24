package com.bhex.wallet.balance.ui.fragment;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.network.mvx.base.BaseDialogFragment;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.ImageLoaderUtil;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.model.BHChain;
import com.bhex.wallet.common.model.BHToken;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gongdongyang
 * 2021-3-20 22:14:07
 */
public class ChooseChainFragment extends BaseDialogFragment {

    private ChainAdapter chainAdapter;
    private RecyclerView rec_chain_list;
    private String mChain;
    private List<BHChain> mChainList;
    private ChooseChainListener mListener;

    @Override
    public int getLayout() {
        return R.layout.fragment_choose_chain;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        GradientDrawable drawable = ShapeUtils.getRoundRectTopDrawable(PixelUtils.dp2px(getContext(), 6), ColorUtil.getColor(getContext(), R.color.app_bg), true, 0);
        mRootView.setBackground(drawable);

        //List<BHChain> bhChainList = new ArrayList<>(CacheCenter.getInstance().getTokenMapCache().loadChains().values());
        rec_chain_list = mRootView.findViewById(R.id.rec_chain_list);
        rec_chain_list.setAdapter(chainAdapter = new ChainAdapter(mChainList));

        chainAdapter.setOnItemClickListener((adapter, view1, position) -> {
            BHChain bhChain = chainAdapter.getData().get(position);
            if(mListener!=null){
                mListener.chooseChainAction(bhChain.chain);
            }
            dismissAllowingStateLoss();
        });

        mRootView.findViewById(R.id.iv_close).setOnClickListener(v -> {
            dismissAllowingStateLoss();
        });
    }

    public static ChooseChainFragment getInstance(List<BHChain> list, String chain,ChooseChainListener listener){
        ChooseChainFragment fragment = new ChooseChainFragment();
        fragment.mChainList = list;
        fragment.mChain = chain;
        fragment.mListener = listener;
        return fragment;
    }


    private class ChainAdapter extends BaseQuickAdapter<BHChain, BaseViewHolder>{
        public ChainAdapter( @org.jetbrains.annotations.Nullable List<BHChain> data) {
            super(R.layout.item_choose_chain, data);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, BHChain bhChain) {
            //holder.setText(R.id.tv_chain_name,bhChain.chain.toUpperCase());
            holder.setText(R.id.tv_chain_full,bhChain.full_name);
            AppCompatCheckBox  ck_status = holder.getView(R.id.ck_status);

            AppCompatImageView iv_chain_icon = holder.getView(R.id.iv_chain_icon);
            //BHToken bhToken = SymbolCache.getInstance().getBHToken(bhChain.chain);
            ImageLoaderUtil.loadImageView(getContext(),bhChain.logo,iv_chain_icon,R.mipmap.ic_default_coin);

            if(mChain.equals(bhChain.chain)){
                ck_status.setChecked(true);
                ck_status.setVisibility(View.VISIBLE);
            }else{
                ck_status.setChecked(false);
                ck_status.setVisibility(View.INVISIBLE);
            }
        }
    }

    public interface  ChooseChainListener{
        public void chooseChainAction(String chooseChain);
    }

}