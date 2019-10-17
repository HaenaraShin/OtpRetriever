package dev.haenara.otp.security;

import dev.haenara.otp.config.Config;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * A simple AES encryptor/decryptor.
 */
public class SecurityImpl implements ISecurity{
    private Cipher cipher;
    private static SecurityImpl instance;

    private SecurityImpl() {
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public static SecurityImpl getInstance() {
        if (instance == null) {
            instance = new SecurityImpl();
        }
        return instance;
    }

    @Override
    public String encrypt(String plainText) {
        try {
            return encAES(plainText);
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String decrypt(String encryptedText) {
        try {
            return decAES(encryptedText);
        } catch (Exception e) {
            return "";
        }
    }

    public Key getAESKey() throws Exception {
        String iv;
        Key keySpec;

        String key = Config.KEY;
        iv = key.substring(0, 16);
        byte[] keyBytes = new byte[16];
        byte[] b = key.getBytes("UTF-8");

        int len = b.length;
        if (len > keyBytes.length) {
            len = keyBytes.length;
        }

        System.arraycopy(b, 0, keyBytes, 0, len);
        keySpec = new SecretKeySpec(keyBytes, "AES");

        return keySpec;
    }

    /**
     * encrypt with AES (and encode with Base64)
     * @param str
     * @return
     * @throws Exception
     */
    private String encAES(String str) throws Exception {
        Key keySpec = getAESKey();
        cipher.init(Cipher.ENCRYPT_MODE, keySpec,  new IvParameterSpec(new byte[16]));
        byte[] encrypted = cipher.doFinal(str.getBytes("UTF-8"));
        String enStr = new String(Base64.getEncoder().encode(encrypted));

        return enStr;
    }

    /**
     * deencrypt with AES
     * @param enStr
     * @return
     * @throws Exception
     */
    private String decAES(String enStr) throws Exception {
        Key keySpec = getAESKey();
        cipher.init(Cipher.DECRYPT_MODE, keySpec,  new IvParameterSpec(new byte[16]));
        byte[] byteStr = Base64.getDecoder().decode(enStr.getBytes("UTF-8"));
        String decStr = new String(cipher.doFinal(byteStr), "UTF-8");

        return decStr;
    }
}
