package com.example.mostsave.data.security;

import android.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class EncryptionHelper {

    private static final String ALGORITHM = "AES/CBC/PKCS7Padding";
    private static final String TRANSFORMATION = "AES/CBC/PKCS7Padding";
    private static final byte[] IV = new byte[16]; // Initialization Vector

    private static SecretKeySpec generateKey(String password) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes(StandardCharsets.UTF_8);
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        return new SecretKeySpec(key, "AES");
    }

    public static String encrypt(String value, String password) throws Exception {
        SecretKeySpec secretKeySpec = generateKey(password);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(IV));
        byte[] encryptedValue = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(encryptedValue, Base64.DEFAULT);
    }

    public static String decrypt(String value, String password) throws Exception {
        SecretKeySpec secretKeySpec = generateKey(password);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(IV));
        byte[] decodedValue = Base64.decode(value, Base64.DEFAULT);
        byte[] decryptedValue = cipher.doFinal(decodedValue);
        return new String(decryptedValue, StandardCharsets.UTF_8);
    }
}
