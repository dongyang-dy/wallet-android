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
        String v_genarator_key = GENARATOR_KEY.concat(BHUserManager.getInstance().getCurrentBhWallet().address);
        GENARATOR_KEY_VALUE= MMKVManager.getInstance().mmkv().decodeString(v_genarator_key,GENARATOR_KEY_VALUE);
    }

    //Sequence 自增
    /*public synchronized void increaseSequence(){
        int i_sequence = sequence.incrementAndGet();
        //LogUtils.d("SequenceManager===>:","increaseSequence=="+i_sequence);
        String key = SEQUENCE_KEY.concat(BHUserManager.getInstance().getCurrentBhWallet().address);
        MMKVManager.getInstance().mmkv().encode(key,i_sequence);
    }*/

    /*public synchronized String getSequence(String v_sequence){
        //String key = SEQUENCE_KEY.concat(BHUserManager.getInstance().getCurrentBhWallet().address);
        //int vv_sequence =  MMKVManager.getInstance().mmkv().decodeInt(key,0);
        //LogUtils.d("SequenceManager===>:","vv_sequence=="+vv_sequence);
        if(!TextUtils.isEmpty(v_sequence) && "0".equals(v_sequence)){
            sequence.set(0);
        }else{
            int i_sequence = Math.max(Integer.valueOf(v_sequence),sequence.get());
            sequence.set(i_sequence);
        }
        return sequence.get()+"";
    }

    public synchronized String getSequence(){
        return sequence.get()+"";
    }*/

    //sequence
    /*public synchronized void resetSequence(String v_sequence){
        if(TextUtils.isEmpty(v_sequence) || !TextUtils.isDigitsOnly(v_sequence)){
            return;
        }
        LogUtils.d("SequenceManager==>:","==resetSequence==");
        if("0".equals(v_sequence)){
            String key = SEQUENCE_KEY.concat(BHUserManager.getInstance().getCurrentBhWallet().address);
            MMKVManager.getInstance().mmkv().encode(key,v_sequence);
        }
    }*/


    //添加
    /*public synchronized void putPeddingTranscation(JsonObject jsonObject) {
        //
        TranscationResponse transcationResponse = JsonUtils.fromJson(jsonObject.toString(),TranscationResponse.class);
        if(!ToolUtils.checkListIsEmpty(transcationResponse.logs) && transcationResponse.logs.get(0).success){
            PeddingTx peddingTx = new PeddingTx();
            peddingTx.tx = transcationResponse.txhash;
            peddingTx.time = System.currentTimeMillis();
            mPeddingTxMap.put(transcationResponse.txhash,peddingTx);
            increaseSequence();
        }

    }

    //
    public ArrayMap<String, PeddingTx> getPeddingTxMap() {
        return mPeddingTxMap;
    }

    //更新未打包交易状态
    public void timerTranscation(BaseActivity activity){
        Observable.interval(1000,3000L, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Long>(){

                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                        if(compositeDisposable==null){
                            compositeDisposable = new CompositeDisposable();
                        }
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        super.onNext(aLong);
                        queryTransactionDetailExt(activity);
                    }
                });
    }

    public void stopTranscation(BaseActivity activity){
        if(compositeDisposable!=null && !compositeDisposable.isDisposed()){
            compositeDisposable.dispose();
            compositeDisposable = null;
        }
    }

    public void queryTransactionDetailExt(BaseActivity activity){
        if(ToolUtils.checkMapEmpty(SequenceManager.getInstance().getPeddingTxMap())){
            return;
        }

        //如果5秒自动移除
        long now = System.currentTimeMillis();
        Iterator<String> itor = mPeddingTxMap.keySet().iterator();
        while (itor.hasNext()) {
            String key = itor.next();
            PeddingTx peddingTx = mPeddingTxMap.get(key);
            if(peddingTx.time+5000<now){
                mPeddingTxMap.remove(key);
            }
        }

        if(ToolUtils.checkMapEmpty(SequenceManager.getInstance().getPeddingTxMap())){
            return;
        }

        String txhash = SequenceManager.getInstance().getPeddingTxMap().keyAt(0);
        queryTransactionDetail(activity,txhash);
    }

    //请求交易
    public void queryTransactionDetail(BaseActivity activity, String hash){
        //LogUtils.d("TransactionViewModel===>","==timer=="+hash);
        BHBaseObserver<JsonObject> observer = new BHBaseObserver<JsonObject>(false) {
            @Override
            protected void onSuccess(JsonObject jsonObject) {
                TransactionOrder transactionOrder = JsonUtils.fromJson(jsonObject.toString(), TransactionOrder.class);
                if(transactionOrder!=null && !TextUtils.isEmpty(transactionOrder.hash)){
                    SequenceManager.getInstance().getPeddingTxMap().remove(transactionOrder.hash);
                    //LogUtils.d("TransactionViewModel===>","==remove=="+hash+"==size=="+SequenceManager.getInstance().getPeddingTxMap().size());
                }
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                //LoadDataModel ldm = new LoadDataModel(code,"");
                //transLiveData.postValue(ldm);
            }
        };
        BHttpApi.getService(BHttpApiInterface.class)
                .queryTranscationView(hash)
                .compose(RxSchedulersHelper.io_main())
                //.as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(observer);
    }*/

    /*public void clear() {
        sequence = new AtomicInteger(0);
    }*/

    public void removeAddressStatus(AccountInfo accountInfo) {
        if(ToolUtils.checkListIsEmpty(accountInfo.assets)){
            GENARATOR_KEY_VALUE = "";
            return;
        }

        if(TextUtils.isEmpty(GENARATOR_KEY_VALUE)){
            return;
        }

        for(AccountInfo.AssetsBean assetsBean:accountInfo.assets){
            BHToken bhToken = SymbolCache.getInstance().getBHToken(assetsBean.symbol);
            if(bhToken!=null && !TextUtils.isEmpty(assetsBean.external_address)
                    && GENARATOR_KEY_VALUE.endsWith(bhToken.chain)){
                GENARATOR_KEY_VALUE="";
                //MMKVManager.getInstance().mmkv().encode(GENARATOR_KEY,GENARATOR_KEY_VALUE);
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
