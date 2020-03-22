package com.bhex.wallet.mnemonic.helper;

import android.text.TextUtils;

import com.bhex.tools.crypto.CryptoUtil;
import com.bhex.tools.crypto.HexUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.mnemonic.ui.item.MnemonicItem;

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
    public static List<MnemonicItem> makeMnemonic(){
        List<MnemonicItem> list = new ArrayList<>();
        try{
            String encryptMnemonic = BHUserManager.getInstance().getTmpBhWallet().getMnemonic();
            String pwd = "";
            if(!TextUtils.isEmpty(encryptMnemonic)){
                encryptMnemonic = BHUserManager.getInstance().getTmpBhWallet().getMnemonic();
                pwd = BHUserManager.getInstance().getTmpBhWallet().getPassword();
            }else{
                encryptMnemonic = BHUserManager.getInstance().getCurrentBhWallet().getMnemonic();
                pwd = BHUserManager.getInstance().getCurrentBhWallet().getPassword();
            }
            byte [] bytes = CryptoUtil.decrypt(HexUtils.toBytes(encryptMnemonic),pwd);
            String  mnemonic  = new String(bytes);
            String []array = mnemonic.split(" ");

            for (int i = 0; i < array.length; i++) {
                MnemonicItem item = new MnemonicItem(array[i],(i+1),false);
                list.add(item);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return list;
    }

    public static List<String> makeMnemonicString(){
        /*String []array= new String[]{"deer", "bleak","bring", "biology", "pole",
                "energy", "galaxy", "situate", "upgrade", "south", "clarify", "lava"};*/
        //sniff float truck talent walk search mad boat away fossil sleep dune
        String []array= new String[]{"sniff", "float","truck", "talent", "walk",
                "search", "mad", "boat", "away", "fossil", "sleep", "dune"};
        List<String> list = Arrays.asList(array);

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
