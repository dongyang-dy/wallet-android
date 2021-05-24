package com.bhex.wallet.market.ui.activity;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.market.R;

/**
 * @author gdy
 * 2021-5-22 10:38:04
 */
@Route(path = ARouterConfig.Market.market_hdex,name = "market-dex")
public class HDexActivity extends BaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_h_dex;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void addEvent() {

    }
}