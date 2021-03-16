package com.bhex.wallet.common.utils;

import android.text.TextUtils;

import com.bhex.network.utils.JsonUtils;
import com.bhex.tools.crypto.CryptoUtil;

import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.MD5;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.common.crypto.wallet.HWallet;
import com.bhex.wallet.common.crypto.wallet.HWalletFile;
import com.bhex.wallet.common.crypto.wallet.SecureRandomUtils;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.enums.MAKE_WALLET_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.wallet.DeterministicSeed;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.List;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/4
 * Time: 22:13
 */
public class BHWalletUtils {

    private static String TAG = BHWalletUtils.class.getSimpleName();

    private static ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    public static final SecureRandom secureRandom = SecureRandomUtils.secureRandom();

    //private Credentials credentials;
    //private static final ObjectMapper objectMapper = new ObjectMapper();
    /*static {
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }*/
    /**
     * 通用的以太坊基于bip44协议的助记词路径 （
     */
    public static String BH_CUSTOM_TYPE = "m/44'/496'/0'/0/0";

    /**
     * 创建助记词，并通过助记词创建钱包
     * @param walletName
     * @param pwd
     * @return
     */
    public static BHWallet generateMnemonic(String walletName, String pwd) {
        String passphrase = "";
        DeterministicSeed ds = new DeterministicSeed(secureRandom, 128, passphrase);
        //种子
        byte[] seeds = ds.getSeedBytes();
        //助记词
        List<String> mnemonic = ds.getMnemonicCode();
        ECKeyPair keyPair = makeECKeyPair(seeds);
        BHWallet bhWallet = generateWallet(walletName, pwd, keyPair,mnemonic);

        return bhWallet;
    }

    //助记词导入
    public static BHWallet importMnemonic(String path, List<String> list,String walletName,String pwd) {
        //String[] pathArray = path.split("/");
        String passphrase = "";
        long creationTimeSeconds = System.currentTimeMillis() / 1000;

        DeterministicSeed ds = new DeterministicSeed(list, null, passphrase, creationTimeSeconds);
        //种子
        byte[] seedBytes = ds.getSeedBytes();
        //助记词
        List<String> mnemonic = ds.getMnemonicCode();
        //生成密钥对
        ECKeyPair keyPair = makeECKeyPair(seedBytes);
        BHWallet bhWallet = generateWallet(walletName, pwd, keyPair,mnemonic);
        bhWallet.setWay(MAKE_WALLET_TYPE.导入助记词.getWay());
        if (bhWallet != null) {
            String old_mnemonic =  convertMnemonicList(mnemonic);
            String encrypt_mnemonic = CryptoUtil.encryptMnemonic(old_mnemonic,pwd);
            bhWallet.setMnemonic(encrypt_mnemonic);
        }

        return bhWallet;
    }

    /**
     * 导入私钥 私钥16进制
     * @param privateKey
     */
    public static BHWallet importPrivateKey(String privateKey,String walletName,String pwd){
        ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));
        BHWallet bhWallet = generateWallet(walletName, pwd, keyPair,null);
        bhWallet.setWay(MAKE_WALLET_TYPE.PK.getWay());
        return bhWallet;
    }


    public static BHWallet importKeyStoreExt(Credentials credentials,String name,String pwd){
        BHWallet bhWallet = null;
        try{

            //String pk = credentials.getEcKeyPair().getPrivateKey().toString(16);
            //走导入私钥的补助
            if(credentials!=null){
                bhWallet = generateWallet(name, pwd, credentials.getEcKeyPair(),null);
                bhWallet.setWay(MAKE_WALLET_TYPE.导入KS.getWay());
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return  bhWallet;
    }


    /**
     * 从私钥到地址
     * @param privateKey
     * @return
     */
    public static String privatekeyToAddress(String privateKey){
        ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));

        String bh_adress = BHKey.getBhexUserDpAddress(keyPair);
        return bh_adress;
    }

    /**
     * 从KeyStore到地址
     * @param keyStore
     * @param pwd
     * @return
     */
    public static String keyStoreToAddress(String keyStore,String pwd) throws CipherException,IOException{
        String bh_address = null;

        HWalletFile walletFile = objectMapper.readValue(keyStore, HWalletFile.class);

        Credentials credentials = Credentials.create(HWallet.decrypt(pwd, walletFile));
        if(credentials!=null){
            bh_address = BHKey.getBhexUserDpAddress(credentials.getEcKeyPair());
        }else{
            return null;
        }
        return  bh_address;
    }

    /**
     * 验证Keystore
     * @param keyStore
     * @param pwd
     * @return
     */
    public static Credentials verifyKeystore(String keyStore,String pwd) throws CipherException,IOException{
        //String bh_address = null;

        HWalletFile walletFile = objectMapper.readValue(keyStore, HWalletFile.class);

        Credentials credentials = Credentials.create(HWallet.decrypt(pwd, walletFile));

        if(credentials!=null){

            return credentials;
        }else{
            return null;
        }
    }

    /**
     * 助记词到公钥
     * @return
     */
    public static String memonicToAddress( List<String> list){
        String passphrase = "";
        long creationTimeSeconds = System.currentTimeMillis() / 1000;

        DeterministicSeed ds = new DeterministicSeed(list, null, passphrase, creationTimeSeconds);
        //种子
        byte[] seedBytes = ds.getSeedBytes();
        //助记词
        List<String> mnemonic = ds.getMnemonicCode();
        //生成密钥对
        ECKeyPair keyPair = makeECKeyPair(seedBytes);

        String bh_adress = BHKey.getBhexUserDpAddress(keyPair);

        return bh_adress;
    }

    /**
     * @param walletName
     * @param pwd
     * @param keyPair
     * @return
     */
    private static BHWallet generateWallet(String walletName, String pwd, ECKeyPair keyPair,List<String> mnemonics) {
        BHWallet bhWallet = new BHWallet();
        try {
            HWalletFile walletFile = HWallet.create(pwd, keyPair,1024, 1);
            //生成BH-地址
            //String v_pub_hex = ECKey.fromPrivate(keyPair.getPrivateKey()).getPublicKeyAsHex();
            //LogUtils.d("BHWalletUtils===>:","v_pub_hex=="+v_pub_hex);
            String bh_adress = BHKey.getBhexUserDpAddress(keyPair);
            walletFile.setAddress(bh_adress);
            //生成bench32地址
            String bh_bech_pubkey = BHKey.getBhexUserDpPubKey(keyPair);
            //keystore存储
            //加密助记词
            if(!ToolUtils.checkListIsEmpty(mnemonics)){
                String encMnemonic = HWallet.加密_M(convertMnemonicList(mnemonics),pwd,walletFile);
                walletFile.encMnemonic = encMnemonic;
            }

            String raw_json = JsonUtils.toJson(walletFile);
            //私钥加密
            String encryptPK = CryptoUtil.encryptPK(keyPair.getPrivateKey(),pwd);
            bhWallet.setName(walletName);
            bhWallet.setAddress(bh_adress);
            bhWallet.setPublicKey(bh_bech_pubkey);
            bhWallet.setPrivateKey(encryptPK);
            bhWallet.setKeystorePath(raw_json);
            bhWallet.setPassword(MD5.generate(pwd));
            bhWallet.setIsDefault(BH_BUSI_TYPE.非默认托管单元.getIntValue());
            bhWallet.setIsBackup(BH_BUSI_TYPE.未备份.getIntValue());
            if(mnemonics!=null && mnemonics.size()==12){
                String old_mnemonic =  convertMnemonicList(mnemonics);
                String encrypt_mnemonic = CryptoUtil.encryptMnemonic(old_mnemonic,pwd);
                bhWallet.setMnemonic(encrypt_mnemonic);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bhWallet;
    }

   /**
     * 生成公私钥对
     * @return
     */
    public static ECKeyPair makeECKeyPair(byte[] seedBytes){
        String[] pathArray = BH_CUSTOM_TYPE.split("/");
        DeterministicKey dkKey = HDKeyDerivation.createMasterPrivateKey(seedBytes);
        for (int i = 1; i < pathArray.length; i++) {
            ChildNumber childNumber;
            if (pathArray[i].endsWith("'")) {
                int number = Integer.parseInt(pathArray[i].substring(0,
                        pathArray[i].length() - 1));
                childNumber = new ChildNumber(number, true);
            } else {
                int number = Integer.parseInt(pathArray[i]);
                childNumber = new ChildNumber(number, false);
            }
            dkKey = HDKeyDerivation.deriveChildKey(dkKey, childNumber);
        }

        ECKeyPair keyPair = ECKeyPair.create(dkKey.getPrivKeyBytes() );

        return keyPair;
    }

    /**
     * 助记词 List 转 String
     * @param mnemonics
     * @return
     */
    public static String convertMnemonicList(List<String> mnemonics) {
        StringBuilder sb = new StringBuilder();
        for (String mnemonic : mnemonics) {
            sb.append(mnemonic);
            sb.append(" ");
        }
        return sb.toString();
    }

    //更新KeyStroe
    public static String updateKeyStore(String keyStore,String pwd,String newPassword) throws CipherException,IOException {
        HWalletFile old_walletFile = objectMapper.readValue(keyStore, HWalletFile.class);
        //解密助记词
        String origin_enemonic = null;
        if(!TextUtils.isEmpty(old_walletFile.encMnemonic)){
            origin_enemonic = HWallet.解密_M(old_walletFile.encMnemonic,pwd,old_walletFile);
        }
        ECKeyPair ecKeyPair = HWallet.decrypt(pwd, old_walletFile);
        HWalletFile new_walletFile = HWallet.create(newPassword, ecKeyPair, null,1024, 1);
        //生成BH-地址
        String bh_adress = BHKey.getBhexUserDpAddress(ecKeyPair);
        new_walletFile.setAddress(bh_adress);
        //生成新的助记词
        if(!TextUtils.isEmpty(origin_enemonic)){
            String enc_enemonic = HWallet.加密_M(origin_enemonic,newPassword,new_walletFile);
            new_walletFile.encMnemonic = enc_enemonic;

        }
        String raw_json = JsonUtils.toJson(new_walletFile);
        LogUtils.d("BHWalletUtils===>:","raw_json=="+raw_json);
        return raw_json;
    }


    /**
     * void
     */
    /*public static void test3(BaseActivity activity){

        BHProgressObserver pbo = new BHProgressObserver<BHWallet>(activity) {
            @Override
            public void onSuccess(BHWallet bhWallet) {

            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
            }
        };

        Observable.create((emitter)->{

            emitter.onNext(null);
            emitter.onComplete();

        }).compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(pbo);
        String addressArray = FileUtils.loadStringByAssets(BaseApplication.getInstance(),"address.txt");
        int count = 0;
        if(!TextUtils.isEmpty(addressArray)){
            List<String> list  = Arrays.asList(addressArray.split(" "));
            if(list!=null && list.size()>0){
                for (int i = 0; i < list.size(); i++) {
                    String itemString = list.get(i);
                    String [] itemArray = itemString.split(",");
                    //私钥
                    String privatekeyHex = itemArray[0];
                    String publicKey = itemArray[1].substring(2);
                    BigInteger publicInt = new BigInteger(publicKey,16);
                    String address = BHKey.getBhexUserDpAddress(publicInt);

                    String compressPub = BHKey.compressPubKey(publicInt);
                    if(address.equals(itemArray[3])){
                        LogUtils.d("BHWalletUtil===>:",i+"privatekeyHex =="+privatekeyHex+"==address=="+address+"=true=");
                    }else{
                        LogUtils.d("BHWalletUtil===>:",i+"privatekeyHex == "+privatekeyHex+"==address=="+compressPub+"=false=");
                        count++;
                    }
                }
                LogUtils.d("BHWalletUtil===>:","==count=="+count);

            }
        }

    }*/


}