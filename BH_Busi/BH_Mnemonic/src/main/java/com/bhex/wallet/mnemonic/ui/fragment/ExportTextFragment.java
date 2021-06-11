package com.bhex.wallet.mnemonic.ui.fragment;


import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import com.bhex.network.app.BaseApplication;
import com.bhex.wallet.common.base.BaseFragment;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.MD5;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.helper.BHWalletHelper;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.mnemonic.R;
import com.bhex.wallet.mnemonic.R2;
import com.google.android.material.button.MaterialButton;

import butterknife.BindView;


/**
 * @author gongdongyang
 * 私钥或Keystore文本导出
 * 2020-5-17 00:08:19
 */
public class ExportTextFragment extends BaseFragment {

    public static final String KEY_FLAG = BHConstants.FLAG;

    @BindView(R2.id.tv_backup_tip_2)
    AppCompatTextView tv_backup_tip_2;

    @BindView(R2.id.tv_backup_tip_4)
    AppCompatTextView tv_backup_tip_4;

    @BindView(R2.id.et_private_key)
    AppCompatEditText et_private_key;

    @BindView(R2.id.btn_copy)
    AppCompatTextView btn_copy;

    BHWallet chooseWallet;

    private String flag;
    private String inptPwd;
    private String wallet_address;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_export_text;
    }

    @Override
    protected void initView() {
        et_private_key.setFocusable(false);
        et_private_key.setFocusableInTouchMode(false);

        flag = getArgumentValue(ExportTextFragment.KEY_FLAG);
        inptPwd = getArgumentValue(BHConstants.INPUT_PASSWORD);
        wallet_address = getArgumentValue(BHConstants.WALLET_ADDRESS);

        chooseWallet = BHUserManager.getInstance().getBHWalletByAddress(wallet_address);

        if(BH_BUSI_TYPE.备份私钥.value.equals(flag)){
            tv_backup_tip_2.setText(getString(R.string.backup_text_tip_2));
            tv_backup_tip_4.setText(getString(R.string.backup_text_tip_4));
            et_private_key.setText(BHWalletHelper.getOriginPK(chooseWallet.keystorePath,inptPwd));

        }else{
            tv_backup_tip_2.setText(getString(R.string.backup_text_tip_ks2));
            tv_backup_tip_4.setText(getString(R.string.backup_text_tip_ks4));
            et_private_key.setText(BHWalletHelper.getOriginKeyStore(chooseWallet.keystorePath));
            //btn_copy.setText(getString(R.string.copy_keystore));
        }

    }


    @Override
    protected void addEvent() {
        btn_copy.setOnClickListener(v -> {
            ToolUtils.copyText(et_private_key.getText().toString(),getContext());
            ToastUtils.showToast(getString(R.string.copyed));
        });
    }

    private String getArgumentValue(String key){
        String result = "";
        if(getArguments()!=null){
            result = getArguments().getString(key,"1");
        }
        return result;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ToolUtils.clearClipboard(BaseApplication.getInstance());
    }
}
