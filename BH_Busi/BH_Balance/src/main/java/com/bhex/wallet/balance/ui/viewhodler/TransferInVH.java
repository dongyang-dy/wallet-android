package com.bhex.wallet.balance.ui.viewhodler;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.network.RxSchedulersHelper;
import com.bhex.network.observer.BHProgressObserver;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.FileUtils;
import com.bhex.tools.utils.ImageLoaderUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.tools.utils.QRCodeEncoder;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.balance.ui.activity.TransferInActivity;
import com.bhex.wallet.balance.ui.fragment.DepositTipsFragment;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHToken;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import io.reactivex.Observable;

/**
 * @author gongdongyang
 * 2020-12-6 22:59:29
 */
public class TransferInVH {
    public TransferInActivity mActivity;

    public View mRootView;

    //token-icon
    public AppCompatImageView iv_token_icon;
    //token-二维码地址
    public AppCompatImageView iv_address_qr;
    //token-文本地址
    public AppCompatTextView tv_token_address;

    //保存二维码
    public AppCompatTextView btn_save_qr;
    //复制地址
    public AppCompatTextView btn_copy_address;

    public FrameLayout layout_token_icon;

    //public LinearLayout layout_fee_tips;

    public TransferInVH( TransferInActivity mActivity,View mRootView) {
        this.mActivity = mActivity;
        this.mRootView = mRootView;
        //初始化布局
        iv_token_icon = mRootView.findViewById(R.id.iv_token_icon);
        iv_address_qr = mRootView.findViewById(R.id.iv_address_qr);

        tv_token_address = mRootView.findViewById(R.id.tv_token_address);
        btn_save_qr = mRootView.findViewById(R.id.btn_save_qr);
        btn_copy_address = mRootView.findViewById(R.id.btn_copy_address);

        layout_token_icon = mRootView.findViewById(R.id.layout_token_icon);

        //设置按钮为圆角
        GradientDrawable btn_save_drawable = ShapeUtils.getRoundRectDrawable(PixelUtils.dp2px(mActivity,100),
                ColorUtil.getColor(mActivity,R.color.btn_gray_bg_color));
        btn_save_qr.setBackgroundDrawable(btn_save_drawable);

        GradientDrawable btn_copy_drawable = ShapeUtils.getRoundRectDrawable(PixelUtils.dp2px(mActivity,100),
                ColorUtil.getColor(mActivity,R.color.btn_blue_bg_color));
        btn_copy_address.setBackgroundDrawable(btn_copy_drawable);

        //设置圆环
        GradientDrawable token_ring_drawable = ShapeUtils.getRoundRectStrokeDrawable(PixelUtils.dp2px(mActivity,58), Color.TRANSPARENT,
                ColorUtil.getColor(mActivity,R.color.white),PixelUtils.dp2px(mActivity,2));
        layout_token_icon.setBackgroundDrawable(token_ring_drawable);

        //点击事件
        //复制地址
        btn_copy_address.setOnClickListener(v -> {
            if (TextUtils.isEmpty(tv_token_address.getText())) {
                return;
            }
            ToolUtils.copyText(tv_token_address.getText().toString(), mActivity);
            ToastUtils.showToast(mActivity.getString(R.string.copyed));
        });

        //保存二维码事件
        btn_save_qr.setOnClickListener(v->{
            requestPermissions();
        });

    }

    public void updateTokenInfo(String symbol){
        //token-logo
        BHToken token = SymbolCache.getInstance().getBHToken(symbol);
        ImageLoaderUtil.loadImageView(mActivity,token.logo,iv_token_icon,R.mipmap.ic_default_coin);

        BHWallet currentWallet = BHUserManager.getInstance().getCurrentBhWallet();
        //二维码地址
        Bitmap bitmap = QRCodeEncoder.syncEncodeQRCode(currentWallet.address, PixelUtils.dp2px(mActivity,168),
                ColorUtil.getColor(mActivity,android.R.color.black));
        iv_address_qr.setImageBitmap(bitmap);
        //文本地址
        tv_token_address.setText(currentWallet.address);

    }


    //保存二维码
    private void requestPermissions() {
        //获取保存文件权限
        final RxPermissions rxPermissions = new RxPermissions(mActivity);
        rxPermissions
                .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(permission -> {
                    if (permission.granted) {
                        // 用户已经同意该权限
                        saveQRAction();
                        //startActivityForResult(IntentUtils.getDocumentPickerIntent(IntentUtils.DocumentType.IMAGE), REQUEST_IMAGE);
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                        LogUtils.d(permission.name + " is denied. More info should be provided.");
                    } else {
                        // 用户拒绝了该权限，并且选中『不再询问』
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

        /*Bitmap bitmap = QREncodUtil.createQRCode(tv_token_address.getText().toString(),
                PixelUtils.dp2px(mActivity,160),PixelUtils.dp2px(mActivity,160),null);*/

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
