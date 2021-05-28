package com.bhex.wallet.market.ui.fragment;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.fastjson.JSONObject;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.utils.JsonUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.language.LocalManageUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.viewmodel.TransactionViewModel;
import com.bhex.wallet.common.browse.wv.WVJBWebViewClient;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.event.AccountEvent;
import com.bhex.wallet.common.event.RequestTokenEvent;
import com.bhex.wallet.market.R;
import com.bhex.wallet.market.R2;
import com.bhex.wallet.market.event.H5SignCacelEvent;
import com.bhex.wallet.market.event.H5SignEvent;
import com.bhex.wallet.market.model.H5Sign;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author gongdongyang
 * 2020-9-6 16:42:41
 */
@Route(path = ARouterConfig.Market.market_dex,name = "market-dex")
public class MarketFragment extends JsBowserFragment {

    @BindView(R2.id.iv_back)
    AppCompatImageView iv_back;
    @BindView(R2.id.iv_refresh)
    AppCompatImageView ivRefresh;
    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;
    private List<H5Sign> mH5Signs;

    private TransactionViewModel transactionViewModel;
    private String mTokenId;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_market;
    }

    @Override
    public View getWebRootView() {
        return mRootView;
    }

    @Override
    protected void initView() {
        mTokenId = getArgumentValue(BHConstants.GO_TOKEN);
        super.initView();

        tv_center_title.setText(getString(R.string.tab_trade));
        transactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);
        transactionViewModel.mutableLiveData.observe(this,ldm -> {
            updateTransferStatus(ldm);
        });
    }

    @Override
    protected void addEvent() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public String getUrl() {
        StringBuffer url = new StringBuffer(BHConstants.MARKET_URL).append("/swap");
        if(!TextUtils.isEmpty(mTokenId)){
            url = url.append("/").append(mTokenId);
        }
        String v_local_display = ToolUtils.getLocalString(getYActivity());
        url = url.append("?lang=").append(v_local_display);
        return url.toString();
    }

    @OnClick({R2.id.iv_refresh,R2.id.iv_back,R2.id.iv_close})
    public void onClickView(View view) {
        if (R.id.iv_refresh == view.getId()) {
            startRefreshAction(view);
            mAgentWeb.getUrlLoader().loadUrl(getUrl());
        } else if(R.id.iv_back == view.getId()){
            if(!mAgentWeb.back()){
                getActivity().finish();
            }
        }else if(R.id.iv_close == view.getId()){
            getActivity().finish();
        }
    }
    ObjectAnimator  objectAnimator = null;
    private void startRefreshAction(View view) {
        objectAnimator = ObjectAnimator.ofFloat(view,"rotation",0,360f)
                .setDuration(800);
        objectAnimator.setRepeatCount(-1);
        view.setPivotX(view.getWidth() / 2);
        view.setPivotY(view.getHeight()/ 2);
        objectAnimator.start();
    }

    @Override
    protected void callbackProgress(WebView view, int newProgress) {
        if(newProgress==100 && objectAnimator!=null){
            objectAnimator.setRepeatCount(1);
            objectAnimator.pause();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void requestToken(RequestTokenEvent tokenEvent){
        mTokenId = tokenEvent.mToken;
        mAgentWeb.getUrlLoader().loadUrl(getUrl());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changeAccount(AccountEvent walletEvent){
        mAgentWeb.getUrlLoader().loadUrl(getUrl());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void signMessage(H5SignEvent h5SignEvent){
        mH5Signs = h5SignEvent.h5Sign;
        transactionViewModel.create_dex_transcation(getYActivity(),h5SignEvent.h5Sign.get(0).type,JsonUtils.toJson(h5SignEvent.h5Sign),h5SignEvent.data);
    }

    private void updateTransferStatus(LoadDataModel ldm) {
        if(ToolUtils.checkListIsEmpty(mH5Signs)){
            return;
        }
        WVJBWebViewClient.WVJBResponseCallback callback = callbackMaps.get(mH5Signs.get(0).type);
        if(callback==null){
            return;
        }

        DexResponse<JSONObject> dexResponse = new DexResponse(ldm.code,ldm.msg);
        dexResponse.data = JSONObject.parseObject(ldm.getData().toString());
        callback.callback(JsonUtils.toJson(dexResponse));
        callbackMaps.remove(mH5Signs.get(0).type);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void cancelH5Event(String h5SignKey){
        if(TextUtils.isEmpty(h5SignKey)){
            return;
        }

        WVJBWebViewClient.WVJBResponseCallback callback = callbackMaps.get(h5SignKey);
        if(callback==null){
            return;
        }

        DexResponse<JSONObject> dexResponse = new DexResponse(400,"User rejected");
        dexResponse.data = JSONObject.parseObject("");
        callback.callback(JsonUtils.toJson(dexResponse));
        callbackMaps.remove(h5SignKey);
    }

    @Override
    public View getBackView() {
        return iv_back;
    }


    private String getArgumentValue(String key){
        String result = "";
        if(getArguments()!=null){
            result = getArguments().getString(key,"1");
        }
        return result;
    }


    public static MarketFragment getInstance(String go_token){
        MarketFragment fragment = new MarketFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BHConstants.GO_TOKEN,go_token);
        fragment.setArguments(bundle);
        return fragment;
    }
}
