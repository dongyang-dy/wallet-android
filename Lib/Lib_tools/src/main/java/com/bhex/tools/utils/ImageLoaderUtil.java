package com.bhex.tools.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;

import com.bhex.tools.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/4/26
 * Time: 10:21
 */
public class ImageLoaderUtil {


    public interface ImageLoaderListener{
        public void onImageLoaderListener(Bitmap resource);
    }

    //默认加载
    public static void loadImageView(Context mContext, String path, ImageView mImageView, @DrawableRes int resId) {
        if(TextUtils.isEmpty(path)){
           mImageView.setImageResource(resId);
           return;
        }
        Glide.with(mContext).load(path)
                .placeholder(resId)
                .error(resId)
                .into(mImageView);
    }

    //设置加载圆形图支持
    public static void loadImageViewToCircle(Context mContext, String path, ImageView mImageView,@DrawableRes int resId){
        if(TextUtils.isEmpty(path)){
            mImageView.setImageResource(resId);
            return;
        }
        RequestOptions requestOptions = RequestOptions.circleCropTransform();
        Glide.with(mContext).load(path)
                .apply(requestOptions)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                //.placeholder(R.drawable.bg_hot_spot_news)
                //.error(R.drawable.bg_hot_spot_news)
                .into(mImageView);
    }
}
