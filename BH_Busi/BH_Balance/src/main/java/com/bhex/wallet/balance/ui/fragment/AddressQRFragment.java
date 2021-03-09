package com.bhex.wallet.balance.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.network.mvx.base.BaseDialogFragment;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ImageLoaderUtil;
import com.bhex.tools.utils.QRCodeEncoder;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.R2;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.model.BHToken;

import butterknife.BindView;

/**
 * @author gongdongyang
 * 2020-9-4 16:08:31
 * 二维码地址
 */
public class AddressQRFragment extends BaseDialogFragment {

    @BindView(R2.id.tv_address_label)
    AppCompatTextView tv_address_label;
    @BindView(R2.id.layout_index_0)
    LinearLayout layout_index_0;

    private String symbol;
    private String address;

    @Override
    public int getLayout() {
        return R.layout.fragment_address_qr;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        //FrameLayout frame = view.findViewById(R.id.layout_index_1);
        /*GradientDrawable drawable = ShapeUtils.getRoundRectDrawable(16, Color.parseColor("#00FFFFFF"),Color.parseColor("#1A3375E0"));
        layout_index_0.setBackground(drawable);*/
        AppCompatImageView iv = view.findViewById(R.id.iv_token_icon);
        LogUtils.d("AddressQRFragment===>:","symbol=="+symbol);
        BHToken bhToken = CacheCenter.getInstance().getSymbolCache().getBHToken(symbol);

        ImageLoaderUtil.loadImageView(getContext(),bhToken.logo, iv,R.mipmap.ic_default_coin);
        AppCompatImageView iv_qr_code = view.findViewById(R.id.iv_qr_code);
        AppCompatTextView tv_token_address = view.findViewById(R.id.tv_token_address);
        //Bitmap bitmap = QREncodUtil.createQRCode(address, PixelUtils.dp2px(getContext(), 181), PixelUtils.dp2px(getContext(), 181), null);
        Bitmap bitmap = QRCodeEncoder.syncEncodeQRCode(address,PixelUtils.dp2px(getContext(),181), ColorUtil.getColor(getContext(),android.R.color.black));
        iv_qr_code.setImageBitmap(bitmap);
        tv_token_address.setText(address);

        FrameLayout layout_token_icon = view.findViewById(R.id.layout_token_icon);
        GradientDrawable ringDrawable = ShapeUtils.getRingDrawable(42, ColorUtil.getColor(getContext(), R.color.white), 36, PixelUtils.dp2px(getContext(), 6));
        layout_token_icon.setBackground(ringDrawable);

        AppCompatTextView tv_short_name = view.findViewById(R.id.tv_short_name);
        char initial = symbol.charAt(0);
        tv_short_name.setText(String.valueOf(initial).toUpperCase());

        view.findViewById(R.id.iv_close).setOnClickListener(v -> {
            if (!isHidden()) {
                dismiss();
            }
        });
        view.findViewById(R.id.btn_copy_address).setOnClickListener(v -> {
            if (TextUtils.isEmpty(address)) {
                return;
            }

            ToolUtils.copyText(address, getContext());
            ToastUtils.showToast(getString(R.string.copyed));
        });
        AppCompatTextView tv_friend_tips = view.findViewById(R.id.tv_friend_tips);

        if(address.startsWith(BHConstants.BHT_TOKEN.toUpperCase())){
            tv_address_label.setText(getString(R.string.hbtc_chain_address));
            tv_friend_tips.setVisibility(View.GONE);
        }else {

            tv_address_label.setText(getString(R.string.crosslink_deposit_address));
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(),R.color.blue_bg));
            String tips = getString(R.string.string_deposit_threshold_2);
            String amount_str = bhToken.deposit_threshold+bhToken.symbol.toUpperCase();
            SpannableString spannableStr = new SpannableString(tips+" "+amount_str);
            spannableStr.setSpan(foregroundColorSpan,tips.length(),tips.length()+amount_str.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv_friend_tips.setText(spannableStr);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getAttributes().windowAnimations = com.bhex.wallet.common.R.style.bottomDialogStyle;

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
    }

    public static AddressQRFragment showFragment(FragmentManager fm, String tag, String tokenName, String address) {
        AddressQRFragment fragment = new AddressQRFragment();
        LogUtils.d("AddressQRFragment===>:","symbol=uu="+fragment.symbol);
        fragment.address = address;
        fragment.symbol = tokenName;
        fragment.show(fm, tag);
        return fragment;
    }
}