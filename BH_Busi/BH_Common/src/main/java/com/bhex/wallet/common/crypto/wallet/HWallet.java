package com.bhex.wallet.common.crypto.wallet;

import android.text.TextUtils;

import com.bhex.tools.crypto.CryptoUtil;
import com.bhex.tools.crypto.HexUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.manager.BHUserManager;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.generators.SCrypt;
import org.bouncycastle.crypto.params.KeyParameter;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.bhex.wallet.common.crypto.wallet.SecureRandomUtils.secureRandom;
import static org.web3j.compat.Compat.UTF_8;

public class HWallet {

    private static final int N_LIGHT = 1 << 12;
    private static final int P_LIGHT = 6;

    private static final int N_STANDARD = 1 << 18;
    private static final int P_STANDARD = 1;

    private static final int R = 8;
    private static final int DKLEN = 32;

    private static final int CURRENT_VERSION = 3;

    private static final String CIPHER = "aes-128-ctr";
    static final String AES_128_CTR = "pbkdf2";
    static final String SCRYPT = "scrypt";

    public static HWalletFile create(String password, ECKeyPair ecKeyPair, int n, int p)
            throws CipherException {

        byte[] salt = generateRandomBytes(32);

        byte[] derivedKey = generateDerivedScryptKey(
                password.getBytes(UTF_8), salt, n, R, p, DKLEN);

        byte[] encryptKey = Arrays.copyOfRange(derivedKey, 0, 16);
        byte[] iv = generateRandomBytes(16);

        /*byte[] privateKeyBytes =
                Numeric.toBytesPadded(ecKeyPair.getPrivateKey(), Keys.PRIVATE_KEY_SIZE);*/

        byte[] privateKeyBytes =
                Numeric.toBytesPadded(ecKeyPair.getPrivateKey(), 32);

        byte[] cipherText = performCipherOperation(
                Cipher.ENCRYPT_MODE, iv, encryptKey, privateKeyBytes);

        byte[] mac = generateMac(derivedKey, cipherText);

        //
        return createWalletFile(ecKeyPair, cipherText, iv, salt, mac, n, p);
    }

    public static HWalletFile create(String password, ECKeyPair ecKeyPair, String mnemonicStr, int n, int p)
            throws CipherException {

        byte[] salt = generateRandomBytes(32);

        byte[] derivedKey = generateDerivedScryptKey(
                password.getBytes(UTF_8), salt, n, R, p, DKLEN);

        byte[] encryptKey = Arrays.copyOfRange(derivedKey, 0, 16);
        byte[] iv = generateRandomBytes(16);

        byte[] privateKeyBytes =
                Numeric.toBytesPadded(ecKeyPair.getPrivateKey(), 32);

        byte[] cipherText = performCipherOperation(
                Cipher.ENCRYPT_MODE, iv, encryptKey, privateKeyBytes);


        byte[] mac = generateMac(derivedKey, cipherText);

        //
        HWalletFile walletFile = createWalletFile(ecKeyPair, cipherText, iv, salt, mac, n, p);

        //byte[] mnemonicBytes =  Numeric.hexStringToByteArray(mnemonicStr);
        /*if(!TextUtils.isEmpty(mnemonicStr)){
            byte[] mnemonicBytes = mnemonicStr.getBytes();
            LogUtils.d("HWallet===>:","加密前的mnemonicBytes==="+Arrays.toString(mnemonicBytes));
            //加密
            byte[] mnemonicText = performCipherOperation(Cipher.ENCRYPT_MODE, iv, encryptKey, mnemonicBytes);
            LogUtils.d("HWallet===>:","加密后的mnemonicBytes==="+Arrays.toString(mnemonicText));
            //转hex编码
            walletFile.encMnemonic =  HexUtils.toHex(mnemonicText);;
            LogUtils.d("HWallet===>:","加密后的mnemonicStr==="+walletFile.encMnemonic);
        }*/

        return walletFile;
    }


    public static HWalletFile createStandard(String password, ECKeyPair ecKeyPair)
            throws CipherException {
        return create(password, ecKeyPair, N_STANDARD, P_STANDARD);
    }

    public static HWalletFile createLight(String password, ECKeyPair ecKeyPair)
            throws CipherException {
        return create(password, ecKeyPair, N_LIGHT, P_LIGHT);
    }

    private static HWalletFile createWalletFile(
            ECKeyPair ecKeyPair, byte[] cipherText, byte[] iv, byte[] salt, byte[] mac,
            int n, int p) {

        HWalletFile walletFile = new HWalletFile();
        walletFile.setAddress(Keys.getAddress(ecKeyPair));

        HWalletFile.Crypto crypto = new HWalletFile.Crypto();
        crypto.setCipher(CIPHER);
        crypto.setCiphertext(Numeric.toHexStringNoPrefix(cipherText));


        HWalletFile.CipherParams cipherParams = new HWalletFile.CipherParams();
        cipherParams.setIv(Numeric.toHexStringNoPrefix(iv));
        crypto.setCipherparams(cipherParams);

        crypto.setKdf(SCRYPT);
        HWalletFile.ScryptKdfParams kdfParams = new HWalletFile.ScryptKdfParams();
        kdfParams.setDklen(DKLEN);
        kdfParams.setN(n);
        kdfParams.setP(p);
        kdfParams.setR(R);
        kdfParams.setSalt(Numeric.toHexStringNoPrefix(salt));
        crypto.setKdfparams(kdfParams);

        crypto.setMac(Numeric.toHexStringNoPrefix(mac));
        walletFile.setCrypto(crypto);
        walletFile.setId(UUID.randomUUID().toString());
        walletFile.setVersion(CURRENT_VERSION);

        return walletFile;
    }

    private static byte[] generateDerivedScryptKey(
            byte[] password, byte[] salt, int n, int r, int p, int dkLen) throws CipherException {
        return SCrypt.generate(password, salt, n, r, p, dkLen);
    }

    private static byte[] generateAes128CtrDerivedKey(
            byte[] password, byte[] salt, int c, String prf) throws CipherException {

        if (!prf.equals("hmac-sha256")) {
            throw new CipherException("Unsupported prf:" + prf);
        }

        // Java 8 supports this, but you have to convert the password to a character array, see
        // http://stackoverflow.com/a/27928435/3211687

        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
        gen.init(password, salt, c);
        return ((KeyParameter) gen.generateDerivedParameters(256)).getKey();
    }

    private static byte[] performCipherOperation(
            int mode, byte[] iv, byte[] encryptKey, byte[] text) throws CipherException {

        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

            SecretKeySpec secretKeySpec = new SecretKeySpec(encryptKey, "AES");
            cipher.init(mode, secretKeySpec, ivParameterSpec);
            byte[] result = cipher.doFinal(text);
            return result;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidAlgorithmParameterException | InvalidKeyException
                | BadPaddingException | IllegalBlockSizeException e) {
            throw new CipherException("Error performing cipher operation", e);
        }
    }

    private static byte[] generateMac(byte[] derivedKey, byte[] cipherText) {
        byte[] result = new byte[16 + cipherText.length];

        System.arraycopy(derivedKey, 16, result, 0, 16);
        System.arraycopy(cipherText, 0, result, 16, cipherText.length);

        return Hash.sha3(result);
    }

    public static ECKeyPair decrypt(String password, HWalletFile walletFile)
            throws CipherException {

        validate(walletFile);

        HWalletFile.Crypto crypto = walletFile.getCrypto();

        byte[] mac = Numeric.hexStringToByteArray(crypto.getMac());
        byte[] iv = Numeric.hexStringToByteArray(crypto.getCipherparams().getIv());
        byte[] cipherText = Numeric.hexStringToByteArray(crypto.getCiphertext());

        byte[] derivedKey;

        if (crypto.getKdfparams() instanceof HWalletFile.ScryptKdfParams) {
            HWalletFile.ScryptKdfParams scryptKdfParams =
                    (HWalletFile.ScryptKdfParams) crypto.getKdfparams();
            int dklen = scryptKdfParams.getDklen();
            int n = scryptKdfParams.getN();
            int p = scryptKdfParams.getP();
            int r = scryptKdfParams.getR();
            byte[] salt = Numeric.hexStringToByteArray(scryptKdfParams.getSalt());
            derivedKey = generateDerivedScryptKey(
                    password.getBytes(Charset.forName("UTF-8")), salt, n, r, p, dklen);
        } else if (crypto.getKdfparams() instanceof HWalletFile.Aes128CtrKdfParams) {
            HWalletFile.Aes128CtrKdfParams aes128CtrKdfParams =
                    (HWalletFile.Aes128CtrKdfParams) crypto.getKdfparams();
            int c = aes128CtrKdfParams.getC();
            String prf = aes128CtrKdfParams.getPrf();
            byte[] salt = Numeric.hexStringToByteArray(aes128CtrKdfParams.getSalt());

            derivedKey = generateAes128CtrDerivedKey(
                    password.getBytes(Charset.forName("UTF-8")), salt, c, prf);
        } else {
            throw new CipherException("Unable to deserialize params: " + crypto.getKdf());
        }

        byte[] derivedMac = generateMac(derivedKey, cipherText);

        if (!Arrays.equals(derivedMac, mac)) {
            throw new CipherException("Invalid password provided");
        }

        byte[] encryptKey = Arrays.copyOfRange(derivedKey, 0, 16);
        byte[] privateKey = performCipherOperation(Cipher.DECRYPT_MODE, iv, encryptKey, cipherText);

        //
        /*if(!TextUtils.isEmpty(walletFile.encMnemonic)){
            byte[] encMnemonicText = HexUtils.toBytes(walletFile.encMnemonic);
            LogUtils.d("HWallet===>:","解密前的 encMnemonicText==="+Arrays.toString(encMnemonicText));
            byte[] encMnemonicKey = performCipherOperation(Cipher.DECRYPT_MODE, iv, encryptKey, encMnemonicText);
            LogUtils.d("HWallet===>:","解密后的 encMnemonicText==="+Arrays.toString(encMnemonicKey));
            //Numeric.hexStringToByteArray
            LogUtils.d("HWallet===>:","解密后的 encMnemonicText==="+new String(encMnemonicKey));
            //加密助记词保存内存中
            String old_mnemonic = new String(encMnemonicKey);
            BHWallet bhWallet = BHUserManager.getInstance().getCurrentBhWallet();
            //
            String encrypt_mnemonic = CryptoUtil.encryptMnemonic(old_mnemonic,password);
            bhWallet.setMnemonic(encrypt_mnemonic);
        }*/

        return ECKeyPair.create(privateKey);
    }



    static void validate(HWalletFile walletFile) throws CipherException {
        HWalletFile.Crypto crypto = walletFile.getCrypto();

        if (walletFile.getVersion() != CURRENT_VERSION) {
            throw new CipherException("Wallet version is not supported");
        }

        if (!crypto.getCipher().equals(CIPHER)) {
            throw new CipherException("Wallet cipher is not supported");
        }

        if (!crypto.getKdf().equals(AES_128_CTR) && !crypto.getKdf().equals(SCRYPT)) {
            throw new CipherException("KDF type is not supported");
        }
    }

    public static List<byte[]> 生成加密的_IV_encryptKey(String password, HWalletFile walletFile) throws CipherException{
        List<byte[]> reslut = new ArrayList<>();
        HWalletFile.Crypto crypto = walletFile.getCrypto();

        byte[] mac = Numeric.hexStringToByteArray(crypto.getMac());
        byte[] iv = Numeric.hexStringToByteArray(crypto.getCipherparams().getIv());
        byte[] cipherText = Numeric.hexStringToByteArray(crypto.getCiphertext());

        byte[] derivedKey;

        if (crypto.getKdfparams() instanceof HWalletFile.ScryptKdfParams) {
            HWalletFile.ScryptKdfParams scryptKdfParams =
                    (HWalletFile.ScryptKdfParams) crypto.getKdfparams();
            int dklen = scryptKdfParams.getDklen();
            int n = scryptKdfParams.getN();
            int p = scryptKdfParams.getP();
            int r = scryptKdfParams.getR();
            byte[] salt = Numeric.hexStringToByteArray(scryptKdfParams.getSalt());
            derivedKey = generateDerivedScryptKey(
                    password.getBytes(Charset.forName("UTF-8")), salt, n, r, p, dklen);
        } else if (crypto.getKdfparams() instanceof HWalletFile.Aes128CtrKdfParams) {
            HWalletFile.Aes128CtrKdfParams aes128CtrKdfParams =
                    (HWalletFile.Aes128CtrKdfParams) crypto.getKdfparams();
            int c = aes128CtrKdfParams.getC();
            String prf = aes128CtrKdfParams.getPrf();
            byte[] salt = Numeric.hexStringToByteArray(aes128CtrKdfParams.getSalt());

            derivedKey = generateAes128CtrDerivedKey(
                    password.getBytes(Charset.forName("UTF-8")), salt, c, prf);
        } else {
            throw new CipherException("Unable to deserialize params: " + crypto.getKdf());
        }

        byte[] derivedMac = generateMac(derivedKey, cipherText);

        if (!Arrays.equals(derivedMac, mac)) {
            throw new CipherException("Invalid password provided");
        }

        byte[] encryptKey = Arrays.copyOfRange(derivedKey, 0, 16);
        reslut.add(iv);
        reslut.add(encryptKey);
        return reslut;
    }
    public static String 加密_M(String content,String password,HWalletFile walletFile) throws CipherException{

        List<byte[]> result = 生成加密的_IV_encryptKey(password,walletFile);

        byte[] contentBytes =  content.getBytes();
        byte[] enc_contentBytes = performCipherOperation(Cipher.ENCRYPT_MODE, result.get(0), result.get(1), contentBytes);
        return HexUtils.toHex(enc_contentBytes);
    }

    public static String 解密_M(String content,String password,HWalletFile walletFile)throws CipherException{
        List<byte[]> result = 生成加密的_IV_encryptKey(password,walletFile);
        byte[] contentText = HexUtils.toBytes(content);
        byte[] encMnemonicKey = performCipherOperation(Cipher.DECRYPT_MODE, result.get(0), result.get(1), contentText);

        String old_mnemonic = new String(encMnemonicKey);
        return  old_mnemonic;
    }
    static byte[] generateRandomBytes(int size) {
        byte[] bytes = new byte[size];
        secureRandom().nextBytes(bytes);
        return bytes;
    }

}
