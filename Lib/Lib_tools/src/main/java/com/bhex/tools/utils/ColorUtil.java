package com.bhex.tools.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import androidx.annotation.IdRes;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.bhex.tools.R;


/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/10
 * Time: 21:15
 */
public class ColorUtil {

    /*public static int getDark(Context context) {
        return SkinColorUtil.getColor(context, CommonUtil.isBlackMode() ? R.color.dark_night : R.color.dark);
    }*/

    public static int getColor(Context context, int colorId) {
        return ContextCompat.getColor(context, colorId);
    }

    public static void setIconColor(AppCompatImageView icon, int r, int g, int b, int a) {
        float[] colorMatrix = new float[]{
                0, 0, 0, 0, r,
                0, 0, 0, 0, g,
                0, 0, 0, 0, b,
                0, 0, 0, (float) a / 255, 0
        };
        icon.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
    }

    //drawable
    public static Drawable getDrawable(Context context, int resId, int resColorId) {
        int dark = ContextCompat.getColor(context, resColorId);
        Drawable mutateDrawable = context.getResources().getDrawable(resId).mutate();
        mutateDrawable.setColorFilter(dark, PorterDuff.Mode.SRC_ATOP);
        return mutateDrawable;
    }

    @SuppressLint("ResourceType")
    private static ColorStateList createColorStateList(Context context, @IdRes int disable_color_id, @IdRes int enable_color_id) {
         int[] colors = new int[]{ContextCompat.getColor(context, disable_color_id), ContextCompat.getColor(context, enable_color_id)};
        int[][] states = new int[2][];
    }
}
