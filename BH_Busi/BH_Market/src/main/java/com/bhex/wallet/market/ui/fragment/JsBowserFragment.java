package com.bhex.wallet.market.ui.fragment;

import android.webkit.WebView;

import com.alibaba.fastjson.JSONObject;
import com.bhex.network.utils.JsonUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.common.browse.BaseBowserFragment;
import com.bhex.wallet.common.browse.wv.WVJBWebViewClient;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.market.model.H5Sign;


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
                //dexResponse.data.put("address", "HBCjFjeC8LEQjKDggRiNn9YEeS3KtzkPhccU");
                dexResponse.data.put("address", BHUserManager.getInstance().getCurrentBhWallet().address);
                callback.callback(JsonUtils.toJson(dexResponse));
            });

            registerHandler("sign",(data, callback) -> {
                if(data==null){
                    return;
                }
                //LogUtils.d("JsBowserFragment====>","sign=="+data.toString());
                H5Sign h5Sign = JsonUtils.fromJson(data.toString(), H5Sign.class);
                PayDetailFragment.newInstance().showDialog(getChildFragmentManager(),PayDetailFragment.class.getSimpleName(),h5Sign);
                callbackMaps.put(h5Sign.type,callback);
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
