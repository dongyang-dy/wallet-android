package com.bhex.wallet.bh_main.my.presenter;

import android.text.TextUtils;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.bhex.lib.uikit.util.ColorUtil;
import com.bhex.network.mvx.base.BaseActivity;
import com.bhex.network.mvx.base.BasePresenter;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.utils.MD5;
import com.bhex.tools.utils.RegexUtil;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.manager.BHUserManager;
import com.google.android.material.button.MaterialButton;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/21
 * Time: 23:13
 */
public class MyPresenter extends BasePresenter {

    public MyPresenter(BaseActivity activity) {
        super(activity);
    }

    /**
     * 检查密码
     * @param mb
     * @param oldPwd
     * @param newPwd
     */
    public void checkPasswordIsInput(AppCompatButton mb, String oldPwd, String newPwd, String newConfrimPwd, AppCompatTextView... tv){
        boolean flag = true;
        if(TextUtils.isEmpty(oldPwd)){
            /*mb.setBackgroundColor(ColorUtil.getColor(getActivity(), R.color.btn_disable_color));
            mb.setEnabled(false);*/
            flag = false;
        }

        if(TextUtils.isEmpty(newPwd)){
            /*mb.setBackgroundColor(ColorUtil.getColor(getActivity(), R.color.btn_disable_color));
            mb.setEnabled(false);*/
            flag = false;
        }

        if(TextUtils.isEmpty(newPwd)){
            flag = false;
        }

        if(!TextUtils.isEmpty(newPwd)){
            if(!RegexUtil.checkContainNum(newPwd)){
                tv[0].setTextColor(ColorUtil.getColor(getActivity(),R.color.red));
                tv[3].setTextColor(ColorUtil.getColor(getActivity(),R.color.red));
                flag = false;
            }else {
                tv[3].setTextColor(ColorUtil.getColor(getActivity(),R.color.dark_blue));
            }

            if(!RegexUtil.checkContainUpper(newPwd)){
                tv[0].setTextColor(ColorUtil.getColor(getActivity(),R.color.red));
                tv[1].setTextColor(ColorUtil.getColor(getActivity(),R.color.red));
                flag = false;
            }else{
                tv[1].setTextColor(ColorUtil.getColor(getActivity(),R.color.dark_blue));
            }

            if(!RegexUtil.checkContainLower(newPwd)){
                tv[0].setTextColor(ColorUtil.getColor(getActivity(),R.color.red));
                tv[2].setTextColor(ColorUtil.getColor(getActivity(),R.color.red));
                flag = false;
            }else{
                tv[2].setTextColor(ColorUtil.getColor(getActivity(),R.color.dark_blue));
            }

            if(newPwd.length()<8){
                tv[0].setTextColor(ColorUtil.getColor(getActivity(),R.color.red));
                tv[4].setTextColor(ColorUtil.getColor(getActivity(),R.color.red));
                flag = false;
            }else{
                tv[4].setTextColor(ColorUtil.getColor(getActivity(),R.color.dark_blue));
            }
        }


        if(TextUtils.isEmpty(newConfrimPwd)) {
            flag = false;
        }

        if(flag){
            mb.setBackgroundColor(ColorUtil.getColor(getActivity(), R.color.blue));
            mb.setEnabled(true);
        }else{
            mb.setBackgroundColor(ColorUtil.getColor(getActivity(), R.color.btn_disable_color));
            mb.setEnabled(false);
        }

    }

    public boolean checkPasswordEqual(String oldPwd,String newPwd,String newConfrimPwd){
        boolean flag = true;
        BHWallet bhWallet = BHUserManager.getInstance().getCurrentBhWallet();
        if(!bhWallet.password.equals(MD5.md5(oldPwd))){
            ToastUtils.showToast(getActivity().getResources().getString(R.string.error_oldpassword));
            return false;
        }

        if(TextUtils.isEmpty(newPwd)){
            ToastUtils.showToast(getActivity().getResources().getString(R.string.please_input_newpassword));
            return false;
        }

        if(TextUtils.isEmpty(newConfrimPwd))

        if(!newPwd.equals(newConfrimPwd)){
            ToastUtils.showToast(getActivity().getResources().getString(R.string.tip_two_password_equal));
            return false;
        }

        return flag;
    }

    public BHWallet makeBhWallet(BHWallet wallet){
        BHWallet item = new BHWallet();
        item.id = wallet.id;
        item.name = wallet.name;
        item.address = wallet.address;
        item.password = wallet.password;
        item.mnemonic = wallet.mnemonic;
        item.isBackup = wallet.isBackup;
        item.isDefault = wallet.isDefault;
        item.privateKey = wallet.privateKey;
        item.way = wallet.way;
        return item;
    }
}
