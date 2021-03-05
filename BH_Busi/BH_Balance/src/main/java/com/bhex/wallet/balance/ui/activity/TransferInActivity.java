package com.bhex.wallet.balance.ui.activity;

import androidx.appcompat.widget.AppCompatTextView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.R2;
import com.bhex.wallet.balance.ui.viewhodler.TransferInVH;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.model.BHToken;
import com.gyf.immersionbar.ImmersionBar;

import butterknife.BindView;

/**
 * @author gongdongyang
 * 2020-4-7 10:29:12
 * 转入
 */

@Route(path= ARouterConfig.Balance.Balance_transfer_in)
public class TransferInActivity extends BaseActivity {

    @Autowired(name="symbol")
    String  symbol;

    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;

    BHToken mSymbolToken;

    public TransferInVH mTransferInVH;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_transfer_in;
    }

    @Override
    protected void initView() {
        ImmersionBar.with(this)
                .statusBarColor(R.color.page_bg_color)
                .statusBarDarkFont(true)
                .navigationBarDarkIcon(true)
                .fitsSystemWindows(true).init();

        ARouter.getInstance().inject(this);
        tv_center_title.setText(getString(R.string.transfer_in));

        mTransferInVH = new TransferInVH(this,findViewById(R.id.root_view));
        mTransferInVH.updateTokenInfo(symbol);
    }

    @Override
    protected void addEvent() {


    }

}
