package com.bhex.wallet.balance.ui.activity;

import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.tools.utils.RegexUtil;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.event.TransctionEvent;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.balance.helper.BHTokenHelper;
import com.bhex.wallet.balance.viewmodel.TransactionViewModel;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.SequenceManager;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.tx.BHRawTransaction;
import com.bhex.wallet.common.tx.TxReq;
import com.bhex.wallet.common.ui.fragment.Password30PFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * @author gongdongyang
 * 生成跨链地址
 * 2020-4-8 09:40:35
 */

@Route(path = ARouterConfig.Balance.Balance_cross_address)
public class GenerateAddressActivity extends BaseActivity
        implements Password30PFragment.PasswordClickListener{

    @Autowired(name = BHConstants.SYMBOL)
    String symbol;

    @Autowired(name = BHConstants.CHAIN)
    String mChain;

    BHBalance bhtBalance;
    BHToken symbolToken;

    BHWallet mCurrentWallet;
    TransactionViewModel transactionViewModel;

    private String gas_fee;

    private AppCompatTextView btn_crosslink_address;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_generate_address;
    }


    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        symbolToken = BHTokenHelper.getCrossBHToken(symbol,mChain);
        //
        bhtBalance = BHBalanceHelper.getBHBalanceFromAccount(BHConstants.BHT_TOKEN);
        mCurrentWallet = BHUserManager.getInstance().getCurrentBhWallet();
        initViewContent();
    }

    //初始化内容
    private void initViewContent() {
        btn_crosslink_address = findViewById(R.id.btn_crosslink_address);
        //标题
        AppCompatTextView tv_center_title = findViewById(R.id.tv_center_title);
        tv_center_title.setText(getResources().getString(R.string.create_crosslink_deposit_address));
        //提示内容
        AppCompatTextView tv_tip_content = findViewById(R.id.tv_tip_content);

        String v_tip_content = String.format(getString(R.string.tip_crosslink_generate),mChain.toUpperCase());

        tv_tip_content.setText(v_tip_content);

        //创建费用
        AppCompatTextView tv_create_fee = findViewById(R.id.tv_create_fee);
        String open_fee = (symbolToken!=null)?symbolToken.open_fee :"0";
        tv_create_fee.setText(open_fee+" "+BHConstants.BHT_TOKEN.toUpperCase());

        //手续费
        gas_fee = BHUserManager.getInstance().getDefaultGasFee().displayFee;
        AppCompatTextView tv_gas_fee = findViewById(R.id.tv_gas_fee);
        tv_gas_fee.setText(gas_fee+" "+BHConstants.BHT_TOKEN.toUpperCase());

        //按钮背景
        GradientDrawable btn_drawable =
                ShapeUtils.getRoundRectDrawable((int)getResources().getDimension(R.dimen.main_radius_conner),
                        ColorUtil.getColor(this,R.color.btn_blue_bg_color));
        btn_crosslink_address.setBackgroundDrawable(btn_drawable);
    }

    @Override
    protected void addEvent() {
        transactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);
        transactionViewModel.mutableLiveData.observe(this,ldm->{
            updateGenerateAddress(ldm);
        });
        btn_crosslink_address.setOnClickListener(this::onViewClicked);
    }


    public void onViewClicked(View view) {
        if(view.getId()==R.id.btn_crosslink_address){
            generateCrossLinkAddress();
        }
    }


    /**
     * 生成跨链地址
     */
    private void generateCrossLinkAddress() {

        if(bhtBalance==null || TextUtils.isEmpty(bhtBalance.amount)||Double.valueOf(bhtBalance.amount)<=0){
            ToastUtils.showToast(getResources().getString(R.string.fee_notenough));
            return;
        }

        /*if(TextUtils.isEmpty(gas_fee)||Double.valueOf(gas_fee)<=0){
            ToastUtils.showToast(getResources().getString(R.string.please_input_gas_fee));
            return;
        }*/

        /*if(!RegexUtil.checkNumeric(gas_fee)){
            ToastUtils.showToast(getResources().getString(R.string.error_input_gas_fes));
            return;
        }*/

        //总共手续费
        double d_total_fee = NumberUtil.add(gas_fee,symbolToken.open_fee);
        if(Double.valueOf(bhtBalance.amount)<d_total_fee){
            ToastUtils.showToast(getResources().getString(R.string.fee_notenough));
            return;
        }

        Password30PFragment.showPasswordDialog(getSupportFragmentManager(),
                Password30PFragment.class.getName(),
                this,0,true);
    }

    /**
     * 更新地址状态
     * @param ldm
     */
    private void updateGenerateAddress(LoadDataModel ldm) {
        if(ldm.loadingStatus== LoadingStatus.SUCCESS){
            ToastUtils.showToast(getResources().getString(R.string.link_outter_generating));
            EventBus.getDefault().post(new TransctionEvent());
            LogUtils.d("GenerateAddressActivity===>:","symbolToken.chain===="+symbolToken.chain);
            SequenceManager.getInstance().updateAddressStatus(symbolToken.chain);
            finish();
        }
    }

    //密码提示回调
    @Override
    public void confirmAction(String password, int position,int way) {
        List<TxReq.TxMsg> tx_msg_list = BHRawTransaction.createGenerateAddressMsg(mChain);
        transactionViewModel.transferInnerExt(this,password,gas_fee,tx_msg_list);
    }
}
