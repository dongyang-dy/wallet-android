package com.bhex.wallet.balance.viewmodel;

import android.util.ArrayMap;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bhex.network.RxSchedulersHelper;
import com.bhex.network.app.BaseApplication;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.network.observer.BHBaseObserver;
import com.bhex.network.utils.JsonUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.helper.BHTokenHelper;
import com.bhex.wallet.common.api.BHttpApi;
import com.bhex.wallet.common.api.BHttpApiInterface;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.db.AppDataBase;
import com.bhex.wallet.common.db.dao.BHTokenDao;
import com.bhex.wallet.common.manager.MMKVManager;
import com.bhex.wallet.common.model.BHToken;
import com.google.gson.JsonArray;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.util.LinkedHashMap;
import java.util.List;

import io.reactivex.Observable;

/**
 * @author gongdongyang
 * 2020年10月31日11:18:48
 */
public class TokenViewModel extends ViewModel {

    private BHTokenDao bhTokenDao = AppDataBase.getInstance(BaseApplication.getInstance()).bhTokenDao();

    public MutableLiveData<LoadDataModel> searchLiveData  = new MutableLiveData<>();
    public MutableLiveData<LoadDataModel> queryLiveData  = new MutableLiveData<>();

    public TokenViewModel(){

    }

    public void search_token(BaseActivity activity,String search_key,String chain){
        BHBaseObserver<JsonArray> observer = new BHBaseObserver<JsonArray>() {
            @Override
            protected void onSuccess(JsonArray jsonArray) {
                if(jsonArray==null){
                    return;
                }
                List<BHToken> bhToken = JsonUtils.getListFromJson(jsonArray.toString(), BHToken.class);
                LoadDataModel ldm = new LoadDataModel(bhToken);
                searchLiveData.postValue(ldm);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel ldm = new LoadDataModel(LoadingStatus.ERROR,errorMsg);
                searchLiveData.postValue(ldm);
            }
        };
        BHttpApi.getService(BHttpApiInterface.class)
                .searchToken(chain,search_key)
                .compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(observer);
    }

    public  void addOrCancelToken(boolean checked, BHToken bhToken) {
        LinkedHashMap<String,BHToken> map_tokens = CacheCenter.getInstance().getSymbolCache().getLocalToken();
        //添加资产
        if(checked){
            CacheCenter.getInstance().getSymbolCache().addBHToken(bhToken);
            //判断币种是否在list中
            saveTokenToDb(bhToken);
            map_tokens.put(bhToken.symbol,bhToken);

        }
        //取消资产
        if(!checked){
            if(map_tokens.get(bhToken.symbol)!=null){
                map_tokens.remove(bhToken.symbol);
            }
        }

        //保存币种
        BaseApplication.getInstance().getExecutor().execute(()->{
            if(ToolUtils.checkMapEmpty(map_tokens)){
                MMKVManager.getInstance().mmkv().remove(BHConstants.SYMBOL_DEFAULT_KEY);
                return;
            }
            //保存于
            StringBuffer v_token = new StringBuffer("");
            for (ArrayMap.Entry<String,BHToken> item:map_tokens.entrySet()) {
                v_token.append(item.getValue().symbol).append("_");
            }

            MMKVManager.getInstance().mmkv().encode(BHConstants.SYMBOL_DEFAULT_KEY, v_token.toString());
            String remove_key = MMKVManager.getInstance().mmkv().decodeString(BHConstants.SYMBOL_REMOVE_KEY,"");
            if(checked){
                remove_key = remove_key.replace(bhToken.symbol+"_","");
                MMKVManager.getInstance().mmkv().encode(BHConstants.SYMBOL_REMOVE_KEY,remove_key);
            }else{
                if(!remove_key.contains(bhToken.symbol+"_")){
                    remove_key = remove_key.concat(bhToken.symbol+"_");
                    MMKVManager.getInstance().mmkv().encode(BHConstants.SYMBOL_REMOVE_KEY,remove_key);
                }
            }
        });

    }

    public void saveTokenToDb(BHToken bhToken){
        Observable.create(emitter -> {
            Long res = bhTokenDao.insertSingle(bhToken);
            emitter.onNext(res+"");
            emitter.onComplete();
        }).compose(RxSchedulersHelper.io_main())
          .subscribe(v->{

          });
    }

    //查询单个币种
    public void queryToken(FragmentActivity activity,String symbol){
        BHBaseObserver<BHToken> observer = new BHBaseObserver<BHToken>() {
            @Override
            protected void onSuccess(BHToken bhToken) {
                LoadDataModel ldm = new LoadDataModel(bhToken);
                queryLiveData.postValue(ldm);
                //保存Token到库中
                saveTokenToDb(bhToken);
                SymbolCache.getInstance().addBHToken(bhToken);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel ldm = new LoadDataModel(LoadingStatus.ERROR,errorMsg);
                queryLiveData.postValue(ldm);
            }
        };
        BHttpApi.getService(BHttpApiInterface.class)
                .queryToken(symbol)
                .compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(observer);
    }

    //查询所有官方列表
    public void loadVerifiedToken(FragmentActivity activity,String chain){
        BHBaseObserver<JsonArray> observer = new BHBaseObserver<JsonArray>() {
            @Override
            protected void onSuccess(JsonArray jsonArray) {
                if(jsonArray==null){
                    return;
                }
                LoadDataModel ldm = new LoadDataModel();
                List<BHToken> coinList = JsonUtils.getListFromJson(jsonArray.toString(), BHToken.class);
                if(!ToolUtils.checkListIsEmpty(coinList)){
                    SymbolCache.getInstance().putSymbolToMap(coinList,2);
                }

                List<BHToken> resList = BHTokenHelper.loadVerifiedToken(chain);
                //过滤所需要币对
                //RefStreams.of(coinList).filter(bhTokens -> bhTokens.iterator().next().chain==chain);
                ldm.setData(resList);
                queryLiveData.postValue(ldm);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                List<BHToken> list = BHTokenHelper.loadVerifiedToken(chain);
                if(ToolUtils.checkListIsEmpty(list)){
                    LoadDataModel ldm = new LoadDataModel(LoadingStatus.ERROR,errorMsg);
                    queryLiveData.postValue(ldm);
                }else{
                    LoadDataModel ldm = new LoadDataModel(list);
                    queryLiveData.postValue(ldm);
                }
            }

        };
        BHttpApi.getService(BHttpApiInterface.class)
                .loadVerifiedToken(chain)
                .compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(observer);
    }
}
