package com.bhex.wallet.market.ui.activity;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.market.R;
import com.bhex.wallet.market.ui.fragment.MarketFragment;

/**
 * @author gdy
 * 2021-5-22 10:38:04
 */
@Route(path = ARouterConfig.Market.market_hdex,name = "market-dex")
public class HDexActivity extends BaseActivity {

    @Autowired(name = BHConstants.GO_TOKEN)
    public String go_token;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_h_dex;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
    }

    @Override
    protected void addEvent() {
        MarketFragment fragment = MarketFragment.getInstance(go_token);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragment).commit();
    }
}