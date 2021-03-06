package com.bhex.wallet.common.utils;

import com.bhex.tools.crypto.CryptoUtil;
import com.bhex.tools.crypto.HexUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.common.crypto.wallet.HWalletFile;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.manager.BHUserManager;
import com.lambdaworks.crypto.SCrypt;

import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.params.KeyParameter;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author gongdongyang
 * 2020-5-23 15:04:25
 *
 */
public class HLWallet {

    private static final int CURRENT_VERSION = 3;

    private static final String CIPHER = "aes-128-ctr";
    static final String AES_128_CTR = "pbkdf2";
    static final String SCRYPT = "scrypt";

    private static byte[] generateDerivedScryptKey(
            byte[] password, byte[] salt, int n, int r, int p, int dkLen)  {
        try {
            return SCrypt.scrypt(password, salt, n, r, p, dkLen);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
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
            byte []result = cipher.doFinal(text);
            return result;
        } catch (NoSuchPaddingException e) {
            return throwCipherException(e);
        } catch (NoSuchAlgorithmException e) {
            return throwCipherException(e);
        } catch (InvalidAlgorithmParameterException e) {
            return throwCipherException(e);
        } catch (InvalidKeyException e) {
            return throwCipherException(e);
        } catch (BadPaddingException e) {
            return throwCipherException(e);
        } catch (IllegalBlockSizeException e) {
            return throwCipherException(e);
        }
    }

    private static byte[] throwCipherException(Exception e) throws CipherException {
        throw new CipherException("Error performing cipher operation", e);
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

        byte[] encMnemonicText = HexUtils.toBytes(walletFile.encMnemonic);
        LogUtils.d("HWallet===>:","???????????? encMnemonicText==="+Arrays.toString(encMnemonicText));

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
        byte[] encMnemonicKey = performCipherOperation(Cipher.DECRYPT_MODE, iv, encryptKey, encMnemonicText);
        LogUtils.d("HWallet===>:","???????????? encMnemonicText==="+Arrays.toString(encMnemonicKey));
        //Numeric.hexStringToByteArray

        LogUtils.d("HWallet===>:","???????????? encMnemonicText==="+new String(encMnemonicKey));
        //??????????????????????????????
        String old_mnemonic = new String(encMnemonicKey);
        BHWallet bhWallet = BHUserManager.getInstance().getCurrentBhWallet();
        //
        String encrypt_mnemonic = CryptoUtil.encryptMnemonic(old_mnemonic,password);
        bhWallet.setMnemonic(encrypt_mnemonic);
        return ECKeyPair.create(privateKey);
    }

    private static void validate(HWalletFile walletFile) throws CipherException {
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
}
