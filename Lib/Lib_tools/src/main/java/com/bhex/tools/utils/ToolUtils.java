package com.bhex.tools.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.bhex.tools.language.LocalManageUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

/**
 * 语言判断
 */
public class ToolUtils {
    // 两次点击按钮之间的点击间隔不能少于1000毫秒
    private static final int MIN_CLICK_DELAY_TIME = 1500;
    private static long lastClickTime;

    /**
     * @param context
     * @return
     */
    public static boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }

    public static String getLocalString(Context context){

        Locale locale = LocalManageUtil.getSetLanguageLocale(context);
        if(locale!=null){
            if(locale.getLanguage().contains("en")){
                return "en-us";
            }else{
                return "zh-cn";
            }
        }else{
            return "en-us";
        }
    }

    //关闭软键盘
    public static void hintKeyBoard(Activity context) {
        //拿到InputMethodManager
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        //如果window上view获取焦点 && view不为空
        if (imm.isActive() && context.getCurrentFocus() != null) {
            //拿到view的token 不为空
            if (context.getCurrentFocus().getWindowToken() != null) {
                //表示软键盘窗口总是隐藏，除非开始时以SHOW_FORCED显示。
                imm.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    //关闭软键盘
    public static void hintKeyBoard(Activity context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    /**
     * 防快速点击
     *
     * @return
     */
    public static boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }

    public static String dateToStamp(String s) throws ParseException {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }

    /**
     * 粘贴文本
     */
    public static void clipText(EditText editText, Context context) {
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                if (!TextUtils.isEmpty(item.getText().toString())) {
                    String pasteData = item.getText() + "";//获取纯文本数据
                    editText.setText(pasteData);
                    editText.setSelection(pasteData.length());
                }
            }
        } catch (Exception e) {

        }
    }

    //隐藏系统键盘
    public static void hideSystemSofeKeyboard(Activity mContext, EditText editText) {
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= 11) {
            try {
                Class<EditText> cls = EditText.class;
                Method setShowSoftInputOnFocus;
                setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(editText, false);

            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            editText.setInputType(InputType.TYPE_NULL);
        }
        // 如果软键盘已经显示，则隐藏
        InputMethodManager imm = (InputMethodManager) mContext.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    //显示键盘
    public static void showKeyBoard(FragmentActivity activity,EditText editText) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(activity.getWindow().getDecorView(), InputMethodManager.SHOW_FORCED);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        //imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 复制文本内容
     *
     * @param copy
     * @param context
     */
    public static void copyText(String copy, Context context) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // 将文本内容放到系统剪贴板里。
        //cm.setText(((TextView)mLayoutItem1.findViewById(R.id.tv_item_hint)).getText());
        cm.setPrimaryClip(ClipData.newPlainText(null, copy));
        if (cm.hasPrimaryClip()) {
            cm.getPrimaryClip().getItemAt(0).getText();
        }
        //cm.setText(textView.getText().toString().trim());
    }

    public static void clearClipboard(Context context){
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            try {
                manager.setPrimaryClip(manager.getPrimaryClip());
                manager.setText(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    //生成随机数
    public static int getRandom(int count) {
        return (int) Math.round(Math.random() * (count));
    }

    //生成随机数
    /*public static String getRandomString(int length) {
        StringBuffer sb = new StringBuffer();
        int len = POCConstants.RANDOMSTR.length();
        for (int i = 0; i < length; i++) {
            sb.append(POCConstants.RANDOMSTR.charAt(getRandom(len - 1)));
        }
        return sb.toString();
    }*/

    public static boolean checkListIsEmpty(List list) {
        if (list == null || list.size() == 0) {
            return true;
        }
        return false;
    }

    public static boolean checkMapEmpty(Map map) {
        if (map == null || map.size() == 0) {
            return true;
        }
        return false;
    }


    public static boolean codeStatusError(int code) {
        if (code < 530) {
            return true;
        }
        return false;
    }

    /**
     * 验证content
     *
     * @return
     */
    public static boolean isVerifyPass(String originContent, String verifyContent) {
        boolean flag = false;
        if (MD5.verify(originContent, verifyContent)) {
            flag = true;
        }
        return flag;
    }

    //检测手机是否root
    public static boolean isSuEnable() {
        File file = null;
        String[] paths = {"/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/", "/su/bin/"};
        try {
            for (String path : paths) {
                file = new File(path + "su");
                if (file.exists() && file.canExecute()) {
                    Log.i("TAG", "find su in : " + path);
                    return true;
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return false;
    }





}
