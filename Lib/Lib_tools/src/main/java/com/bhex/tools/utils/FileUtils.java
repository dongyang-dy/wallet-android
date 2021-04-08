package com.bhex.tools.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/23
 * Time: 23:40
 */
public class FileUtils {

    /**
     *
     * @param fileName
     * @param context
     * @return
     */
    public static String loadStringByAssets(Context context,String fileName) {

        StringBuilder sb = new StringBuilder();

        try{
            AssetManager am = context.getAssets();

            BufferedReader bf = new BufferedReader(new InputStreamReader(am.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                sb.append(line).append(" ");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String loadStringByAssets$$(Context context,String fileName) {

        StringBuilder sb = new StringBuilder();

        try{
            AssetManager am = context.getAssets();

            BufferedReader bf = new BufferedReader(new InputStreamReader(am.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                sb.append(line).append(";");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return sb.toString();
    }

    //图片保存
    public static boolean saveImageToGallery(Context context, Bitmap bitmap, String fileName){
        //存储路径
        String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "qrcode";
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        try{
            File file = new File(appDir, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片(80代表压缩20%)
            boolean isSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();

            //发送广播通知系统图库刷新数据
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            if (isSuccess) {
                return true;
            } else {
                return false;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据文件路径获取文件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    @Nullable
    public static File getFileByPath(final String filePath) {
        return isSpace(filePath) ? null : new File(filePath);
    }

    private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
