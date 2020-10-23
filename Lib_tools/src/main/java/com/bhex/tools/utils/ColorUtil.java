package com.bhex.tools.utils;

import android.content.Context;
import android.graphics.ColorMatrixColorFilter;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;


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

    public static int getColor(Context context, int colorId){
        return ContextCompat.getColor(context, colorId);
    }

    public static void setIconColor(AppCompatImageView icon, int r, int g, int b, int a) {
        float []colorMatrix = new float[]{
                0, 0, 0, 0, r,
                0, 0, 0, 0, g,
                0, 0, 0, 0, b,
                0, 0, 0, (float) a / 255, 0
        };
        icon.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
    }
}
