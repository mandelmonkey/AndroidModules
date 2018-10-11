package com.indiesquare.androidkeystore;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;

/**
 * Created by a on 2018/10/11.
 */

public class AndroidKeyStore {

    public AndroidKeyStore(){

    }
    static String keyIdentifier = "userData";
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean saveToKeyStore(String sensitiveData, Context ctx){

        final KeyStore ks;
        try {
            ks = KeyStore.getInstance("AndroidKeyStore");

            try {
                ks.load(null);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            }
            try {
                SecretKey key = (SecretKey) ks.getKey(keyIdentifier,null);
                try {
                    KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
                    keyGenerator.init(
                            new KeyGenParameterSpec.Builder(keyIdentifier,
                                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                                    .build());
                    key = keyGenerator.generateKey();

                    SecretKeyFactory factory = SecretKeyFactory.getInstance(key.getAlgorithm(), "AndroidKeyStore");
                    KeyInfo keyInfo = (KeyInfo) factory.getKeySpec(key, KeyInfo.class);
                    if (!keyInfo.isInsideSecureHardware()) {
                        // is this acceptable? Depends on the app
                    }

                    Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES+"/"+KeyProperties.BLOCK_MODE_CBC+"/"+KeyProperties.ENCRYPTION_PADDING_PKCS7);
                    cipher.init(Cipher.ENCRYPT_MODE,key);

                    byte[] encryptionIV = cipher.getIV();
                    byte[] sensitiveDataBytes = sensitiveData.getBytes("UTF-8");
                    byte[] encryptedSensitiveDataBytes = cipher.doFinal(sensitiveDataBytes);
                    String encryptedSensitiveDataString = Base64.encodeToString(encryptedSensitiveDataBytes,Base64.DEFAULT);
                    String encryptionIVString = Base64.encodeToString(encryptionIV,Base64.DEFAULT);

                    SharedPreferences.Editor editor = ctx.getSharedPreferences("SP", Activity.MODE_PRIVATE).edit();
                    editor.putString("data",encryptedSensitiveDataString);
                    editor.putString("iv",encryptionIVString);
                    editor.apply();
                    return true;


                }
                catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e){
                    throw new RuntimeException("Failed to create symetric key",e);
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                }
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                e.printStackTrace();

            }

        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static String loadFromKeyStore(Context ctx) {

        try {
            SharedPreferences sharedPreferences = ctx.getSharedPreferences("SP", Activity.MODE_PRIVATE);

            String ivString = sharedPreferences.getString("iv", null);
            String encryptedDataString = sharedPreferences.getString("data", null);


            byte[] encryptionIV = Base64.decode(ivString, Base64.DEFAULT);
            byte[] encryptedSensitiveData = Base64.decode(encryptedDataString, Base64.DEFAULT);

            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            SecretKey secretKey = (SecretKey) keyStore.getKey(keyIdentifier, null);
            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec((encryptionIV)));

            byte[] sensitiveDataBytes = cipher.doFinal(encryptedSensitiveData);

            String sensitiveDataString = new String(sensitiveDataBytes, "UTF-8");

            return sensitiveDataString;
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return "";

    }
}
