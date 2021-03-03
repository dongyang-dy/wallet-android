package com.bhex.wallet.bh_main.my.ui.decoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bhex.wallet.bh_main.R;


/**
 * @author gongdongyang
 *
 */
public class FunctionItemDecoration  extends RecyclerView.ItemDecoration  {

    private Paint mPaint;
    private Context mContext;
    private int mDividerColor;
    private float mDividerHeight;

    public FunctionItemDecoration(Context context,int dividerColor,float dividerHeight) {
        mContext = context;
        mDividerColor = dividerColor;
        mDividerHeight = dividerHeight;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(dividerColor);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        //int position = parent.getChildLayoutPosition(view);
        outRect.bottom = (int)mDividerHeight;
    }

    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(canvas, parent, state);
        final int childCount = parent.getChildCount();
        //int left = parent.getPaddingLeft();
        float left = mContext.getResources().getDimension(R.dimen.main_padding_left);
        final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        mPaint.setColor(mDividerColor);
        //i = 0;
        for (int i = 0; i < childCount; i++) {
            if(i==childCount-1){
                break;
            }
            View v = parent.getChildAt(i);
            canvas.drawRect(left, v.getBottom(), right,
                    v.getBottom()+mDividerHeight, mPaint);
        }

    }
}
