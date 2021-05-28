package com.bhex.wallet.market.ui.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.lib.uikit.widget.EmptyLayout;
import com.bhex.network.app.BaseApplication;
import com.bhex.network.mvx.base.BaseDialogFragment;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.common.ui.fragment.Password30PFragment;
import com.bhex.wallet.market.R;
import com.bhex.wallet.market.R2;
import com.bhex.wallet.market.event.H5SignEvent;
import com.bhex.wallet.market.helper.PayDetailHelper;
import com.bhex.wallet.market.model.H5Sign;
import com.bhex.wallet.market.model.PayDetailItem;
import com.bhex.wallet.market.viewmodel.PayDetailViewModel;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.material.button.MaterialButton;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author gongdongyang
 * 支付详情页面
 * 2020-9-3 16:01:51
 */
public class PayDetailFragment extends BaseDialogFragment {

    @BindView(R2.id.tv_pay_origin)
    AppCompatTextView tv_pay_origin;
    @BindView(R2.id.rec_pay)
    RecyclerView rec_pay;
    @BindView(R2.id.btn_confrim)
    MaterialButton btn_confrim;
    @BindView(R2.id.empty_layout)
    EmptyLayout empty_layout;

    private List<H5Sign> mH5Sign;

    private List<PayDetailItem> mDatas;

    private PayDetailAdapter mPayDetailAdapter;

    //private TransactionViewModel transactionViewModel;
    private PayDetailViewModel payDetailViewModel;


    @Override
    public int getLayout() {
        return R.layout.fragment_pay_detail;
    }

    @Override
    public void onStart() {
        super.onStart();
        //setStyle(STYLE_NO_FRAME,R.style.DuobaoDialog);
        Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getAttributes().windowAnimations = R.style.bottomDialogStyle;

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = dm.widthPixels;
        window.setAttributes(params);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        GradientDrawable drawable = ShapeUtils.getRoundRectTopDrawable(PixelUtils.dp2px(getContext(), 6),
                ColorUtil.getColor(getContext(), R.color.app_default_bg), true, 0);
        mRootView.setBackground(drawable);
        view.findViewById(R.id.iv_close).setOnClickListener(v -> {
            dismiss();
        });

        tv_pay_origin.setText(BHConstants.MARKET_URL);
        //mDatas = PayDetailHelper.loadPayItemByType(getContext(), mH5Sign);
        //rec_pay.setAdapter(mPayDetailAdapter = new PayDetailAdapter(mDatas));

        //请求币种信息
        //
        List<String> tokenList = PayDetailHelper.loadPayItemToken(getContext(),mH5Sign.get(0));
        payDetailViewModel = ViewModelProviders.of(this).get(PayDetailViewModel.class);
        payDetailViewModel.tokenLiveData.observe(this,ldm->{
            updateTokenDetail();
        });

        if(!ToolUtils.checkListIsEmpty(tokenList)){
            payDetailViewModel.queryTokenDetail(this,tokenList);
        }else{
            updateTokenDetail();
        }

    }

    private void updateTokenDetail() {
        empty_layout.loadSuccess();
        mDatas = PayDetailHelper.loadPayItemByType(getContext(), mH5Sign.get(0));
        rec_pay.setAdapter(mPayDetailAdapter = new PayDetailAdapter(mDatas));
    }

    public static PayDetailFragment newInstance() {
        Bundle args = new Bundle();
        PayDetailFragment fragment = new PayDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void showDialog(FragmentManager fm, String tag, List<H5Sign> sign) {
        if(PayDetailHelper.isShowPayDetail(sign)){
            //PayDetailFragment fragment = new PayDetailFragment();
            mH5Sign = sign;
            show(fm, tag);
        }/*else if(!PayDetailHelper.isHasOrders(sign)){
            ToastUtils.showToast(BaseApplication.getInstance().getString(R.string.no_order));
        }*/else{
            mH5Sign = sign;
            Password30PFragment fragment = Password30PFragment.showPasswordDialog(fm,Password30PFragment.class.getName(),passwordClickListener,0,true);
            fragment.mHSignKey = mH5Sign.get(0).type;
        }
    }

    @OnClick({R2.id.btn_confrim})
    public void onViewClicked(View view) {
        Password30PFragment fragment =
                Password30PFragment.showPasswordDialog(getChildFragmentManager(),Password30PFragment.class.getName(),passwordClickListener,0,true);
        fragment.mHSignKey = mH5Sign.get(0).type;
    }

    public class PayDetailAdapter extends BaseQuickAdapter<PayDetailItem, BaseViewHolder> {

        public PayDetailAdapter(@org.jetbrains.annotations.Nullable List<PayDetailItem> data) {
            super(R.layout.item_pay_detail, data);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, PayDetailItem pdi) {
            holder.setText(R.id.tv_title, pdi.label);
            holder.setText(R.id.tv_info, pdi.info);
        }
    }

    Password30PFragment.PasswordClickListener passwordClickListener = (password, position, way) -> {
        if(isAdded() && !isHidden()){
            dismiss();
        }
        EventBus.getDefault().post(new H5SignEvent(mH5Sign,password));
    };


}