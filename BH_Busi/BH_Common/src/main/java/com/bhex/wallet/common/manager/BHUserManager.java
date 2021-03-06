package com.bhex.wallet.common.manager;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.bhex.network.app.BaseApplication;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.crypto.CryptoUtil;
import com.bhex.tools.utils.FileUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.crypto.wallet.HWallet;
import com.bhex.wallet.common.crypto.wallet.HWalletFile;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.model.AccountInfo;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.model.CreateWalletParams;
import com.bhex.wallet.common.model.GasFee;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.utils.Numeric;

import java.util.Arrays;
import java.util.List;

import java8.util.stream.StreamSupport;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/12
 * Time: 23:56
 */
public class BHUserManager {

    private final static String TAG = BHUserManager.class.getSimpleName();

    private static volatile BHUserManager  _INSTANCE;

    //private BHWallet tmpBhWallet;

    //当前使用的钱包
    private BHWallet mCurrentBhWallet;

    //资产账户信息
    private AccountInfo mAccountInfo;

    private List<BHWallet> allWallet;

    public GasFee gasFee;

    //助记词列表
    private List<String> mWordList;

    private CreateWalletParams createWalletParams;

    private BHUserManager(){
        //tmpBhWallet = new BHWallet();
        createWalletParams = new CreateWalletParams();
        mCurrentBhWallet = new BHWallet();
        initWord();
    }

    public static BHUserManager getInstance(){
        if(_INSTANCE==null){
            synchronized (BHUserManager.class){
                if(_INSTANCE==null){
                    _INSTANCE = new BHUserManager();
                }
            }
        }
        return _INSTANCE;
    }

    private void initWord(){
        try{
            String res = FileUtils.loadStringByAssets(BaseApplication.getInstance(),"en-mnemonic-word-list.txt");
            if(!TextUtils.isEmpty(res)){
                mWordList = Arrays.asList(res.split(" "));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public CreateWalletParams getCreateWalletParams() {
        if(createWalletParams==null){
            createWalletParams = new CreateWalletParams();
        }
        return createWalletParams;
    }

    public synchronized void setAllWallet(List<BHWallet> allWallet) {
        this.allWallet = allWallet;
        if(ToolUtils.checkListIsEmpty(allWallet)){
            return;
        }

        for (BHWallet bhWallet:allWallet) {
            if(bhWallet.isDefault==BH_BUSI_TYPE.默认托管单元.getIntValue()){
                mCurrentBhWallet = bhWallet;
            }
        }

        if(mCurrentBhWallet==null || TextUtils.isEmpty(mCurrentBhWallet.address)){
            mCurrentBhWallet = allWallet.get(0);
        }
    }

    public synchronized List<BHWallet> getAllWallet() {
        return allWallet;
    }

    public synchronized boolean isHasWallet(){
        if(mCurrentBhWallet.id>0 || !ToolUtils.checkListIsEmpty(allWallet)){
            return true;
        }
        return false;
    }

    public synchronized BHWallet getCurrentBhWallet() {
        return mCurrentBhWallet;
    }

    public synchronized void setCurrentBhWallet(BHWallet mCurrentBhWallet) {
        this.mCurrentBhWallet = mCurrentBhWallet;
        if(allWallet==null || allWallet.size()<=0){
            return;
        }
        //设置默认
        StreamSupport.stream(allWallet).forEach( item->{
            if(item.id==mCurrentBhWallet.id){
                item.setIsDefault(BH_BUSI_TYPE.默认托管单元.getIntValue());
            }else{
                item.setIsDefault(BH_BUSI_TYPE.非默认托管单元.getIntValue());
            }
        });
    }

    public synchronized AccountInfo getAccountInfo() {
        return mAccountInfo;
    }

    public synchronized void setAccountInfo(AccountInfo accountInfo) {
        this.mAccountInfo = accountInfo;
        //更新状态
        //SequenceManager.getInstance().initSequence(accountInfo.sequence);
    }

    public List<String> getWordList() {
        return mWordList;
    }

    public synchronized String getUserBalanceList(){
        String key = BHUserManager.getInstance().mCurrentBhWallet.getAddress()+"_balance";
        String result = MMKVManager.getInstance().mmkv().decodeString(key, BHConstants.COIN_DEFAULT_LIST);
        return result;
    }

    public synchronized String getSymbolList(){
        StringBuffer sb = new StringBuffer("");
        ArrayMap<String,BHToken> map_tokens = SymbolCache.getInstance().getLocalToken();
        for(ArrayMap.Entry<String,BHToken> item:map_tokens.entrySet()){
            sb.append(item.getValue().symbol.toUpperCase()).append(",");
        }

        if(TextUtils.isEmpty(sb.toString())){
            sb.append(MMKVManager.getInstance().mmkv().decodeString(BHConstants.TOKEN_DEFAULT_LIST,sb.toString()));
        }
        return sb.toString();
    }

    public synchronized GasFee getDefaultGasFee(){
        if(gasFee!=null){
            return gasFee;
        }
        GasFee t_gasFee = new GasFee("10000000000000000","2000000");
        return  t_gasFee;
    }

    public void clear(){
        //MainActivityManager._instance.setTargetClass(null);
    }

    // 通过address 获取Wallet
    public   BHWallet getBHWalletByAddress(String address ){
        List<BHWallet> list = BHUserManager.getInstance().getAllWallet();
        for(BHWallet item:list){
            if(address.equals(item.address)){
                return item;
            }
        }
        return null;
    }

}
