package com.bhex.wallet.common.manager;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;

import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.common.db.entity.BHWallet;

/**
 * 安全定时
 */
public class SecurityTimer {

    private static volatile SecurityTimer _instance = new SecurityTimer();

    public final long VAL_DURATION = 30*60*1000;

    public  boolean isStart = false;
    private HandlerThread mHandlerThread;
    private SecurityTimer(){

    }

    public static SecurityTimer getInstance(){
        return _instance;
    }

    public void start(){
        if(isStart){
           return;
        }
        mHandlerThread = new HandlerThread("WorkThread"){
            @Override
            protected void onLooperPrepared() {
                super.onLooperPrepared();
                CountDownTimer countDownTimer = new CountDownTimer(VAL_DURATION,1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        //LogUtils.d("SecurityTimer==>:","==millisUntilFinished=="+millisUntilFinished);
                    }

                    @Override
                    public void onFinish() {
                        isStart = false;
                        BHWallet bhWallet = BHUserManager.getInstance().getCurrentBhWallet();
                        bhWallet.pwd = "";
                        bhWallet.password = "";
                        bhWallet.mnemonic = "";
                        SecuritySettingManager.getInstance().thirty_in_time = false;
                    }
                };
                isStart = true;
                countDownTimer.start();
            }
        };
        mHandlerThread.start();

    }

    public void stop(){
        if(mHandlerThread!=null && mHandlerThread.getLooper()!=null){
            isStart = false;
            mHandlerThread.getLooper().quit();
            SecuritySettingManager.getInstance().thirty_in_time = false;
        }
    }
}
