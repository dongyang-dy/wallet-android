package com.bhex.wallet.mnemonic;

import android.view.View;

import androidx.appcompat.widget.AppCompatButton;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bhex.network.mvx.base.BaseActivity;
import com.bhex.tools.utils.NavitateUtil;
import com.bhex.wallet.common.base.BaseCacheActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.mnemonic.helper.MnemonicDataHelper;
import com.bhex.wallet.mnemonic.ui.activity.ImportMnemonicActivity;
import com.bhex.wallet.mnemonic.ui.fragment.GlobalTipsFragment;
import com.bhex.wallet.mnemonic.utils.BHWalletUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

@Route(path= ARouterConfig.MNEMONIC_INDEX_PAGE)
public class MnemonicIndexActivity extends BaseCacheActivity {
    @BindView(R2.id.btn_generate_wallet)
    AppCompatButton btn_generate_wallet;

    @BindView(R2.id.btn_import_wallet)
    AppCompatButton btn_import_wallet;

    private List<String> mWords;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_nnemonic_index;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void addEvent() {
        mWords = MnemonicDataHelper.makeMnemonicString();
    }

    @OnClick({R2.id.btn_generate_wallet,R2.id.btn_import_wallet})
    public void onViewClicked(View view) {
        if(view.getId()== R.id.btn_generate_wallet){
            GlobalTipsFragment.showDialog(getSupportFragmentManager(),"");
        }else if(view.getId()== R.id.btn_import_wallet){
            //NavitateUtil.startActivity(this, ImportMnemonicActivity.class);
            BHWalletUtils.importMnemonic(BHWalletUtils.BH_CUSTOM_TYPE,mWords);

            /*byte []a1 = HexUtils.toBytes("D289605F9206C498D933E393B6D0ABF83BF3E350");
            //System.out.println("BHWallUtils=>"+"a1:"+ Arrays.toString(a1));
            String res = BHWalletUtils.base58Adress(a1);
            System.out.println("BHWallUtils=>"+"res:"+ res);*/
        }

    }
}
