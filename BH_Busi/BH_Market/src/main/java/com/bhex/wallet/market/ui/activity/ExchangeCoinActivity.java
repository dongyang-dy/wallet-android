package com.bhex.wallet.market.ui.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.network.cache.stategy.CacheStrategy;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.balance.viewmodel.TransactionViewModel;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.AccountInfo;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.model.BHTokenMapping;
import com.bhex.wallet.common.tx.BHRawTransaction;
import com.bhex.wallet.common.tx.TxReq;
import com.bhex.wallet.common.ui.fragment.Password30PFragment;
import com.bhex.wallet.common.utils.LiveDataBus;
import com.bhex.wallet.common.viewmodel.BalanceViewModel;
import com.bhex.wallet.market.R;
import com.bhex.wallet.market.R2;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author gongdongyang
 * 2020-8-31 16:37:06
 * 兑币页面
 */
@Route(path = ARouterConfig.Market_exchange_coin, name = "兑币")
public class ExchangeCoinActivity extends BaseActivity
        implements Password30PFragment.PasswordClickListener,
        ChooseTokenFragment.ChooseTokenListener {

    private BHBalance mBhtBalance;
    private BHBalance mTokenBalance;

    @Autowired(name = BHConstants.SYMBOL)
    String mSymbol;
    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;
    @BindView(R2.id.tv_issue_token)
    AppCompatTextView tv_issue_token;
    @BindView(R2.id.tv_target_token)
    AppCompatTextView tv_target_token;
    @BindView(R2.id.inp_amount)
    AppCompatEditText inp_amount;
    @BindView(R2.id.tv_token_name)
    AppCompatTextView tv_token_name;
    @BindView(R2.id.btn_exchange_action)
    AppCompatTextView btn_exchange_action;
    @BindView(R2.id.iv_right_more)
    AppCompatImageView iv_right_more;
    @BindView(R2.id.layout_left)
    LinearLayout layout_left;
    @BindView(R2.id.layout_right)
    LinearLayout layout_right;
    @BindView(R2.id.iv_issue)
    AppCompatImageView iv_issue;
    @BindView(R2.id.iv_target)
    AppCompatImageView iv_target;

    @BindView(R2.id.tv_rate)
    AppCompatTextView tv_rate;

    protected TransactionViewModel mTransactionViewModel;
    protected BalanceViewModel mBalanceViewModel;
    private BHTokenMapping mTokenMapping;
    private List<BHTokenMapping> mMappingToknList;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_exchange_coin;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        tv_center_title.setText(getString(R.string.mapping_swap));
        mTokenMapping = CacheCenter.getInstance().getTokenMapCache().getTokenMappingOne(mSymbol.toUpperCase());
        mTokenBalance = BHBalanceHelper.getBHBalanceFromAccount(mSymbol);
        initMappingTokenView();
    }

    @Override
    protected void addEvent() {
        mTransactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);
        mTransactionViewModel.mutableLiveData.observe(this, ldm -> {
            updateTransferStatus(ldm);
        });

        mBalanceViewModel = ViewModelProviders.of(this).get(BalanceViewModel.class).build(this);
        //资产订阅
        LiveDataBus.getInstance().with(BHConstants.Label_Account, LoadDataModel.class).observe(this, ldm -> {
            if (ldm.loadingStatus == LoadingStatus.SUCCESS) {
                updateAssets((AccountInfo) ldm.getData());
            }
        });
        mBalanceViewModel.getAccountInfo(this, CacheStrategy.onlyRemote());
    }

    int view_left_left, view_right_left;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        view_left_left = layout_left.getLeft();
        view_right_left = layout_right.getLeft();
    }

    @OnClick({R2.id.btn_exchange_action, R2.id.iv_exchange,R2.id.layout_left,R2.id.layout_right})
    public void onClickView(View view) {
        if (view.getId() == R.id.btn_exchange_action) {
            exchangeAction();
        } else if (view.getId() == R.id.iv_exchange) {
            exchangeAnimatorAction();
        } else if(view.getId() == R.id.layout_left){
            ChooseTokenFragment frg = ChooseTokenFragment.showDialog(mSymbol,0,this);
            frg.show(getSupportFragmentManager(),ChooseTokenFragment.class.getName());

        } else if(view.getId() == R.id.layout_right){
            if(ToolUtils.checkListIsEmpty(mMappingToknList) || mMappingToknList.size()<=1){
               return;
            }
            ChooseTokenFragment frg = ChooseTokenFragment.showDialog(tv_issue_token.getText().toString(),1,chooseTokenListener);
            frg.mTargetSymbol = tv_target_token.getText().toString();
            frg.show(getSupportFragmentManager(),ChooseTokenFragment.class.getName());
        }
    }

    //兑换
    private void exchangeAction() {
        String map_amount = inp_amount.getText().toString().trim();
        if (TextUtils.isEmpty(map_amount)) {
            ToastUtils.showToast(getString(R.string.please_input_exchange_amount));
            return;
        }

        if (mTokenBalance == null || Double.valueOf(mTokenBalance.amount) <= 0) {
            ToastUtils.showToast(getResources().getString(R.string.not_have_amount) + mSymbol.toUpperCase());
            return;
        }

        if (Double.valueOf(mTokenBalance.amount) < Double.valueOf(map_amount)) {
            ToastUtils.showToast(getResources().getString(R.string.not_have_amount) + mSymbol.toUpperCase());
            return;
        }

        if (mBhtBalance == null || Double.valueOf(mBhtBalance.amount) <= 0) {
            ToastUtils.showToast(getResources().getString(R.string.not_have_amount) + BHConstants.BHT_TOKEN.toUpperCase());
            return;
        }

        Password30PFragment.showPasswordDialog(getSupportFragmentManager(),
                Password30PFragment.class.getName(),
                this, 0,true);
    }

    boolean flag = true;

    public void exchangeAnimatorAction() {
        /*leftAnimator();
        rightAnimator();
        flag = !flag;*/

        mSymbol = mTokenMapping.target_symbol;
        mTokenMapping = CacheCenter.getInstance().getTokenMapCache().getTokenMappingOne(mSymbol.toUpperCase());
        //tv_token_name.setText(mTokenMapping.coin_symbol.toUpperCase());
        mTokenBalance = BHBalanceHelper.getBHBalanceFromAccount(mSymbol);
        initMappingTokenView();
    }

    private void leftAnimator() {
        int offset = view_right_left - view_left_left;
        ObjectAnimator left_animator = null;
        if (flag) {
            left_animator = ObjectAnimator.ofFloat(layout_left, "x", view_right_left);
        } else {
            left_animator = ObjectAnimator.ofFloat(layout_left, "x", view_left_left);
        }
        left_animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mSymbol = mTokenMapping.target_symbol;
                mTokenMapping = CacheCenter.getInstance().getTokenMapCache().getTokenMappingOne(mSymbol.toUpperCase());
                tv_token_name.setText(mTokenMapping.coin_symbol.toUpperCase());
                mTokenBalance = BHBalanceHelper.getBHBalanceFromAccount(mSymbol);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        left_animator.setInterpolator(new AccelerateDecelerateInterpolator());
        left_animator.setDuration(800);
        left_animator.start();
    }

    private void rightAnimator() {
        ObjectAnimator right_animator = null;
        if (flag) {
            right_animator = ObjectAnimator.ofFloat(layout_right, "x", view_left_left);
        } else {
            right_animator = ObjectAnimator.ofFloat(layout_right, "x", view_right_left);
        }
        right_animator.setInterpolator(new AccelerateDecelerateInterpolator());
        right_animator.setDuration(800);
        right_animator.start();
    }

    @Override
    public void confirmAction(String password, int position, int way) {
        String coin_symbol = mTokenMapping.coin_symbol;
        String issue_symbol = mTokenMapping.issue_symbol;
        String map_amount = inp_amount.getText().toString().trim();
        //
        List<TxReq.TxMsg> tx_msg_list = BHRawTransaction.createSwapMappingMsg(issue_symbol,coin_symbol,map_amount);

        mTransactionViewModel.transferInnerExt(this,password, BHUserManager.getInstance().getDefaultGasFee().displayFee,tx_msg_list);
    }

    //更新兑换状态
    private void updateTransferStatus(LoadDataModel ldm) {
        if (ldm.loadingStatus == LoadingStatus.SUCCESS) {
            inp_amount.setText("");
            ToastUtils.showToast(getString(R.string.transfer_in_success));
        } else {
            ToastUtils.showToast(getString(R.string.transfer_in_fail));
        }
    }

    //更新资产
    private void updateAssets(AccountInfo accountInfo) {
        if (accountInfo == null) {
            return;
        }
        mBhtBalance = BHBalanceHelper.getBHBalanceFromAccount(BHConstants.BHT_TOKEN);
        mTokenBalance = BHBalanceHelper.getBHBalanceFromAccount(mSymbol);

        //
    }

    @Override
    public void clickTokenPosition(BHTokenMapping tokenMapping) {
        flag = true;
        mTokenMapping = tokenMapping;
        mSymbol = mTokenMapping.coin_symbol;
        mTokenBalance = BHBalanceHelper.getBHBalanceFromAccount(mSymbol);
        initMappingTokenView();
    }

    public void initMappingTokenView(){
        try{
            mMappingToknList = CacheCenter.getInstance().getTokenMapCache().getTokenMapping(mSymbol.toUpperCase());

            BHToken bh_coin_token = CacheCenter.getInstance().getSymbolCache().getBHToken(mTokenMapping.coin_symbol);
            BHToken bh_target_token = CacheCenter.getInstance().getSymbolCache().getBHToken(mTokenMapping.target_symbol);
            if(bh_coin_token.name.equalsIgnoreCase(bh_target_token.name)){
                String coin_token = bh_coin_token.name.toUpperCase().concat("(").concat(bh_coin_token.chain.toUpperCase()).concat(")");
                tv_issue_token.setText(coin_token);

                String target_token = bh_target_token.name.toUpperCase().concat("(").concat(bh_target_token.chain.toUpperCase()).concat(")");
                tv_target_token.setText(target_token);
            }else{
                tv_issue_token.setText(bh_coin_token.name.toUpperCase());
                tv_target_token.setText(bh_target_token.name.toUpperCase());
            }
            BHBalance inp_balance = BHBalanceHelper.getBHBalanceFromAccount(bh_coin_token.symbol);
            inp_amount.setText(NumberUtil.dispalyForUsertokenAmount4Level(inp_balance.amount));

            tv_token_name.setText(mTokenMapping.coin_symbol.toUpperCase());
            BHBalanceHelper.loadTokenIcon(this,iv_issue,mTokenMapping.coin_symbol.toUpperCase());
            BHBalanceHelper.loadTokenIcon(this,iv_target,mTokenMapping.target_symbol.toUpperCase());

            if(!ToolUtils.checkListIsEmpty(mMappingToknList)&& mMappingToknList.size()>1){
                iv_right_more.setVisibility(View.VISIBLE);
            }else{
                iv_right_more.setVisibility(View.GONE);
            }
            String tv_rate_str = "1 ".concat(mTokenMapping.coin_symbol.toUpperCase()).concat(" = 1 ").concat(mTokenMapping.target_symbol.toUpperCase());
            tv_rate.setText(tv_rate_str);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private ChooseTokenFragment.ChooseTokenListener chooseTokenListener = item -> {
        tv_target_token.setText(item.target_symbol.toUpperCase());
        mTokenMapping = item;
        BHBalanceHelper.loadTokenIcon(this,iv_target,mTokenMapping.target_symbol.toUpperCase());
    };
}