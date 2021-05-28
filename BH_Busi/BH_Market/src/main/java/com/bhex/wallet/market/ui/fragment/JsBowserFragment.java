package com.bhex.wallet.market.ui.fragment;

import android.text.TextUtils;
import android.webkit.WebView;

import com.alibaba.fastjson.JSONObject;
import com.bhex.network.utils.JsonUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.common.browse.BaseBowserFragment;
import com.bhex.wallet.common.browse.wv.WVJBWebViewClient;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.market.model.H5Sign;

import java.util.ArrayList;
import java.util.List;


public abstract class JsBowserFragment extends BaseBowserFragment {

    @Override
    protected WVJBWebViewClient getWVJBWebViewClient(WebView webView) {
        return new MyWebViewClient(webView);
    }


    class MyWebViewClient extends WVJBWebViewClient {
        public MyWebViewClient(WebView webView) {
            super(webView,((data, callback) -> {
                callback.callback("Response for message from ObjC!");
            }));

            registerHandler("connect",(data,callback)->{
                DexResponse<JSONObject> dexResponse = new DexResponse<JSONObject>(200,"OK");
                callback.callback(JsonUtils.toJson(dexResponse));
            });

            registerHandler("get_account",(data,callback) -> {
                DexResponse<JSONObject> dexResponse = new DexResponse<JSONObject>(200,"OK");
                dexResponse.data = new JSONObject();
                dexResponse.data.put("address", BHUserManager.getInstance().getCurrentBhWallet().address);
                callback.callback(JsonUtils.toJson(dexResponse));
            });

            registerHandler("sign",(data, callback) -> {
                if(data==null){
                    return;
                }
                LogUtils.d("JsBowserFragment====>","sign=="+data.toString());
                if(TextUtils.isEmpty(data.toString())){
                    return;
                }
                if(data.toString().startsWith("[") ){
                    List<H5Sign> h5Signs = JsonUtils.getListFromJson(data.toString(), H5Sign.class);
                    PayDetailFragment.newInstance().showDialog(getChildFragmentManager(),PayDetailFragment.class.getSimpleName(),h5Signs);
                    callbackMaps.put(h5Signs.get(0).type,callback);
                }else{
                    H5Sign h5Sign = JsonUtils.fromJson(data.toString(), H5Sign.class);
                    List<H5Sign> h5Signs = new ArrayList<>();
                    h5Signs.add(h5Sign);
                    PayDetailFragment.newInstance().showDialog(getChildFragmentManager(),PayDetailFragment.class.getSimpleName(),h5Signs);
                    callbackMaps.put(h5Sign.type,callback);
                }

            });
        }
    }

    public static class DexResponse<T>{
        public int code;
        public String msg;
        public T data;

        public DexResponse(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }


}
