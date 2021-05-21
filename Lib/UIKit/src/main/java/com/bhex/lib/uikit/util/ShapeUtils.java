package com.bhex.lib.uikit.util;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

/**
 * @author gongdongyang
 * 2020年9月1日16:51:06
 * 动态设置圆角
 */
public class ShapeUtils {

    public static GradientDrawable getRoundRectStrokeDrawable(int radius, int fillColor, int storkeColor,int strokeWidth) {
        float[] radius_f = {radius, radius, radius, radius, radius, radius, radius, radius};
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadii(radius_f);
        drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(fillColor);
        drawable.setStroke(strokeWidth,storkeColor);
        return drawable;
    }
    public static GradientDrawable getRoundRectDrawable(int radius, int color) {
        float[] radius_f = {radius, radius, radius, radius, radius, radius, radius, radius};
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadii(radius_f);
        drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(color );
        return drawable;
    }

    public static GradientDrawable getRoundRectBottomDrawable(int radius, int color) {
        float[] radius_f = {0, 0, 0, 0, 0, 0, radius, radius};
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadii(radius_f);
        drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(color );
        //drawable.setStroke(isFill ? 0 : strokeWidth, color);
        return drawable;
    }

    public static GradientDrawable getRoundRectDrawable(int radius, int startColor,int endColor) {
        float[] radius_f = {radius, radius, radius, radius, radius, radius, radius, radius};
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{startColor,endColor});
        drawable.setCornerRadii(radius_f);
        drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        drawable.setShape(GradientDrawable.RECTANGLE);
        //drawable.setCo
        //drawable.setStroke(isFill ? 0 : strokeWidth, color);
        return drawable;
    }

    public static GradientDrawable getRingDrawable(int radius, int storkeColor, int size, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(radius);
        drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setSize(size,size);
        drawable.setColor(Color.TRANSPARENT);
        drawable.setStroke( strokeWidth, storkeColor);
        return drawable;
    }

    public static GradientDrawable getRoundRectTopDrawable(int radius, int color, boolean isFill, int strokeWidth) {
        float[] radius_f = {radius, radius, radius, radius, 0, 0, 0, 0};
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadii(radius_f);
        drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(isFill ? color : Color.TRANSPARENT);
        drawable.setStroke(isFill ? 0 : strokeWidth, color);
        return drawable;
    }


    public static GradientDrawable getOvalDrawable(int radius, int color, boolean isFill, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(radius);
        drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(isFill ? color : Color.TRANSPARENT);
        drawable.setStroke(isFill ? 0 : strokeWidth, color);
        return drawable;
    }
}
