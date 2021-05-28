package com.bhex.wallet.common.tx;

import com.bhex.network.utils.JsonUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.crypto.CryptoUtil;
import com.bhex.tools.crypto.Sha256;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.MD5;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.utils.BHKey;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;

import java.math.BigInteger;
import java.util.List;

/**
 * @author gongdongyang
 * 2020-11-4 23:45:36
 */
public class BHTransactionManager {
    private static String signBHRawTranscation(BHCredentials bhCredentials,String message){
        Sha256Hash input = Sha256Hash.wrap(Sha256.from(message.getBytes()).toString());
        ECKey.ECDSASignature signature =  bhCredentials.ecKey.sign(input);
        String sign = BHKey.signECDSA(signature);
        return sign;
    }

    public static BHSendTranscation createSendTranscation(String data,String sequence,String feeAmount, List<TxReq.TxMsg> msgs ){
        BigInteger double_feeAmount = NumberUtil.mulExt(feeAmount,String.valueOf(BHConstants.BHT_DECIMALS));
        BHRawTransaction bhRawTransactionExt = BHRawTransaction.createBaseTransaction(sequence,BHConstants.BH_MEMO,double_feeAmount);
        bhRawTransactionExt.msgs = msgs;
        //
        String pk = CryptoUtil.decryptPK(BHUserManager.getInstance().getCurrentBhWallet().privateKey, MD5.md5(data));
        LogUtils.d("BHTransactionManager==>:","pk=="+pk);
        BHCredentials bhCredentials = BHCredentials.createBHCredentials(pk);

        String raw_json = JsonUtils.toJson(bhRawTransactionExt);
        raw_json = JsonUtils.sortJson(raw_json);

        LogUtils.d("BHTransactionManager==>:","raw_json=="+raw_json);

        String sign = BHTransactionManager.signBHRawTranscation(bhCredentials,raw_json);
        //交易请求数据构建
        BHSendTranscation bhSendTranscation = BHSendTranscation.createBHSendTransaction2(bhRawTransactionExt,bhCredentials,sign, BHConstants.TRANSCTION_MODE);
        return  bhSendTranscation;
    }

    /**
     * 增加流动性
     * @return
     */
    public static BHSendTranscation create_dex_transcation(JsonArray jsonArray, String sequence, String data){
        String pk = CryptoUtil.decryptPK(BHUserManager.getInstance().getCurrentBhWallet().privateKey, MD5.md5(data));
        BHCredentials bhCredentials = BHCredentials.createBHCredentials(pk);
        BigInteger double_feeAmount = NumberUtil.mulExt(BHUserManager.getInstance().getDefaultGasFee().displayFee,
                String.valueOf(BHConstants.BHT_DECIMALS));

        BHRawTransaction bhRawTransaction = BHRawTransaction.createBHRaw_transcation(jsonArray,double_feeAmount,sequence);
        String raw_json = JsonUtils.toJson(bhRawTransaction);

        //json排序
        raw_json = JsonUtils.sortJson(raw_json);
        String sign = BHTransactionManager.signBHRawTranscation(bhCredentials,raw_json);
        //交易请求数据构建
        BHSendTranscation bhSendTranscation = BHSendTranscation.createBHSendTransaction2(bhRawTransaction,bhCredentials,sign,BHConstants.TRANSCTION_MODE);

        LogUtils.d("JsBowserFragment====>11===",JsonUtils.toJson(bhSendTranscation));
        return bhSendTranscation;
    }

}
