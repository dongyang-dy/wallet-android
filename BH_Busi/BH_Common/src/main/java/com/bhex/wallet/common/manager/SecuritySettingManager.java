package com.bhex.wallet.common.manager;

import android.text.TextUtils;

import com.bhex.wallet.common.db.entity.BHWallet;

/**
 * @author gongdongyang
 * 2020-10-16 01:22:43
 */
public class SecuritySettingManager {

    private static volatile SecuritySettingManager _instance = new SecuritySettingManager();

    public static final String every_time_pwd_label = "every_time_pwd";
    public static final String thirty_in_time_label = "thirty_in_time";

    //public boolean every_time_pwd = false;
    public boolean thirty_in_time = false;

    private SecuritySettingManager(){

    }

    public static SecuritySettingManager getInstance(){
        return _instance;
    }

    public void initSecuritySetting(){
        /*BHWallet bhWallet = BHUserManager.getInstance().getCurrentBhWallet();
        every_time_pwd = MMKVManager.getInstance().mmkv().decodeBool(every_time_pwd_label,
                every_time_pwd);
        thirty_in_time =  MMKVManager.getInstance().mmkv().decodeBool(thirty_in_time_label,
                thirty_in_time);*/
    }

    /*public void save_every_time_pwd(boolean checked,String pwd){
        BHWallet bhWallet = BHUserManager.getInstance().getCurrentBhWallet();
        String key = every_time_pwd_label;
        SecuritySettingManager.getInstance().every_time_pwd = checked;
        bhWallet.pwd = pwd;
        MMKVManager.getInstance().mmkv().encode(key,checked);
    }*/

    public void request_thirty_in_time(boolean checked,String pwd) {
        BHWallet bhWallet = BHUserManager.getInstance().getCurrentBhWallet();
        //String key = thirty_in_time_label;
        SecuritySettingManager.getInstance().thirty_in_time = checked;

        if(!TextUtils.isEmpty(pwd)){
            bhWallet.pwd = pwd;
            SecurityTimer.getInstance().start();
        }else{
            bhWallet.pwd = pwd;
            bhWallet.password = "";
            bhWallet.mnemonic = "";
            SecurityTimer.getInstance().stop();
        }
        //MMKVManager.getInstance().mmkv().encode(key,checked);
    }

    public void recordPwd(String inputPwd) {
        if(thirty_in_time){
            BHWallet currentBHWallet = BHUserManager.getInstance().getCurrentBhWallet();
            currentBHWallet.pwd = inputPwd;
        }
        if(thirty_in_time){
            //开启定时任务
            SecurityTimer.getInstance().start();
        }
    }

    public boolean notNeedPwd(){
        BHWallet currentBHWallet = BHUserManager.getInstance().getCurrentBhWallet();
        return (thirty_in_time)&& !TextUtils.isEmpty(currentBHWallet.pwd);
    }


}
