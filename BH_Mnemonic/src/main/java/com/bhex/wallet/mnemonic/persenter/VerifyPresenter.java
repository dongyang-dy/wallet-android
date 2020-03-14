package com.bhex.wallet.mnemonic.persenter;

import androidx.appcompat.widget.AppCompatButton;

import com.bhex.network.mvx.base.BaseActivity;
import com.bhex.network.mvx.base.BasePresenter;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.NavitateUtil;
import com.bhex.tools.utils.RegexUtils;
import com.bhex.wallet.mnemonic.R;
import com.bhex.wallet.mnemonic.ui.item.MnemonicItem;

import java.util.List;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/11
 * Time: 15:39
 */
public class VerifyPresenter extends BasePresenter {

    public VerifyPresenter(BaseActivity activity) {
        super(activity);
    }

    /**
     * 验证助记词
     * @param aboveList
     * @param orginList
     * @return
     */
    public boolean  verifyMnmonic(List<MnemonicItem> aboveList, List<MnemonicItem> orginList, AppCompatButton btn){
        boolean flag = true;
        for (int i = 0; i < orginList.size(); i++) {
            MnemonicItem aboveItem = aboveList.get(i);
            MnemonicItem orginItem = orginList.get(i);
            LogUtils.d("VerifyPresenter===",orginItem.getWord()+"==word=="+aboveItem.getWord());
            if(!orginItem.getWord().equals(aboveItem.getWord())){
                flag = false;
                break ;
            }

        }

        LogUtils.d("VerifyPresenter===","flag=="+flag);

        if (!flag) {
            btn.setBackgroundResource(R.drawable.btn_gray_e7ecf4);
            btn.setEnabled(false);
        }else{
            btn.setBackgroundResource(R.drawable.btn_bg_blue_6_corner);
            btn.setEnabled(true);
        }
        return flag;
    }
}
