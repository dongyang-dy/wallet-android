package com.bhex.wallet.bh_main.my.ui.decoration;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bhex.tools.utils.PixelUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.bh_main.R;

import java8.util.stream.IntStreams;

/**
 * Created by gdy on 2018/11/22
 * Describe:
 */


public class MyRecyclerViewDivider extends RecyclerView.ItemDecoration {
    private Paint mPaint;
    private Context mContext;
    private int mDividerColor;
    private float mDividerHeight;
    private float mDividerLineHeight;
    private int [] mDividerPosition;

    public MyRecyclerViewDivider(Context context,int dividerColor,float dividerHeight, int... params) {
        mContext = context;
        mDividerColor = dividerColor;
        mDividerHeight = dividerHeight;
        mDividerLineHeight = context.getResources().getDimension(R.dimen.item_line_divider_height);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(dividerColor);
        mPaint.setStyle(Paint.Style.FILL);
        mDividerPosition = params;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildLayoutPosition(view);
        IntStreams.range(0,mDividerPosition.length).forEach(t->{
            if(mDividerPosition !=null && mDividerPosition[t]==position){
                outRect.bottom = (int)mDividerHeight;
            }else{
                outRect.bottom = (int)mDividerLineHeight;
            }

        });
    }
    int i = 0;
    @Override
    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(canvas, parent, state);
        final int childCount = parent.getChildCount();
        //int left = parent.getPaddingLeft();
        final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        mPaint.setColor(mDividerColor);
        //i = 0;
        for (i = 0; i < childCount; i++) {
            View v = parent.getChildAt(i);
            IntStreams.range(0,mDividerPosition.length).forEach(t->{
                if(mDividerPosition !=null &&  mDividerPosition[t]==i){
                    int left = parent.getPaddingLeft();
                    canvas.drawRect(left, v.getBottom(), right,
                            v.getBottom()+mDividerHeight, mPaint);
                }else{
                    int left = (int)mContext.getResources().getDimension(R.dimen.main_padding_left);
                    canvas.drawRect(left, v.getBottom(), right,
                            v.getBottom()+mDividerLineHeight, mPaint);
                }
            });

        }
    }


    /*public void setDividerColor(int mDividerColor) {
        this.mDividerColor = mDividerColor;
    }*/

}
