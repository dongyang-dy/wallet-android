package com.bhex.wallet.mnemonic.ui.fragment;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;

import com.bhex.network.mvx.base.BaseDialogFragment;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.network.app.BaseApplication;
import com.bhex.network.mvx.base.BaseBottomSheetDialog;
import com.bhex.tools.language.LocalManageUtil;
import com.bhex.tools.utils.FileUtils;
import com.bhex.wallet.common.enums.BH_BUSI_URL;
import com.bhex.wallet.mnemonic.R;
import com.just.agentweb.AbsAgentWebSettings;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.DefaultWebClient;
import com.just.agentweb.IAgentWebSettings;
import com.just.agentweb.WebChromeClient;

import java.util.Locale;

/**
 *
 */
public class GlobalTipsFragment extends BaseDialogFragment {

    private CheckedTextView check_agreement;
    //private AppCompatTextView tv_agreement;
    protected AgentWeb mAgentWeb;

    public GlobalTipsFragment() {
        // Required empty public constructor
    }

    GlobalOnClickListenter globalOnClickListenter;

    private boolean isCheck;

    @Override
    public int getLayout() {
        return R.layout.fragment_global_tips;
    }

    @Override
    public void onStart() {
        super.onStart();

        Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getAttributes().windowAnimations = R.style.bottomDialogStyle;

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;

        params.width = ViewGroup.LayoutParams.MATCH_PARENT;

        //params.width = dm.widthPixels;
        params.height = dm.heightPixels- PixelUtils.dp2px(BaseApplication.getInstance(),60);

        window.setAttributes(params);

    }

    public static void showDialog(FragmentManager fm, String tag,GlobalOnClickListenter globalOnClickListenter,Boolean isCheck){
        GlobalTipsFragment fragment = new GlobalTipsFragment();
        fragment.setGlobalOnClickListenter(globalOnClickListenter);
        fragment.isCheck = isCheck;
        fragment.show(fm,tag);
    }

    @Override
    protected void initView() {
        super.initView();
        mAgentWeb = AgentWeb.with(this)//
                .setAgentWebParent(mRootView.findViewById(R.id.layout_main), -1, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT))//??????AgentWeb???????????????
                .useDefaultIndicator(ContextCompat.getColor(getContext(), com.bhex.wallet.common.R.color.blue_bg), 3)//?????????????????????????????????-1????????????????????????2????????????dp???
                .setAgentWebWebSettings(getSettings())//?????? IAgentWebSettings???
                .setWebChromeClient(new WebChromeClient()) //WebChromeClient
                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK) //???????????? Android 4.2.2 ??????????????????????????? ?????????AgentWebView????????????
                .setMainFrameErrorView(com.bhex.wallet.common.R.layout.agentweb_error_page, -1) //??????1?????????????????????????????????2??????????????????ID -1???????????????????????????????????? AgentWeb 3.0.0 ?????????
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK)//???????????????????????????????????????????????????????????? AgentWeb 3.0.0 ?????????
                .createAgentWeb()//??????AgentWeb???
                .ready()//?????? WebSettings???
                .go(BH_BUSI_URL.????????????.getGotoUrl(getContext()));

        //????????????
        WebSettings webSettings = mAgentWeb.getAgentWebSettings().getWebSettings();
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GradientDrawable drawable = ShapeUtils.getRoundRectTopDrawable(PixelUtils.dp2px(getContext(),6), ColorUtil.getColor(getContext(),R.color.app_bg),true,0);
        mRootView.setBackground(drawable);
        check_agreement = mRootView.findViewById(R.id.check_agreement);
        check_agreement.setChecked(isCheck);
        //webView= mRootView.findViewById(R.id.webView);
        /*tv_agreement = mRootView.findViewById(R.id.tv_agreement);
        Locale locale = LocalManageUtil.getSetLanguageLocale(getActivity());
        if(locale!=null && locale.getLanguage().contains("zh")){
            String agreement = FileUtils.loadStringByAssets(BaseApplication.getInstance(),"zh.txt").replace("\\n", "\n");
            tv_agreement.setText(agreement);
        }else{
            String agreement = FileUtils.loadStringByAssets(BaseApplication.getInstance(),"en.txt").replace("\\n", "\n");
            tv_agreement.setText(agreement);
        }*/
        LogUtils.d("url===",BH_BUSI_URL.????????????.getGotoUrl(getContext()));
        //webView.loadUrl(BH_BUSI_URL.????????????.getGotoUrl(getContext()));
        addEvent();
    }

    public IAgentWebSettings getSettings() {
        AbsAgentWebSettings webSettings =  new AbsAgentWebSettings() {
            private AgentWeb mAgentWeb;
            @Override
            protected void bindAgentWebSupport(AgentWeb agentWeb) {
                this.mAgentWeb = agentWeb;
            }

            @Override
            public IAgentWebSettings toSetting(WebView webView) {
                IAgentWebSettings iAgentWebSettings = super.toSetting(webView);
                iAgentWebSettings.getWebSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                return iAgentWebSettings;
            }
        };
        return  webSettings;
    }

    /**
     * ????????????
     */
    private void addEvent() {
        check_agreement.setOnClickListener(v -> {
            check_agreement.setChecked(!check_agreement.isChecked());
        });

        mRootView.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            dismiss();
            if(globalOnClickListenter!=null){
                globalOnClickListenter.onCheckClickListener(null,check_agreement.isChecked());
            }
        });
    }


    /*public GlobalOnClickListenter getGlobalOnClickListenter() {
        return globalOnClickListenter;
    }*/

    public void setGlobalOnClickListenter(GlobalOnClickListenter globalOnClickListenter) {
        this.globalOnClickListenter = globalOnClickListenter;
    }

    public interface GlobalOnClickListenter{
        public void onCheckClickListener(View view,boolean isCheck);
    }

}
