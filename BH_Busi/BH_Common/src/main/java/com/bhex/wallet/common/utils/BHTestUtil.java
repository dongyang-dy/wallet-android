package com.bhex.wallet.common.utils;

import android.text.TextUtils;

import com.bhex.network.RxSchedulersHelper;
import com.bhex.network.app.BaseApplication;
import com.bhex.network.observer.BHProgressObserver;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.utils.FileUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;

public class BHTestUtil {

    //助记词到地址
    public static void 助记词到地址(BaseActivity activity){

        BHProgressObserver pbo = new BHProgressObserver<String>(activity) {
            @Override
            protected void onSuccess(String result) {
                super.onSuccess(result);
                ToastUtils.showToast(result);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
            }

        };

        Observable.create(emitter -> {
            String addressArray = FileUtils.loadStringByAssets$$(BaseApplication.getInstance(),"hbc_address.txt");
            int count = 0;
            if(TextUtils.isEmpty(addressArray)){
                return;
            }

            List<String> list  = Arrays.asList(addressArray.split("\\;"));
            if(ToolUtils.checkListIsEmpty(list)){
                return;
            }

            //int i = 5/0;

            for(int i=0;i<list.size();i++){
                String itemStr = list.get(i);
                //
                String [] itemArray = itemStr.split("\\|");
                //
                String[] mnemonic_text = itemArray[0].trim().split(" ");
                List<String> mnemonic = Arrays.asList(mnemonic_text);

                String address = itemArray[2].trim();

                BHWallet bhWallet = BHWalletUtils.importMnemonic("",mnemonic,"gongdongyang","123456");

                if(!address.equals(bhWallet.address)){
                    LogUtils.d("BHTestUtil===>:","第"+i+"地址不相同,原地址=="+address+"=生成地址="+bhWallet.address);
                }else{
                    count++;
                    LogUtils.d("BHTestUtil===>:","第"+i+"地址相同");
                }
            }

            if(count==list.size()){
                emitter.onNext("验证成功");
            }else{
                emitter.onNext("验证出错");
            }
            emitter.onComplete();


        }).compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(pbo);
        ;

    }

}
