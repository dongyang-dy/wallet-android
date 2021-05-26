package com.bhex.wallet.common.manager;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.bhex.network.RxSchedulersHelper;
import com.bhex.network.cache.stategy.CacheStrategy;
import com.bhex.network.observer.BHBaseObserver;
import com.bhex.network.observer.SimpleObserver;
import com.bhex.network.utils.JsonUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.RegexUtil;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.common.api.BHttpApi;
import com.bhex.wallet.common.api.BHttpApiInterface;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.model.AccountInfo;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.tx.TransactionOrder;
import com.bhex.wallet.common.viewmodel.BalanceViewModel;
import com.google.gson.JsonObject;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Sequence 管理
 */
public class SequenceManager {
    private static SequenceManager _instance = new SequenceManager();

    //Sequence
    public final String SEQUENCE_KEY = "sequence_";
    //private AtomicInteger sequence = new AtomicInteger(0);

    //跨链地址生成
    private String GENARATOR_KEY = "GenaratorChain";
    private String GENARATOR_KEY_VALUE = "";

    private ArrayMap<String,PeddingTx> mPeddingTxMap;

    private CompositeDisposable compositeDisposable;


    private SequenceManager(){
        mPeddingTxMap = new ArrayMap<>();
        compositeDisposable = new CompositeDisposable();
    }

    public static SequenceManager getInstance(){
        return _instance;
    }

    public synchronized void initSequence(){
        //初始化Sequence
        /*String key = SEQUENCE_KEY.concat(BHUserManager.getInstance().getCurrentBhWallet().address);
        int v_sequence =  MMKVManager.getInstance().mmkv().decodeInt(key,0);
        sequence = new AtomicInteger(v_sequence);*/
        //跨链地址生成
        GENARATOR_KEY = GENARATOR_KEY.concat(BHUserManager.getInstance().getCurrentBhWallet().address);
        GENARATOR_KEY_VALUE= MMKVManager.getInstance().mmkv().decodeString(GENARATOR_KEY,GENARATOR_KEY_VALUE);
    }

    public void removeAddressStatus(AccountInfo accountInfo) {
        if(ToolUtils.checkListIsEmpty(accountInfo.assets)){
            GENARATOR_KEY_VALUE = "";
            return;
        }

        if(TextUtils.isEmpty(GENARATOR_KEY_VALUE)){
            return;
        }



        for(AccountInfo.AssetsBean assetsBean:accountInfo.assets){
            if(GENARATOR_KEY_VALUE.equalsIgnoreCase(assetsBean.chain)){
                //String k_genarator_key = GENARATOR_KEY.concat(BHUserManager.getInstance().getCurrentBhWallet().address);
                MMKVManager.getInstance().mmkv().remove(GENARATOR_KEY);
            }
        }
    }


    public void updateAddressStatus(String chain) {
        GENARATOR_KEY_VALUE = chain;
        MMKVManager.getInstance().mmkv().encode(GENARATOR_KEY,GENARATOR_KEY_VALUE);
    }

    public String getAddressStatus(){
        GENARATOR_KEY_VALUE = MMKVManager.getInstance().mmkv().decodeString(GENARATOR_KEY,"");
        return GENARATOR_KEY_VALUE;
    }

    public class PeddingTx{
        public String tx;
        public String type;
        public long time;
    }

    public class TranscationResponse{

        /**
         * height : 0
         * txhash : 4E7DC6ABC3323CAA54C1B1B30544633F5FA8C19F977C037594548145ED497256
         * raw_log : [{"msg_index":0,"success":true,"log":"","events":[{"type":"message","attributes":[{"key":"action","value":"send"}]}]}]
         * logs : [{"msg_index":0,"success":true,"log":"","events":[{"type":"message","attributes":[{"key":"action","value":"send"}]}]}]
         */

        public String height;
        public String txhash;
        public String raw_log;
        public List<LogsBean> logs;

        public  class LogsBean {
            /**
             * msg_index : 0
             * success : true
             * log :
             * events : [{"type":"message","attributes":[{"key":"action","value":"send"}]}]
             */

            public int msg_index;
            public boolean success;
            public String log;

        }
    }
}
