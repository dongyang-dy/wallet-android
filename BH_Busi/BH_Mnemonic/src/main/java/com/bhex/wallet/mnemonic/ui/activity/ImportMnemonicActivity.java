package com.bhex.wallet.mnemonic.ui.activity;

import android.text.Editable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.MnemonicInputView;
import com.bhex.lib.uikit.widget.editor.SimpleTextWatcher;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.RegexUtil;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.common.base.BaseCacheActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.enums.MAKE_WALLET_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.utils.BHWalletUtils;
import com.bhex.wallet.mnemonic.R;
import com.bhex.wallet.mnemonic.R2;

import org.web3j.crypto.MnemonicUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 导入助记词
 *
 * @author gdy
 * 2020-3-11 18:37:15
 */
@Route(path = ARouterConfig.TRUSTEESHIP_IMPORT_MNEMONIC,name="导入助记词")
public class ImportMnemonicActivity extends BaseCacheActivity implements MnemonicInputView.MnemonicInputViewChangeListener {

    @BindView(R2.id.btn_next)
    AppCompatTextView btn_next;

    @BindView(R2.id.et_mnemonic)
    AppCompatEditText et_mnemonic;

    @BindView(R2.id.input_mnemonic)
    MnemonicInputView input_mnemonic;


    private List<String> mnemonicItems = new ArrayList<>();

    List<String> mOriginWords = BHUserManager.getInstance().getWordList();

    //private List<String> mMnemonicWords = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_import_mnemonic;
    }

    @Override
    protected void initView() {
        input_mnemonic.setWordList(mOriginWords);
        input_mnemonic.setInputViewChangeListener(this);
        et_mnemonic.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        et_mnemonic.setGravity(Gravity.TOP);
        et_mnemonic.setSingleLine(false);
        et_mnemonic.setHorizontallyScrolling(false);
        et_mnemonic.addTextChangedListener(new SimpleTextWatcher(){
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                if(checkIsEmpty()){
                    btn_next.setBackgroundResource(R.drawable.btn_gray_corner);
                    btn_next.setEnabled(false);
                }else{
                    btn_next.setBackgroundResource(R.drawable.btn_bg_blue_6_corner);
                    btn_next.setEnabled(true);
                }
            }
        });

        input_mnemonic.addEditText(null);
    }

    @Override
    protected void addEvent() {

    }

    @OnClick({R2.id.btn_next})
    public void onViewClicked(View view) {
        if(view.getId()==R.id.btn_next){
            importMnemoic(et_mnemonic.getText().toString().trim());
        }
    }

    private boolean checkIsEmpty(){
        String mnemoicStr = et_mnemonic.getText().toString().trim();

        boolean flag = RegexUtil.checkIsLetter(mnemoicStr);

        String []array = mnemoicStr.split("\\s+");

        if(!flag || array.length < 12){
            return true;
        }
        return false;
    }
    /**
     * 导入助记词
     */
    private void importMnemoic(String mnemoicStr) {
        boolean flag = true;

        mnemonicItems = input_mnemonic.getAllMnemonic();

        for (int i = 0; i < mnemonicItems.size(); i++) {
            String tmp = mnemonicItems.get(i);
            if(!mOriginWords.contains(tmp)){
                flag = false;
                break;
            }
        }

        if(!flag){
            ToastUtils.showToast(getResources().getString(R.string.error_mnemonic_form));
            ToolUtils.hintKeyBoard(this);
            return;
        }
        //String mnemonic_text = et_mnemonic.getText().toString().replaceAll("\\s+"," ");
        String mnemonic_text = BHWalletUtils.convertMnemonicList(mnemonicItems);
        //LogUtils.d("ImportMnemonicActivity==>:","mnemonic_text=="+mnemonic_text);
        //助记词校验位验证
        boolean v_flag = MnemonicUtils.validateMnemonic(mnemonic_text);
        if(!v_flag){
            ToastUtils.showToast(getString(R.string.mnemonic_checksum_error));
            ToolUtils.hintKeyBoard(this);
            return;
        }

        BHUserManager.getInstance().getCreateWalletParams().mWords = mnemonicItems;
        ARouter.getInstance().build(ARouterConfig.TRUSTEESHIP_MNEMONIC_FRIST)
                .withInt("way", MAKE_WALLET_TYPE.导入助记词.getWay())
                .navigation();
    }

    @Override
    public void inputViewChange() {
        List<String> words = input_mnemonic.getAllMnemonic();
        if(words==null && words.size()==0){
            return;
        }
        boolean flag = true;
        for (int i = 0; i < words.size(); i++) {
            String tmp = words.get(i);
            if(!mOriginWords.contains(tmp)){
                flag = false;
                break;
            }
        }

        if(flag){
            flag = words.size()!=12?false:true;
        }

        if(flag){
            btn_next.setBackgroundResource(R.drawable.btn_bg_blue_6_corner);
            btn_next.setEnabled(true);
        }else{
            btn_next.setBackgroundResource(R.drawable.btn_gray_corner);
            btn_next.setEnabled(false);
        }

        input_mnemonic.setInputViewStatus();
    }
}
