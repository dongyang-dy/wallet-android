package com.bhex.wallet.balance.ui.viewhodler;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.bhex.tools.utils.PixelUtils;
import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.tools.language.LocalManageUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.balance.R;


/**
 * @author gongdongyang
 * 2020-10-21 23:03:14
 */
public class TipsViewHolder {

    public Context mContext;

    public View mView;

    public AppCompatTextView tv_tip_content;
    public LinearLayout layout_action;
    public View v_line;
    public AppCompatImageView iv_arrow;
    public void initView(Context context,View view){
        mView = view;
        mContext = context;
        tv_tip_content = view.findViewById(R.id.tv_tip_content);
        layout_action = view.findViewById(R.id.layout_action);
        v_line = view.findViewById(R.id.v_line);
        iv_arrow = view.findViewById(R.id.iv_arrow);
        tv_tip_content.setSingleLine(false);


        tv_tip_content.post(()->{
            /*int count = tv_tip_content.getLineCount();
            LogUtils.d("TipsViewHolder==>:","count=="+count);
            if(count>1){
                tv_tip_content.setSingleLine(true);
                tv_tip_content.setEllipsize(TextUtils.TruncateAt.END);
                layout_action.setVisibility(View.VISIBLE);
                layout_action.setTag(true);
            }else{
                //点击查看更多
                layout_action.setTag(false);
                layout_action.setVisibility(View.GONE);
            }*/

            int index = LocalManageUtil.getSetLanguageLocaleIndex(context);

            if(index==2){
                tv_tip_content.setSingleLine(true);
                tv_tip_content.setEllipsize(TextUtils.TruncateAt.END);
                layout_action.setTag(true);
                layout_action.setVisibility(View.VISIBLE);
            }else {
                layout_action.setVisibility(View.GONE);
            }
        });

        Drawable drawable = ShapeUtils.getRoundRectDrawable(PixelUtils.dp2px(mContext,10), Color.parseColor("#16ED3756"));
        mView.setBackground(drawable);

        layout_action.setOnClickListener(v -> {
            actionMore();
        });
    }

    private void xx(){

    }

    private void actionMore(){
        boolean flag = (Boolean) layout_action.getTag();
        if(flag){
            iv_arrow.animate().setDuration(500).rotation(180).start();
        }else{
            iv_arrow.animate().setDuration(500).rotation(0).start();
        }
        tv_tip_content.setSingleLine(!flag);
        layout_action.setTag(!flag);

        mView.postDelayed(()->{
            int height = tv_tip_content.getMeasuredHeight();
            v_line.getLayoutParams().height = height-PixelUtils.dp2px(mContext,4);
            v_line.requestLayout();
        },50);





    }
}
