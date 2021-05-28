package com.bhex.wallet.balance.ui.activity;

import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.R2;
import com.bhex.wallet.balance.event.TransctionEvent;
import com.bhex.wallet.balance.helper.BHTokenHelper;
import com.bhex.wallet.balance.ui.fragment.ChooseTokenFragment;
import com.bhex.wallet.balance.ui.pw.ChooseChainPW;
import com.bhex.wallet.balance.ui.viewhodler.TransferInCrossVH;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.model.BHChain;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.utils.LiveDataBus;
import com.bhex.wallet.common.viewmodel.BalanceViewModel;
import com.gyf.immersionbar.ImmersionBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;

/**
 * @author gdy
 * 跨链充值
 */
@Route(path = ARouterConfig.Balance.Balance_transfer_in_cross, name = "跨链充值")
public class TransferInCrossActivity extends BaseActivity {

    @Autowired(name= BHConstants.SYMBOL)
    String m_select_symbol;

    BHToken bhToken;

    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;

    @BindView(R2.id.btn_save_qr)
    AppCompatTextView btn_save_qr;

    @BindView(R2.id.btn_copy_address)
    AppCompatTextView btn_copy_address;

    TransferInCrossVH transferCrossVH;

    //资产
    private BalanceViewModel balanceViewModel;

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
        bhToken = SymbolCache.getInstance().getBHToken(m_select_symbol);
        transferCrossVH = new TransferInCrossVH(this,findViewById(R.id.root_view),m_select_symbol);
        transferCrossVH.updateTokenInfo(m_select_symbol);

    }

    @Override
    protected void addEvent() {
        EventBus.getDefault().register(this);

        //资产
        balanceViewModel = ViewModelProviders.of(this).get(BalanceViewModel.class);
        //资产订阅
        LiveDataBus.getInstance().with(BHConstants.Label_Account, LoadDataModel.class).observe(this, ldm->{
            if(ldm.loadingStatus== LoadingStatus.SUCCESS){
                updateAddressStatus();
            }
        });
        //选择币种
        transferCrossVH.layout_select_token.setOnClickListener(this::chooseTokenAction);
        //选择链
        transferCrossVH.layout_select_chain.setOnClickListener(this::chooseChainAction);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    //选择币种
    private void chooseTokenAction(View view) {
        ChooseTokenFragment fragment = ChooseTokenFragment.showFragment(m_select_symbol,chooseTokenListener);
        fragment.show(getSupportFragmentManager(),ChooseTokenFragment.class.getName());
    }

    //选择链
    private void chooseChainAction(View view) {
        //
        List<BHChain> chainList = BHTokenHelper.getBHChainList(m_select_symbol);
        /*ChooseChainFragment fragment = ChooseChainFragment.getInstance(chainList,transferCrossVH.mChainToken.chain,this::chooseChainListener);
        fragment.show(getSupportFragmentManager(),ChooseChainFragment.class.getName());*/
        ChooseChainPW chainPW = new ChooseChainPW(this,chainList,transferCrossVH.mChainToken.chain,this::chooseChainListener);
        chainPW.showAsDropDown(view,0, PixelUtils.dp2px(this,2));
    }

    private ChooseTokenFragment.OnChooseTokenListener chooseTokenListener = new ChooseTokenFragment.OnChooseTokenListener() {
        @Override
        public void onChooseClickListener(String symbol, int position) {
            m_select_symbol = symbol;
            transferCrossVH.mChainToken = BHTokenHelper.getCrossDefaultToken(m_select_symbol);
            transferCrossVH.updateTokenInfo(symbol);
        }
    };

    //选择链回调
    private void chooseChainListener(String chain) {
        transferCrossVH.mChainToken = BHTokenHelper.getCrossBHToken(m_select_symbol,chain);
        transferCrossVH.updateTokenInfo(m_select_symbol);
    }

    @Subscribe(threadMode= ThreadMode.MAIN)
    public void changeStatus(TransctionEvent transctionEvent){
        transferCrossVH.updateTokenInfo(m_select_symbol);
    }

    public void updateAddressStatus(){
        transferCrossVH.updateTokenInfo(m_select_symbol);
    }

}
