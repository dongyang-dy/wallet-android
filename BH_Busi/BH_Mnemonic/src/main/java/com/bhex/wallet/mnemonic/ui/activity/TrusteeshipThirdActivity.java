package com.bhex.wallet.mnemonic.ui.activity;

import android.text.SpannableString;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.keyborad.PasswordInputView;
import com.bhex.lib.uikit.widget.keyborad.PasswordKeyBoardView;
import com.bhex.network.base.LoadingStatus;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.NavigateUtil;
import com.bhex.tools.utils.StringUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.common.ActivityCache;
import com.bhex.wallet.common.base.BaseCacheActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.MAKE_WALLET_TYPE;
import com.bhex.wallet.common.event.AccountEvent;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.SecuritySettingManager;
import com.bhex.wallet.common.viewmodel.WalletViewModel;
import com.bhex.wallet.mnemonic.R;
import com.bhex.wallet.mnemonic.R2;
import com.bhex.wallet.mnemonic.persenter.TrusteeshipPresenter;
import com.bhex.wallet.mnemonic.ui.fragment.GlobalTipsFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author gongdongyang
 * 创建托管单元第三步
 * 2020-3-12 20:47:54
 */
@Route(path = ARouterConfig.TRUSTEESHIP_MNEMONIC_THIRD)
public class TrusteeshipThirdActivity extends BaseCacheActivity<TrusteeshipPresenter>
        implements GlobalTipsFragment.GlobalOnClickListenter {

    @BindView(R2.id.ck_agreement)
    AppCompatCheckBox ck_agreement;
    @BindView(R2.id.tv_agreement)
    AppCompatTextView tv_agreement;
    @BindView(R2.id.btn_create)
    AppCompatButton btn_create;
    @Autowired(name = BHConstants.PASSWORD)
    String mOldPwd;
    @Autowired(name = BHConstants.WAY)
    int mWay;

    PasswordInputView mPasswordInputView;
    PasswordKeyBoardView mPasswordKeyboardView;

    WalletViewModel walletViewModel;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_trusteeship_third;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        mPresenter.setToolBarTitle(mWay);
        mPresenter.setButtonTitle(mWay);
        SpannableString highlightText = StringUtils.highlight(this,
                getString(R.string.bh_register_agreement),
                getString(R.string.bh_register_agreement_sub),
                R.color.highlight_text_color,
                0, 0);
        tv_agreement.setText(highlightText);

        mPasswordInputView = findViewById(R.id.input_password_view);
        mPasswordKeyboardView = findViewById(R.id.my_keyboard);;

        TextView btn_finish = findViewById(R.id.keyboard_root).findViewById(R.id.btn_finish);
        btn_finish.setText(getString(R.string.close));
    }

    @Override
    protected void initPresenter() {
        mPresenter = new TrusteeshipPresenter(this);
    }

    @Override
    protected void addEvent() {
        walletViewModel = ViewModelProviders.of(this).get(WalletViewModel.class);

        mPasswordKeyboardView.setAttachToEditText(this,mPasswordInputView.m_input_content,mPasswordInputView,findViewById(R.id.keyboard_root));
        mPasswordKeyboardView.setOnKeyListener(new PasswordKeyBoardView.OnKeyListener() {
            @Override
            public void onInput(String text) {
                mPasswordInputView.onInputChange(mPasswordInputView.m_input_content.getEditableText());
            }

            @Override
            public void onDelete() {
                mPasswordInputView.onKeyDelete();
            }
        });

        mPasswordInputView.setOnInputListener(new PasswordInputView.OnInputListener() {
            @Override
            public void onComplete(String input) {
                //跳转密码确认
                /*BHUserManager.getInstance().getTmpBhWallet().setPassword(input);
                ARouter.getInstance().build(ARouterConfig.TRUSTEESHIP_MNEMONIC_THIRD)
                        .withString("password",input)
                        .navigation();*/
                if(!input.equals(mOldPwd)){
                    ToastUtils.showToast(getResources().getString(R.string.two_password_n_same));
                    return;
                }
                getPresenter().checkConfirmPassword(mPasswordInputView.getInputContent(),btn_create,mOldPwd,ck_agreement);
            }

            @Override
            public void onChange(String input) {

            }

            @Override
            public void onClear() {

            }
        });

        ck_agreement.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getPresenter().checkConfirmPassword(mPasswordInputView.getInputContent(),btn_create,mOldPwd,ck_agreement);
        });

        walletViewModel.mutableLiveData.observe(this,loadDataModel -> {
            if (loadDataModel.loadingStatus== LoadingStatus.SUCCESS) {
                if(mWay==MAKE_WALLET_TYPE.导入助记词.getWay()){
                    NavigateUtil.startMainActivity(this,new String[]{});
                    ActivityCache.getInstance().finishActivity();
                    EventBus.getDefault().post(new AccountEvent());
                    ToastUtils.showToast(getResources().getString(R.string.import_mnemonic_success));
                    BHUserManager.getInstance().clear();
                }else if(mWay==MAKE_WALLET_TYPE.PK.getWay()){
                    NavigateUtil.startMainActivity(this,new String[]{});
                    ActivityCache.getInstance().finishActivity();
                    EventBus.getDefault().post(new AccountEvent());
                    ToastUtils.showToast(getResources().getString(R.string.import_privatekey_success));
                    BHUserManager.getInstance().clear();
                }else{
                    ARouter.getInstance()
                            .build(ARouterConfig.TRUSTEESHIP_CREATE_OK_PAGE)
                            .withString(BHConstants.INPUT_PASSWORD,mPasswordInputView.getInputContent())
                            .navigation();
                    ActivityCache.getInstance().finishActivity();
                }
                SecuritySettingManager.getInstance().request_thirty_in_time(false,"");
            }else if(loadDataModel.loadingStatus== LoadingStatus.ERROR){
                if(loadDataModel.code==1){
                    ToastUtils.showToast(getResources().getString(R.string.trusteeship_exist));
                }else{
                    ToastUtils.showToast(getString(R.string.create_fail));
                }
            }
        });


    }


    @OnClick({R2.id.btn_create,R2.id.tv_agreement})
    public void onViewClicked(View view) {
        if(view.getId()==R.id.btn_create){
            String confirmPwd = mPasswordInputView.getInputContent();
            if(!mOldPwd.equals(confirmPwd)){
                ToolUtils.hintKeyBoard(this);
                ToastUtils.showToast(getResources().getString(R.string.two_password_n_same));
                return;
            }
            //设置密码
            String userName = BHUserManager.getInstance().getCreateWalletParams().name;
            //int way = BHUserManager.getInstance().getTmpBhWallet().way;
            if(mWay== MAKE_WALLET_TYPE.创建助记词.getWay()){
                generateMnemonic(userName,mOldPwd);
            }else if(mWay== MAKE_WALLET_TYPE.导入助记词.getWay()){
                importMnemoic(userName,mOldPwd);
            }else if(mWay==MAKE_WALLET_TYPE.PK.getWay()){
                importPrivatekey(userName,mOldPwd);
            }
        }else if(view.getId()==R.id.tv_agreement){
            GlobalTipsFragment.showDialog(getSupportFragmentManager(),"",
                    this,ck_agreement.isChecked());
        }
    }

    /**
     * 生成助记词
     */
    private void generateMnemonic(String name, String pwd) {
        walletViewModel.generateMnemonic(this,name, pwd);
    }

    /**
     * 导入助记词
     * @param name
     * @param pwd
     */
    private void importMnemoic(String name, String pwd) {
        //List<String> words = BHUserManager.getInstance().getTmpBhWallet().mWords;
        List<String> words = BHUserManager.getInstance().getCreateWalletParams().mWords;

        walletViewModel.importMnemonic(this,words,name,pwd);
    }

    /**
     * 导入私钥
     * @param name
     * @param pwd
     */
    private void importPrivatekey(String name,String pwd){
        walletViewModel.importPrivateKey(this,name,pwd);
    }

    @Override
    public void onCheckClickListener(View view, boolean isCheck) {
        ck_agreement.setChecked(isCheck);
    }
}
