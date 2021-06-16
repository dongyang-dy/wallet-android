package com.bhex.network.base;

import android.util.Log;

import com.bhex.network.BuildConfig;
import com.bhex.network.R;
import com.bhex.network.cache.utils.LogUtils;
import com.bhex.network.environment.EnvironmentActivity;
import com.bhex.network.environment.IEnvironment;
import com.bhex.network.errorhandler.HttpErrorHandler;
import com.bhex.network.interceptor.CommonRequestInterceptor;
import com.bhex.network.utils.HttpsUtils;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.CertificatePinner;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * created by
 * 2020/2/24
 */
public abstract class NetworkApi implements IEnvironment {

    private static INetworkRequiredInfo iNetworkRequiredInfo;
    private static HashMap<String, Retrofit> retrofitHashMap = new HashMap<>();
    private String mBaseUrl;
    private OkHttpClient mOkHttpClient;
    private static boolean mIsFormal = true;

    public NetworkApi() {
        if (!mIsFormal) {
            mBaseUrl = getTest();
        }else{
            mBaseUrl = getFormal();
        }
    }

    public static void init(INetworkRequiredInfo networkRequiredInfo) {
        iNetworkRequiredInfo = networkRequiredInfo;
        mIsFormal = EnvironmentActivity.isOfficialEnvironment(networkRequiredInfo.getApplicationContext());
    }

    protected Retrofit getRetrofit(Class service) {
        if (retrofitHashMap.get(mBaseUrl + service.getName()) != null) {
            return retrofitHashMap.get(mBaseUrl + service.getName());
        }
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder();
        retrofitBuilder.baseUrl(mBaseUrl);
        retrofitBuilder.client(getOkHttpClient());
        retrofitBuilder.addConverterFactory(GsonConverterFactory.create());
        retrofitBuilder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        Retrofit retrofit = retrofitBuilder.build();
        retrofitHashMap.put(mBaseUrl + service.getName(), retrofit);
        return retrofit;
    }


    public OkHttpClient getOkHttpClient() {
        //HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);

        if (mOkHttpClient == null) {
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
            if (getInterceptor() != null) {
                okHttpClientBuilder.addInterceptor(getInterceptor());
            }
            //OKHttp请求参数设置
            okHttpClientBuilder.retryOnConnectionFailure(true);
            okHttpClientBuilder.connectTimeout(15, TimeUnit.SECONDS);//10秒连接超时
            okHttpClientBuilder.writeTimeout(15, TimeUnit.SECONDS);//10m秒写入超时
            okHttpClientBuilder.readTimeout(15, TimeUnit.SECONDS);//10秒读取超时
            //okHttpClientBuilder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
            //okHttpClientBuilder.proxy(Proxy.NO_PROXY);
           /* CertificatePinner certificatePinner = new CertificatePinner.Builder()
                    .add("explorer.hbtcchain.io", "sha256/ONUV2iewvbpTvPliXQM1Mol7j9PPZgGYT6/7WhJ0yy0=")
                    .add("explorer.hbtcchain.io", "sha256/4H6OXny7MqJPbCOTpHyS0fSSUeHk/I5nKbIyuQwnfsA=")
                    .add("dex.hbtcchain.io","sha256/ONUV2iewvbpTvPliXQM1Mol7j9PPZgGYT6/7WhJ0yy0=")
                    .add("dex.hbtcchain.io","sha256/4H6OXny7MqJPbCOTpHyS0fSSUeHk/I5nKbIyuQwnfsA=")
                    .build();*/
            //okHttpClientBuilder.certificatePinner(certificatePinner);

            if(BuildConfig.DEBUG){
                okHttpClientBuilder.addNetworkInterceptor(new StethoInterceptor());
            }

            HttpsUtils.setCertificate(iNetworkRequiredInfo.getApplicationContext(),okHttpClientBuilder, R.raw.bhexchain);

            okHttpClientBuilder.addInterceptor(new CommonRequestInterceptor(iNetworkRequiredInfo));

            //okHttpClientBuilder.addInterceptor(new CommonResponseInterceptor());
            /*okHttpClientBuilder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    System.out.println("NetworkApi====>:hostname=="+hostname);
                    //LogUtils.debug("NetworkApi====>:hostname=="+hostname);
                    if("explorer.bhexchain.com".equals(hostname)){
                        return true;
                    }else{
                        HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
                        return hv.verify(hostname,session);
                    }
                    //return true;
                }
            });*/

            if (iNetworkRequiredInfo != null && (iNetworkRequiredInfo.isDebug())) {
                HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
                httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                okHttpClientBuilder.addInterceptor(httpLoggingInterceptor);
            }
            mOkHttpClient = okHttpClientBuilder.build();
        }
        return mOkHttpClient;

    }

    public <T> ObservableTransformer<T, T> applySchedulers(final Observer<T> observer) {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                Log.d("NetworkApi","applySchedulers");
                Observable<T> observable = upstream.subscribeOn(Schedulers.io());
                observable = observable.observeOn(AndroidSchedulers.mainThread());
                observable = (Observable<T>)observable.map(getAppErrorHandler());
                observable.subscribe(observer);
                //observable.onErrorResumeNext(new HttpErrorHandler<T>());
                return observable;
            }
        };
    }

    public <T> ObservableTransformer<T, T> applySchedulersMap() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                Observable<T> observable = (Observable<T>)upstream.map(getAppErrorHandler());
                return observable;
            }
        };
    }

    public <T> ObservableTransformer<T, T> applySchedulersError() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                Observable<T> observable  = upstream.onErrorResumeNext(new HttpErrorHandler<T>());
                return observable;
            }
        };

    }

    protected abstract Interceptor getInterceptor();

    protected abstract <T> Function<T, T> getAppErrorHandler();
}
