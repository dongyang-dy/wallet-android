package com.bhex.network.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.RequestBody;

/**
 * Created by gongdongyang on 2018/7/19.
 */

public class HUtils {

    public static <T> T checkNotNull(T t, String message) {
        if (t == null) {
            throw new NullPointerException(message);
        }
        return t;
    }

    public static boolean checkMain() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }

    public static RequestBody createJson(String jsonString) {
        checkNotNull(jsonString, "json not null!");
        return RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), jsonString);
    }

    /**
     * @param name
     * @return
     */
    public static RequestBody createFile(String name) {
        checkNotNull(name, "name not null!");
        return RequestBody.create(okhttp3.MediaType.parse("multipart/form-data; charset=utf-8"), name);
    }

    /**
     * @param file
     * @return
     */
    public static RequestBody createFile(File file) {
        checkNotNull(file, "file not null!");
        return RequestBody.create(okhttp3.MediaType.parse("multipart/form-data; charset=utf-8"), file);
    }

    /**
     * @param file
     * @return
     */
    public static RequestBody createImage(File file) {
        checkNotNull(file, "file not null!");
        return RequestBody.create(okhttp3.MediaType.parse("image/jpg; charset=utf-8"), file);
    }

    public static void close(Closeable close) {
        if (close != null) {
            try {
                closeThrowException(close);
            } catch (IOException ignored) {
            }
        }
    }

    public static void closeThrowException(Closeable close) throws IOException {
        if (close != null) {
            close.close();
        }
    }

    /**
     * find the type by interfaces
     *
     * @param cls
     * @param <R>
     * @return
     */
    public static <R> Type findNeedType(Class<R> cls) {
        List<Type> typeList = HUtils.getMethodTypes(cls);
        if (typeList == null || typeList.isEmpty()) {
            return RequestBody.class;
        }
        return typeList.get(0);
    }

    /**
     * MethodHandler
     */
    public static <T> List<Type> getMethodTypes(Class<T> cls) {
        Type typeOri = cls.getGenericSuperclass();
        List<Type> needtypes = null;
        // if Type is T
        if (typeOri instanceof ParameterizedType) {
            needtypes = new ArrayList<>();
            Type[] parentypes = ((ParameterizedType) typeOri).getActualTypeArguments();
            for (Type childtype : parentypes) {
                needtypes.add(childtype);
                if (childtype instanceof ParameterizedType) {
                    Type[] childtypes = ((ParameterizedType) childtype).getActualTypeArguments();
                    Collections.addAll(needtypes, childtypes);
                }
            }
        }
        return needtypes;
    }

    public static Class getClass(Type type, int i) {
        if (type instanceof ParameterizedType) { // ??????????????????
            return getGenericClass((ParameterizedType) type, i);
        } else if (type instanceof TypeVariable) {
            return getClass(((TypeVariable) type).getBounds()[0], 0); // ????????????????????????
        } else {// class????????????type???????????????
            return (Class) type;
        }
    }

    public static Type getType(Type type, int i) {
        if (type instanceof ParameterizedType) { // ??????????????????
            return getGenericType((ParameterizedType) type, i);
        } else if (type instanceof TypeVariable) {
            return getType(((TypeVariable) type).getBounds()[0], 0); // ????????????????????????
        } else {// class????????????type???????????????
            return type;
        }
    }

    public static Type getParameterizedType(Type type, int i) {
        if (type instanceof ParameterizedType) { // ??????????????????
            Type genericType = ((ParameterizedType) type).getActualTypeArguments()[i];
            return genericType;
        } else if (type instanceof TypeVariable) {
            return getType(((TypeVariable) type).getBounds()[0], 0); // ????????????????????????
        } else {// class????????????type???????????????
            return type;
        }
    }

    public static Class getGenericClass(ParameterizedType parameterizedType, int i) {
        Type genericClass = parameterizedType.getActualTypeArguments()[i];
        if (genericClass instanceof ParameterizedType) { // ??????????????????
            return (Class) ((ParameterizedType) genericClass).getRawType();
        } else if (genericClass instanceof GenericArrayType) { // ??????????????????
            return (Class) ((GenericArrayType) genericClass).getGenericComponentType();
        } else if (genericClass instanceof TypeVariable) { // ????????????????????????
            return getClass(((TypeVariable) genericClass).getBounds()[0], 0);
        } else {
            return (Class) genericClass;
        }
    }

    public static Type getGenericType(ParameterizedType parameterizedType, int i) {
        Type genericType = parameterizedType.getActualTypeArguments()[i];
        if (genericType instanceof ParameterizedType) { // ??????????????????
            return ((ParameterizedType) genericType).getRawType();
        } else if (genericType instanceof GenericArrayType) { // ??????????????????
            return ((GenericArrayType) genericType).getGenericComponentType();
        } else if (genericType instanceof TypeVariable) { // ????????????????????????
            return getClass(((TypeVariable) genericType).getBounds()[0], 0);
        } else {
            return genericType;
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);
        if (null == manager)
            return false;
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (null == info || !info.isAvailable())
            return false;
        return true;
    }


    /**
     * ?????????????????????????????????????????????????????????????????????
     *
     * @param <T>
     * @return
     */
    public static <T> Type findNeedClass(Class<T> cls) {
        //?????????????????????????????????????????????,???????????????
        Type genType = cls.getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        Type type = params[0];
        Type finalNeedType;
        if (params.length > 1) {//??????????????????CacheResult<SkinTestResult> 2???
            if (!(type instanceof ParameterizedType)) throw new IllegalStateException("????????????????????????");
            finalNeedType = ((ParameterizedType) type).getActualTypeArguments()[0];
            //Type rawType = ((ParameterizedType) type).getRawType();
        } else {//???????????????:SkinTestResult  1???
            finalNeedType = type;
        }
        return finalNeedType;
    }

    /**
     * ????????????????????????????????????????????????????????????
     */
    public static <T> Type findRawType(Class<T> cls) {
        Type genType = cls.getGenericSuperclass();
        return getGenericType((ParameterizedType) genType, 0);
    }

}
