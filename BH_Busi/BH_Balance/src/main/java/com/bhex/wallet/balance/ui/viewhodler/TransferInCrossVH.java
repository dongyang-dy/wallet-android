package com.bhex.wallet.balance.ui.viewhodler;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.network.RxSchedulersHelper;
import com.bhex.network.observer.BHProgressObserver;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.language.LocalManageUtil;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.FileUtils;
import com.bhex.tools.utils.ImageLoaderUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.tools.utils.QRCodeEncoder;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.tools.utils.ViewUtil;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.balance.helper.BHTokenHelper;
import com.bhex.wallet.balance.ui.activity.TransferInCrossActivity;
import com.bhex.wallet.balance.ui.fragment.DepositTipsFragment;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.manager.SequenceManager;
import com.bhex.wallet.common.model.BHChain;
import com.bhex.wallet.common.model.BHToken;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import io.reactivex.Observable;

/**
 * @author gongdongyang
 * 2020-12-6 22:59:29
 */
public class TransferInCrossVH {

    private TransferInCrossActivity mActivity;
    private View view;
    public String mSymbol;
    public BHToken mChainToken;

    public View layout_select_token;
    //token-icon
    public AppCompatImageView iv_token_icon;
    //token-name
    public AppCompatTextView tv_token_name;

    public View layout_select_chain;
    //chain-icon
    public AppCompatImageView iv_chain_icon;
    //chain-name
    public AppCompatTextView tv_chain_name;

    //token-???????????????
    public AppCompatImageView iv_address_qr;
    //token-??????
    public AppCompatTextView tv_token_address;
    //???????????????
    public AppCompatTextView btn_save_qr;
    //????????????
    public AppCompatTextView btn_copy_address;
    //??????????????????
    //??????????????????
    public AppCompatTextView tv_min_deposit_amount;
    //??????????????????
    public AppCompatTextView tv_deposit_collect_amount;
    public LinearLayout layout_deposit;
    //
    public LinearLayout layout_genarate_address;

    //??????????????????
    public AppCompatTextView btn_genarate_address;

    //????????????
    public LinearLayout layout_deposit_contract;
    public AppCompatTextView tv_deposit_contract;
    public AppCompatTextView tv_contract_address;

    public TransferInCrossVH(TransferInCrossActivity activity, View view, String  symbol) {
        this.mActivity = activity;
        this.view = view;
        this.mSymbol = symbol;

        //???????????????
        btn_save_qr = view.findViewById(R.id.btn_save_qr);
        btn_copy_address = view.findViewById(R.id.btn_copy_address);
        //
        layout_select_token = view.findViewById(R.id.layout_select_token);
        iv_token_icon = view.findViewById(R.id.iv_token_icon);
        tv_token_name = view.findViewById(R.id.tv_token_name);

        layout_select_chain = view.findViewById(R.id.layout_select_chain);
        iv_chain_icon = view.findViewById(R.id.iv_chain_icon);
        tv_chain_name = view.findViewById(R.id.tv_chain_name);

        //
        iv_address_qr = view.findViewById(R.id.iv_address_qr);
        tv_token_address = view.findViewById(R.id.tv_token_address);

        tv_min_deposit_amount = view.findViewById(R.id.tv_min_deposit_amount);
        tv_deposit_collect_amount = view.findViewById(R.id.tv_deposit_collect_amount);

        layout_deposit_contract = view.findViewById(R.id.layout_deposit_contract);
        tv_deposit_contract = view.findViewById(R.id.tv_deposit_contract);
        tv_contract_address = view.findViewById(R.id.tv_contract_address);

        //?????????????????????
        GradientDrawable btn_save_drawable = ShapeUtils.getRoundRectDrawable(PixelUtils.dp2px(activity,100),
                ColorUtil.getColor(activity,R.color.btn_gray_bg_color));
        btn_save_qr.setBackgroundDrawable(btn_save_drawable);

        GradientDrawable btn_copy_drawable = ShapeUtils.getRoundRectDrawable(PixelUtils.dp2px(activity,100),
                ColorUtil.getColor(activity,R.color.btn_blue_bg_color));
        btn_copy_address.setBackgroundDrawable(btn_copy_drawable);

        layout_deposit = view.findViewById(R.id.layout_deposit);
        layout_genarate_address = view.findViewById(R.id.layout_genarate_address);

        //
        GradientDrawable btn_genarate_address_drawable = ShapeUtils.getRoundRectDrawable(PixelUtils.dp2px(activity,100),
                ColorUtil.getColor(activity,R.color.btn_blue_bg_color));
        btn_genarate_address = view.findViewById(R.id.btn_genarate_address);
        btn_genarate_address.setBackgroundDrawable(btn_genarate_address_drawable);

        //????????????
        btn_copy_address.setOnClickListener(v -> {
            if (TextUtils.isEmpty(tv_token_address.getText())) {
                return;
            }
            ToolUtils.copyText(tv_token_address.getText().toString(), mActivity);
            ToastUtils.showToast(mActivity.getString(R.string.copyed));
        });

        //?????????????????????
        btn_save_qr.setOnClickListener(v->{
            requestPermissions();
        });

        view.findViewById(R.id.tv_min_deposit).setOnClickListener(v->{
            DepositTipsFragment.newInstance(mActivity.getString(R.string.cross_min_deposit_count_tips))
                    .show(mActivity.getSupportFragmentManager(),DepositTipsFragment.class.getName());
        });

        view.findViewById(R.id.tv_deposit_collect).setOnClickListener(v->{
            DepositTipsFragment.newInstance(mActivity.getString(R.string.cross_collect_deposit_tips))
                    .show(mActivity.getSupportFragmentManager(),DepositTipsFragment.class.getName());
        });

        mChainToken = BHTokenHelper.getCrossDefaultToken(mSymbol);
        //?????????
        btn_genarate_address.setOnClickListener(v -> {
            if(btn_genarate_address.getText().toString().equals(mActivity.getString(R.string.cross_address_generatoring))){
                ToastUtils.showToast(mActivity.getString(R.string.link_outter_generating));
            }else{
                ARouter.getInstance()
                        .build(ARouterConfig.Balance.Balance_cross_address)
                        .withString(BHConstants.SYMBOL,mSymbol)
                        .withString(BHConstants.CHAIN,mChainToken.chain)
                        .navigation();
            }

        });

        GradientDrawable  bg_deposit_drawable = ShapeUtils.getRoundRectDrawable(
                (int)mActivity.getResources().getDimension(R.dimen.middle_radius_conner),
                ColorUtil.getColor(mActivity,R.color.color_1F4299FF));
        layout_deposit_contract.setBackground(bg_deposit_drawable);
    }

    public void updateTokenInfo(String symbol){
        try{
            this.mSymbol = symbol;
            BHToken showBhToken = SymbolCache.getInstance().getBHToken(mSymbol);
            ImageLoaderUtil.loadImageView(mActivity,showBhToken.logo,iv_token_icon,R.mipmap.ic_default_coin);
            tv_token_name.setText(showBhToken.name.toUpperCase());


            //???Token??????
            BHChain bhChain = BHTokenHelper.getBHChain(mChainToken.chain);
            //?????????
            ImageLoaderUtil.loadImageViewToCircle(mActivity,bhChain.logo,iv_chain_icon,R.mipmap.ic_default_coin);
            tv_chain_name.setText(bhChain.full_name);
            //????????????????????????
            String chain_address = BHBalanceHelper.queryAddressByChain(mChainToken.chain);
            //LogUtils.d("GenerateAddressActivity===>:","AddressStatus===="+SequenceManager.getInstance().getAddressStatus());

            if(!TextUtils.isEmpty(chain_address)){
                layout_deposit.setVisibility(View.VISIBLE);
                layout_genarate_address.setVisibility(View.GONE);
                //?????????
                Bitmap bitmap = QRCodeEncoder.syncEncodeQRCode(chain_address,PixelUtils.dp2px(mActivity,168),
                        ColorUtil.getColor(mActivity,android.R.color.black));
                iv_address_qr.setImageBitmap(bitmap);
                //???????????????
                tv_token_address.setText(chain_address);
            }else if(!TextUtils.isEmpty(SequenceManager.getInstance().getAddressStatus())){
                //??????????????????
                //ViewUtil.getListenInfo(btn_genarate_address);
                btn_genarate_address.setText(mActivity.getString(R.string.cross_address_generatoring));
                layout_deposit.setVisibility(View.GONE);
                layout_genarate_address.setVisibility(View.VISIBLE);
            }else{
                layout_deposit.setVisibility(View.GONE);
                layout_genarate_address.setVisibility(View.VISIBLE);
                btn_genarate_address.setText(mActivity.getString(R.string.click_make_cross_address));
            }

            //??????????????????
            String v_amount_str = mChainToken.deposit_threshold+" "+mChainToken.name.toUpperCase();
            tv_min_deposit_amount.setText(v_amount_str);

            //???????????????
            String v_amount_str2 = mChainToken.collect_fee+" "+mChainToken.chain.toUpperCase();
            tv_deposit_collect_amount.setText(v_amount_str2);

            //
            String v_deposit_contract = "";
            if(!TextUtils.isEmpty(mChainToken.issuer)){
                layout_deposit_contract.setVisibility(View.VISIBLE);

                if(LocalManageUtil.getSetLanguageLocale(mActivity).getLanguage().contains("zh")){
                    v_deposit_contract = String.format(mActivity.getString(R.string.string_deposit_contract),
                            bhChain.full_name,
                            showBhToken.name.toUpperCase()).concat(":");
                    //tv_deposit_contract.setText(v_deposit_contract);
                }else{
                    v_deposit_contract = String.format(mActivity.getString(R.string.string_deposit_contract),
                            showBhToken.name.toUpperCase(),
                            bhChain.full_name).concat(":");
                    //tv_deposit_contract.setText(v_deposit_contract);
                }

                SpannableString v_span_deposit_contract = new SpannableString(v_deposit_contract);
                ForegroundColorSpan span_color = new ForegroundColorSpan(ColorUtil.getColor(mActivity,R.color.transfer_highlight_text_color));

                int start_index_0 = v_deposit_contract.indexOf(bhChain.full_name);
                int end_index_0 = start_index_0 +bhChain.full_name.length();
                v_span_deposit_contract.setSpan(CharacterStyle.wrap(span_color), start_index_0, end_index_0, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                int start_index_1 = v_deposit_contract.indexOf(showBhToken.name.toUpperCase());
                int end_index_1 = start_index_1 +showBhToken.name.length();
                v_span_deposit_contract.setSpan(CharacterStyle.wrap(span_color), start_index_1, end_index_1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                tv_deposit_contract.setText(v_span_deposit_contract);
                tv_contract_address.setText(mChainToken.issuer);
            }else{
                layout_deposit_contract.setVisibility(View.GONE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    //???????????????
    private void requestPermissions() {
        //????????????????????????
        final RxPermissions rxPermissions = new RxPermissions(mActivity);
        rxPermissions
                .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(permission -> {
                    if (permission.granted) {
                        // ???????????????????????????
                        saveQRAction();
                        //startActivityForResult(IntentUtils.getDocumentPickerIntent(IntentUtils.DocumentType.IMAGE), REQUEST_IMAGE);
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        // ????????????????????????????????????????????????????????????Never ask again???,??????????????????????????????????????????????????????????????????
                        LogUtils.d(permission.name + " is denied. More info should be provided.");
                    } else {
                        // ?????????????????????????????????????????????????????????
                        LogUtils.d(permission.name + " is denied.");
                    }
                });


    }

    public void saveQRAction(){
        BHProgressObserver observer = new BHProgressObserver<Boolean>(mActivity) {
            @Override
            protected void onSuccess(Boolean o) {
                super.onSuccess(o);
                ToastUtils.showToast(mActivity.getResources().getString(R.string.save_success));
            }
        };

        Bitmap bitmap = QRCodeEncoder.syncEncodeQRCode(tv_token_address.getText().toString(),PixelUtils.dp2px(mActivity,168),
                ColorUtil.getColor(mActivity,android.R.color.black));

        Observable.create(emitter -> {
            boolean flag = FileUtils.saveImageToGallery(mActivity,bitmap,tv_token_address.getText().toString()+".png");
            emitter.onNext(flag);
            emitter.onComplete();
        }).compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(mActivity)))
                .subscribe(observer);
    }
}
