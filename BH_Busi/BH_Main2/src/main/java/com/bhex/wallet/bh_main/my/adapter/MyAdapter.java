package com.bhex.wallet.bh_main.my.adapter;

import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SwitchCompat;

import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.my.ui.item.MyItem;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/12
 * Time: 11:19
 */
public class MyAdapter extends BaseQuickAdapter<MyItem, BaseViewHolder> {

    private int unread;

    public MyAdapter(@Nullable List<MyItem> data) {
        super(R.layout.item_my_ext, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder viewHolder, @Nullable MyItem myItem) {
        viewHolder.setText(R.id.tv_title,myItem.title);
        viewHolder.setText(R.id.tv_right_txt,"");
        /*if(myItem.isArrow){
            viewHolder.getView(R.id.iv_arrow).setVisibility(View.VISIBLE);
        }else{
            viewHolder.getView(R.id.iv_arrow).setVisibility(View.INVISIBLE);
        }

        if(TextUtils.isEmpty(myItem.rightTxt)){
            viewHolder.getView(R.id.tv_right_txt).setVisibility(View.INVISIBLE);
        }else{
            viewHolder.getView(R.id.tv_right_txt).setVisibility(View.VISIBLE);
            viewHolder.setText(R.id.tv_right_txt,myItem.rightTxt);
        }*/
        View layout_right = viewHolder.getView(R.id.layout_right);

        if(this.unread>0){
            layout_right.setVisibility(View.VISIBLE);
            viewHolder.setText(R.id.tv_right_txt,"查看最新消息");
            //
            AppCompatImageView iv_remind = viewHolder.getView(R.id.iv_remind);
            GradientDrawable drawable =  ShapeUtils.getOvalDrawable(PixelUtils.dp2px(getContext(),8),
                    ColorUtil.getColor(getContext(),R.color.alarm_highlight_text_color),true,0);
            iv_remind.setImageDrawable(drawable);
        }else {
            layout_right.setVisibility(View.INVISIBLE);
        }
    }

    public void changeMessageCount(int unread){
        this.unread = unread;
        notifyItemChanged(3);
    }
}
