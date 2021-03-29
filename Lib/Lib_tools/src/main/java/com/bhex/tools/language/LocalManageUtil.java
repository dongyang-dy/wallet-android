package com.bhex.tools.language;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import com.bhex.tools.utils.LogUtils;

import java.util.Locale;

/**
 * created by gongdongyang
 * on 2020/2/25
 */
public class LocalManageUtil {

    private final static String TAG = LocalManageUtil.class.getSimpleName();

    /**
     * 赋予Context对象新的属性
     * @param context
     * @param language
     * @return
     */
    public static Context attachBaseContext(Context context, String language) {
        Locale locale = getSetLanguageLocale(context);
        return  createConfigurationResources(context,locale.getLanguage());
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context createConfigurationResources(Context context, String language) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();

        Locale locale = new Locale(language);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);
        } else {
            configuration.locale = locale;
        }

        resources.updateConfiguration(configuration, dm);
        return context;
    }

    /*public static void applyLanguage(Context context, String newLanguage) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = new Locale(newLanguage);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // apply locale
            configuration.setLocale(locale);
        } else {
            // updateConfiguration
            configuration.locale = locale;
            DisplayMetrics dm = resources.getDisplayMetrics();
            resources.updateConfiguration(configuration, dm);
        }
    }*/

    /**
     * 获取选择的语言
     * @param context
     * @return
     */
    public static Locale getSetLanguageLocale(Context context) {
        int selectIndex = LocalManageUtil.getSetLanguageLocaleIndex(context);
        switch (selectIndex) {
            case 1:
                return Locale.SIMPLIFIED_CHINESE;
            case 2:
                return Locale.US;
            case 3:
                return Locale.KOREA;
            default:
                return Locale.US;
        }

    }

    public static int getSetLanguageLocaleIndex(Context context) {
        int selectIndex = LocalSPUtil.getInstance(context).getSelectLanguage();
        if(selectIndex==-1){
           Locale locale =  getSystemLocale(context);
           if(locale.getLanguage().contains("zh")){
               selectIndex = 1;
           }else if(locale.getLanguage().contains("en")){
               selectIndex = 2;
           }
        }
        return selectIndex;
    }



    /**
     * 获取系统语言
     * @param context
     * @return
     */
    public static Locale getSystemLocale(Context context) {
        return LocalSPUtil.getInstance(context).getSystemCurrentLocal(context);
    }

    /**
     * 保存语言设置
     * @param context
     * @param select
     */
    public static void saveSelectLanguage(Context context, int select) {
        LocalSPUtil.getInstance(context).saveLanguage(select);
    }
}
