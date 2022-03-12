package com.example.myapplication;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CrptoUtil {

    private static final String ALGORITHM = "RSA";

    public KeyPair keypair;
    static byte[] privateKey;
    static byte[] encryptedData;
    static SecretKey AESkey;

    public static byte[] getAESkey()
    {
        return AESkey.getEncoded();
    }
    public static byte[] encryptkey(byte[] aeskey, byte[] publicKey)
            throws Exception {

        PublicKey key = KeyFactory.getInstance(ALGORITHM)
                .generatePublic(new X509EncodedKeySpec(publicKey));

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encryptedBytes = cipher.doFinal(aeskey);

        return encryptedBytes;
    }

    public static byte[] decryptkey(byte[] privateKey, byte[] encryptedKey)
            throws Exception {

        PrivateKey key = KeyFactory.getInstance(ALGORITHM)
                .generatePrivate(new PKCS8EncodedKeySpec(privateKey));

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decryptedBytes = cipher.doFinal(encryptedKey);

        return decryptedBytes;
    }

    public static KeyPair generateKeyPair()
            throws NoSuchAlgorithmException, NoSuchProviderException {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);

        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        keyGen.initialize(512, random);

        KeyPair generateKeyPair = keyGen.generateKeyPair();
        return generateKeyPair;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String EncryptDriver(String inputtext) throws Exception {

        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        keygen.init(256);
        AESkey = keygen.generateKey();

        String encryptedText = encryptMsg(inputtext.getBytes(), AESkey);

        return encryptedText;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String DecryptionDriver(String encryptmsg, byte[] aeskey) throws Exception {
        byte[] decryptedData = decryptMsg(encryptmsg, aeskey);
        return new String(decryptedData);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String encryptMsg(byte[] message, SecretKey key) throws Exception {
        byte[] iv = new byte[16];
        SecureRandom random;
        random = new SecureRandom();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] ciphertext = cipher.doFinal(message);

        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + iv.length + ciphertext.length);
        byteBuffer.putInt(iv.length);
        byteBuffer.put(iv);
        byteBuffer.put(ciphertext);
        byte[] cipherMessage = byteBuffer.array();

        String encodedString = Base64.getEncoder().encodeToString(cipherMessage);
        return encodedString;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static byte[] decryptMsg(String encryptedMsg, byte[] key) throws Exception {

        byte[] cipherMessage = Base64.getDecoder().decode(encryptedMsg);
        ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);
        int ivLength = byteBuffer.getInt();

        byte[] iv = new byte[ivLength];
        byteBuffer.get(iv);
        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decryptedText = cipher.doFinal(cipherText);
        return decryptedText;

    }

}
