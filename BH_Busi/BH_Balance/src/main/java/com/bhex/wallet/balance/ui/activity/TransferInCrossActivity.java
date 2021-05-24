package com.bhex.wallet.balance.ui.activity;

import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.tools.constants.BHConstants;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.R2;
import com.bhex.wallet.balance.ui.fragment.ChooseTokenFragment;
import com.bhex.wallet.balance.ui.viewhodler.TransferInCrossVH;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.model.BHChain;
import com.bhex.wallet.common.model.BHToken;
import com.gyf.immersionbar.ImmersionBar;

import butterknife.BindView;

/**
 * @author gdy
 * 跨链充值
 */
@Route(path = ARouterConfig.Balance.Balance_transfer_in_cross, name = "跨链充值")
public class TransferInCrossActivity extends BaseActivity {

    @Autowired(name= BHConstants.SYMBOL)
    String  symbol;

    BHToken bhToken;

    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;

    @BindView(R2.id.btn_save_qr)
    AppCompatTextView btn_save_qr;

    @BindView(R2.id.btn_copy_address)
    AppCompatTextView btn_copy_address;

    TransferInCrossVH transferCrossVH;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_transfer_in_cross;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        tv_center_title.setText(getString(R.string.cross_deposit));

        ImmersionBar.with(this)
                .statusBarColor(R.color.page_bg_color)
                .statusBarDarkFont(true)
                .navigationBarDarkIcon(true)
                .fitsSystemWindows(true).init();

        //获取对应Token
        bhToken = SymbolCache.getInstance().getBHToken(symbol);
        transferCrossVH = new TransferInCrossVH(this,findViewById(R.id.root_view),symbol);
        transferCrossVH.updateTokenInfo(symbol);
    }

    @Override
    protected void addEvent() {
        //选择币种
        findViewById(R.id.layout_select_token).setOnClickListener(this::chooseTokenAction);

    }

    //选择币种
    private void chooseTokenAction(View view) {
        ChooseTokenFragment fragment = ChooseTokenFragment.showFragment(symbol,BH_BUSI_TYPE.跨链转账.value,chooseTokenListener);
        fragment.show(getSupportFragmentManager(),ChooseTokenFragment.class.getName());

    }

    private ChooseTokenFragment.OnChooseTokenListener chooseTokenListener = new ChooseTokenFragment.OnChooseTokenListener() {
        @Override
        public void onChooseClickListener(String symbol, int position) {
            transferCrossVH.updateTokenInfo(symbol);
        }
    };

}
