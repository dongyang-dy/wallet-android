package com.bhex.wallet.balance.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.lib.uikit.widget.CircleView;
import com.bhex.lib.uikit.widget.text.marqueen.MarqueeFactory;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.enums.BUSI_ANNOUNCE_TYPE;
import com.bhex.wallet.balance.model.AnnouncementItem;

/**
 * @author gongdongyang
 *
 */
public class AnnouncementMF  extends MarqueeFactory<RelativeLayout, AnnouncementItem> {
    private LayoutInflater inflater;
    private OnItemListener mOnItemListener;

    public AnnouncementMF(Context context, OnItemListener listener) {
        super(context);
        inflater = LayoutInflater.from(context);
        this.mOnItemListener = listener;
    }

    @Override
    public RelativeLayout generateMarqueeItemView(AnnouncementItem item) {
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.layout_index_announcement, null);
        /*CircleView circleView = view.findViewById(R.id.iv_announce_bg);
        AppCompatImageView iv_announce_type = view.findViewById(R.id.iv_announce_type);
        //增加背景
        GradientDrawable drawable = null;
        if(item.type== BUSI_ANNOUNCE_TYPE.公告.index){
            drawable = ShapeUtils.getRoundRectDrawable(60,
                    ColorUtil.getColor(mContext,R.color.announcement_bg_color));
            circleView.setCircleColor( ColorUtil.getColor(mContext,R.color.announcement_type_color));
            Drawable srcDrawable = ColorUtil.getDrawable(mContext,R.mipmap.ic_announcement,R.color.announcement_icon_color);
            iv_announce_type.setImageDrawable(srcDrawable);

        }else if(item.type== BUSI_ANNOUNCE_TYPE.警告.index){
            drawable = ShapeUtils.getRoundRectDrawable(60,
                    ColorUtil.getColor(mContext,R.color.alarm_bg_color));
            circleView.setCircleColor( ColorUtil.getColor(mContext,R.color.alarm_type_color));
            Drawable srcDrawable = ColorUtil.getDrawable(mContext,R.mipmap.ic_announcement,R.color.alarm_icon_color);
            iv_announce_type.setImageDrawable(srcDrawable);
        }else if(item.type== BUSI_ANNOUNCE_TYPE.活动.index){
            drawable = ShapeUtils.getRoundRectDrawable(60,
                    ColorUtil.getColor(mContext,R.color.activity_bg_color));
            circleView.setCircleColor( ColorUtil.getColor(mContext,R.color.activity_type_color));

            Drawable srcDrawable = ColorUtil.getDrawable(mContext,R.mipmap.ic_announcement,R.color.activity_icon_color);
            iv_announce_type.setImageDrawable(srcDrawable);
        }else if(item.type== BUSI_ANNOUNCE_TYPE.提示.index) {
            drawable = ShapeUtils.getRoundRectDrawable(60,
                    ColorUtil.getColor(mContext,R.color.tips_bg_color));
            circleView.setCircleColor( ColorUtil.getColor(mContext,R.color.tips_type_color));

            Drawable srcDrawable = ColorUtil.getDrawable(mContext,R.mipmap.ic_announcement,R.color.tips_icon_color);
            iv_announce_type.setImageDrawable(srcDrawable);
        }else{
            drawable = ShapeUtils.getRoundRectDrawable(60,
                    ColorUtil.getColor(mContext,R.color.announcement_bg_color));
            circleView.setCircleColor( ColorUtil.getColor(mContext,R.color.announcement_type_color));
            Drawable srcDrawable = ColorUtil.getDrawable(mContext,R.mipmap.ic_announcement,R.color.announcement_icon_color);
            iv_announce_type.setImageDrawable(srcDrawable);
        }
        view.setBackground(drawable);*/
        AppCompatTextView tv_announce_title = view.findViewById(R.id.tv_announce_title);
        tv_announce_title.setText(item.text);

        //
        /*view.findViewById(R.id.iv_close).setOnClickListener(v -> {
            if(mOnItemListener==null){
                return;
            }

            mOnItemListener.closeAction();
        });*/
        return view;
    }


    public interface  OnItemListener{
        public void closeAction();
    }
}
