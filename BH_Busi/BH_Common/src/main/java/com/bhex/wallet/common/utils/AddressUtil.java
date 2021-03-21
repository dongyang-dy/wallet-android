package com.bhex.wallet.common.utils;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.web3j.utils.Numeric;

/**
 * @author gongdongyang
 * 地址校验
 */
public class AddressUtil {
    //btc地址校验
    public static boolean validBtcAddress(String input) {
        try{
            NetworkParameters networkParameters = MainNetParams.get();;
            Address address = Address.fromString(networkParameters, input);
            return address!=null?true:false;
        }catch (Exception e){
            e.printStackTrace();
            return  false;
        }

    }

    //eth地址校验
    public static boolean validEthAddress(String input) {
        if (!input.startsWith("0x")){
            return false;
        }
        String cleanHexInput = Numeric.cleanHexPrefix(input);
        try{
            Numeric.toBigIntNoPrefix(cleanHexInput);
        }catch(NumberFormatException e){
            return false;
        }
        return cleanHexInput.length() == 40;
    }

    //hbc地址校验
    public static boolean validHbcAddress(String input) {
        if (!input.startsWith("HBC")){
            return false;
        }
        return input.length()==34;
    }
}
