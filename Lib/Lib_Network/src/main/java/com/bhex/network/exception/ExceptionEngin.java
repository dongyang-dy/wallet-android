package com.bhex.network.exception;

import android.util.Log;


import com.bhex.network.R;
import com.bhex.network.app.BaseApplication;

import org.json.JSONObject;
import org.web3j.crypto.CipherException;

import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class ExceptionEngin {

    public static final int OK = 200;

    /*public static final int BAD_GATEWAY = 502;

    public static final int ERROR_NO_PREMESSION = 1005;

    public static final int FORBIDDEN = 403;

    public static final int GATEWAY_TIMEOUT = 504;

    public static final int HTTP_ERROR = 1003;

    public static final int INTERNAL_SERVER_ERROR = 500;

    public static final int NOT_FOUND = 404;

    public static final int PARSE_ERROR = 1001;

    public static final int REQUEST_TIMEOUT = 408;

    public static final int SERVER_ERROR = 512;

    public static final int SERVICE_UNAVAILABLE = 503;

    public static final int SSL_ERROR = 1004;

    public static final int UNAUTHORIZED = 401;

    public static final int UNKNOWN = 1000;*/

    public static ApiException handleException(Throwable throwable) throws Exception {
        if (throwable != null) {
            throwable.printStackTrace();
            Log.e("error:", throwable.toString());
        }

        if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException)throwable;
            ApiException apiException = new ApiException(throwable, httpException.code());
            int code = httpException.code();

            if (code == 500 || code == 502 || code == 504 || code == 512) {
                apiException.setDisplayMessage(BaseApplication.getInstance().getString(R.string.app_net_error_msg));
                return apiException;
            }

            if ( code == 408 || code == 404) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(code+":");
                stringBuilder.append(BaseApplication.getInstance().getString(R.string.app_net_error_msg));
                apiException.setDisplayMessage(stringBuilder.toString());
                return apiException;
            }

            ResponseBody response = httpException.response().errorBody();
            if (response != null) {
                JSONObject json = new JSONObject(response.string());
                String error = (String) json.get("error");
                apiException.setDisplayMessage(error);
            }else{
                apiException.setDisplayMessage(throwable.getMessage());
            }
            return apiException;

            //apiException.setDisplayMessage(BaseApplication.getInstance().getString(R.string.net_error_msg));
            //return apiException;
        }

        if (throwable instanceof ServerException) {
            ServerException serverException = (ServerException)throwable;
            ApiException apiException1 = new ApiException(serverException, serverException.getCode());
            apiException1.setDisplayMessage(serverException.getMsg());
            return apiException1;
        }

        if (throwable instanceof com.google.gson.JsonParseException || throwable instanceof org.json.JSONException
                || throwable instanceof android.net.ParseException || throwable instanceof  com.fasterxml.jackson.core.JsonParseException) {
            ApiException apiException = new ApiException(throwable, 1001);
            apiException.setDisplayMessage(BaseApplication.getInstance().getString(R.string.json_parse_error));
            return apiException;
        }
        if (throwable instanceof java.net.ConnectException) {
            ApiException apiException = new ApiException(throwable, 1003);
            apiException.setDisplayMessage(BaseApplication.getInstance().getString(R.string.error_network_later_try));
            return apiException;
        }
        if (throwable instanceof java.net.SocketTimeoutException) {
            ApiException apiException = new ApiException(throwable, 1003);
            apiException.setDisplayMessage(BaseApplication.getInstance().getString(R.string.error_network_time_out));
            return apiException;
        }
        if (throwable instanceof java.net.UnknownHostException) {
            ApiException apiException =  new ApiException(throwable, 1003);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(BaseApplication.getInstance().getString(R.string.error_network_time_out));
            stringBuilder.append("_UnknownHost");
            apiException.setDisplayMessage(stringBuilder.toString());
            return apiException;
        }
        if (throwable instanceof SecurityException)
            return new ApiException(throwable, 1005);
        if (throwable instanceof javax.net.ssl.SSLException) {
            ApiException apiException = new ApiException(throwable, 1004);
            StringBuilder sb = new StringBuilder();
            sb.append(BaseApplication.getInstance().getString(R.string.error_network_later_try));
            sb.append("_SSL");
            apiException.setDisplayMessage(sb.toString());
            return apiException;
        }

        if(throwable instanceof CipherException){
            ApiException apiException = new ApiException(throwable, 1005);
            StringBuilder sb = new StringBuilder();
            sb.append(BaseApplication.getInstance().getString(R.string.error_oldpassword));
            apiException.setDisplayMessage(sb.toString());
            return apiException;
        }
        if (throwable instanceof java.io.IOException) {
            ApiException apiException = new ApiException(throwable, 1004);
            apiException.setDisplayMessage(BaseApplication.getInstance().getString(R.string.error_process));
            return apiException;
        }

        if(throwable instanceof io.reactivex.exceptions.OnErrorNotImplementedException){
            ApiException apiException = new ApiException(throwable, 1006);
            apiException.setDisplayMessage(BaseApplication.getInstance().getString(R.string.traffic_network_later_try));
            //apiException.setDisplayMessage(BaseApplication.getInstance().getString(R.string.error_process));
            return apiException;
        }

        ApiException apiException = new ApiException(throwable, 1000);
        apiException.setDisplayMessage(apiException.getMessage());
        return apiException;
    }

}
