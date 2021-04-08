package com.bhex.wallet.mnemonic.ui.activity;

import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.InputView;
import com.bhex.lib.uikit.widget.editor.SimpleTextWatcher;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.NavigateUtil;
import com.bhex.wallet.common.base.BaseCacheActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.viewmodel.WalletViewModel;
import com.bhex.wallet.mnemonic.R;
import com.bhex.wallet.mnemonic.R2;
import com.bhex.wallet.mnemonic.persenter.TrusteeshipPresenter;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * gdy
 * 2020-3-4 18:15:01
 * 创建托管单元`
 */
@Route(path = ARouterConfig.TRUSTEESHIP_MNEMONIC_FRIST)
public class TrusteeshipActivity extends BaseCacheActivity<TrusteeshipPresenter> {

    WalletViewModel walletViewModel;

    @Autowired(name="isForgetPwd")
    int isForgetPwd = 0;

    @Autowired(name = BHConstants.WAY)
    int mWay;

    @BindView(R2.id.btn_next)
    AppCompatButton btn_next;

    @BindView(R2.id.inp_wallet_name)
    AppCompatEditText inp_wallet_name;

    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;

    @BindView(R2.id.tv_wallet_name_count)
    AppCompatTextView tv_wallet_name_count;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_trusteeship;
    }

    @Override
    protected void initPresenter() {
        mPresenter = new TrusteeshipPresenter(this);
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        mPresenter.setToolBarTitle(mWay);
        inp_wallet_name.setInputType(InputType.TYPE_CLASS_TEXT);
    }

    @Override
    protected void addEvent() {
        walletViewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
        inp_wallet_name.addTextChangedListener(new SimpleTextWatcher(){
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                if(!TextUtils.isEmpty(inp_wallet_name.getText().toString().trim())){
                    btn_next.setBackgroundResource(R.drawable.btn_bg_blue_6_corner);
                    btn_next.setEnabled(true);
                }else{
                    btn_next.setBackgroundResource(R.drawable.btn_disabled_gray);
                    btn_next.setEnabled(false);
                }
                int count = inp_wallet_name.getText().toString().trim().length();
                tv_wallet_name_count.setText(String.format(getString(R.string.pwd_index), count));

            }
        });

    }


    @OnClick({R2.id.btn_next})
    public void onViewClicked(View view) {
        if(view.getId()==R.id.btn_next){
            //设置钱包用户名
            BHUserManager.getInstance().getCreateWalletParams().name = inp_wallet_name.getText().toString().trim();
            ARouter.getInstance()
                    .build(ARouterConfig.TRUSTEESHIP_MNEMONIC_SECOND)
                    .withInt("way",mWay)
                    .navigation();
        }
    }

}
