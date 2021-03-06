package com.bhex.wallet.mnemonic.helper;

import android.text.TextUtils;

import com.bhex.tools.crypto.CryptoUtil;
import com.bhex.tools.crypto.HexUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.MD5;
import com.bhex.wallet.common.crypto.wallet.HWallet;
import com.bhex.wallet.common.crypto.wallet.HWalletFile;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.mnemonic.ui.item.MnemonicItem;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.web3j.protocol.ObjectMapperFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/6
 * Time: 20:43
 */
public class MnemonicDataHelper {
    public static List<MnemonicItem> makeMnemonic(String inputPassword,String wallet_address){
        List<MnemonicItem> list = new ArrayList<>();
        try{

            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
            BHWallet currentWallet = BHUserManager.getInstance().getBHWalletByAddress(wallet_address);
            //解密助记词
            String []array = null;
            HWalletFile old_walletFile = objectMapper.readValue(currentWallet.keystorePath, HWalletFile.class);
            if(!TextUtils.isEmpty(old_walletFile.encMnemonic)){
                String origin_enemonic = HWallet.解密_M(old_walletFile.encMnemonic,inputPassword,old_walletFile);
                array = origin_enemonic.split(" ");
            }

            for (int i = 0; i < array.length; i++) {
                MnemonicItem item = new MnemonicItem(array[i],(i+1),false);
                list.add(item);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return list;
    }

    /**
     * under-mnemonic-list
     * @param datas
     * @return
     */
    public static List<MnemonicItem> makeNewMnemonicList(List<MnemonicItem> datas ){
        List<MnemonicItem> list = new ArrayList<>();

        for (int i = 0; i <datas.size() ; i++) {
            MnemonicItem item = datas.get(i);
            MnemonicItem newItem = new MnemonicItem(item.getWord(),item.getIndex(),item.isSelected());
            list.add(newItem);
        }
        return list;
    }

    /**
     * 创建
     * @return
     */
    public static List<MnemonicItem> makeAboveMnemonicList( ){
        List<MnemonicItem> list = new ArrayList<>();

        for (int i = 0; i <12 ; i++) {
            MnemonicItem item = new MnemonicItem(i+1);
            list.add(item);
        }
        return list;
    }

    public static int getAboveFillIndex(List<MnemonicItem> datas){
        int index = 0;
        for (int j = 0; j < datas.size(); j++) {
            MnemonicItem item = datas.get(j);
            if(TextUtils.isEmpty(item.getWord())){
                index = j;
                break;
            }
        }
        return index;
    }

    public static int getUnderFillIndex(List<MnemonicItem> datas,MnemonicItem mnemonicItem){
        int index = -1;
        for (int i = 0; i < datas.size(); i++) {
            MnemonicItem temp = datas.get(i);
            if(temp.getWord().equals(mnemonicItem.getWord())){
                index = i;
                break;
            }
        }
        return index;
    }


}
