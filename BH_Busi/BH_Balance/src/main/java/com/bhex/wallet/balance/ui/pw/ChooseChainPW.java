package com.bhex.wallet.balance.ui.pw;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.ImageLoaderUtil;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.tools.utils.ResUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.common.model.BHChain;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author gdy
 * 2021-5-28 13:18:21
 */
public class ChooseChainPW extends PopupWindow {

    public ViewGroup m_root_view;

    public Context m_context;
    public ChooseChainListener m_listener;
    private ChainAdapter chain_adapter;
    private RecyclerView rec_chain_list;
    private String mChain;
    private List<BHChain> mChainList;

    public ChooseChainPW(Context context,List<BHChain> list,String chain,ChooseChainListener listener) {
        super(context);
        m_context = context;
        mChainList = list;
        mChain = chain;
        m_listener = listener;
        init(context);
    }


    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_root_view = (LinearLayout)inflater.inflate(R.layout.pw_choose_chain, null);

        this.setContentView(m_root_view);
        int width = PixelUtils.getScreenWidth(context)-PixelUtils.dp2px(m_context,32);
        this.setWidth(width);
        this.setOutsideTouchable(true);
        this.setFocusable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setElevation(16);
        }

        this.setBackgroundDrawable(ResUtils.getDrawable(m_context, R.drawable.drop_down_bg_radius));
        //setAnimationStyle(R.style.pop_window_anim_style);

        rec_chain_list = m_root_view.findViewById(R.id.rec_chain_list);
        chain_adapter = new ChainAdapter(mChainList);
        rec_chain_list.setAdapter(chain_adapter);

        chain_adapter.setOnItemClickListener((adapter, view1, position) -> {
            BHChain bhChain = chain_adapter.getData().get(position);
            if(m_listener!=null){
                m_listener.chooseChainAction(bhChain.chain);
            }
            dismiss();
        });

    }


    private class ChainAdapter extends BaseQuickAdapter<BHChain, BaseViewHolder> {

        public ChainAdapter( @org.jetbrains.annotations.Nullable List<BHChain> data) {
            super(R.layout.item_choose_chain, data);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, BHChain bhChain) {
            holder.setText(R.id.tv_chain_full,bhChain.full_name);
            AppCompatCheckBox ck_status = holder.getView(R.id.ck_status);
            ck_status.setVisibility(View.INVISIBLE);
            AppCompatImageView iv_chain_icon = holder.getView(R.id.iv_chain_icon);
            ImageLoaderUtil.loadImageView(getContext(),bhChain.logo,iv_chain_icon,R.mipmap.ic_default_coin);
            holder.itemView.setBackgroundColor(ColorUtil.getColor(getContext(),R.color.white));
            if(bhChain.chain.equals(mChain)){
                holder.itemView.setBackgroundColor(ColorUtil.getColor(getContext(),R.color.item_select_bg_color));
            }
        }
    }

    public  interface ChooseChainListener{
        void chooseChainAction(String chooseChain);
    }
}
