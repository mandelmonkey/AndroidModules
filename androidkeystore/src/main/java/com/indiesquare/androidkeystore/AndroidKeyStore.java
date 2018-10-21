package com.indiesquare.androidkeystore;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.security.auth.x500.X500Principal;
/**
 * Created by a on 2018/10/11.
 */

public class AndroidKeyStore {
    private static final String TAG = "KeyStoreModule";
    static KeyStore keyStore;
    static SecretKey secretKey;
    static String cipherParams = KeyProperties.KEY_ALGORITHM_AES+"/"+KeyProperties.BLOCK_MODE_CBC+"/"+KeyProperties.ENCRYPTION_PADDING_PKCS7;

    public AndroidKeyStore(){


    }

    public static boolean useAES(String alias, Context ctx)  {

        if(keyStore == null) {
            try {
                keyStore = KeyStore.getInstance("AndroidKeyStore");
                keyStore.load(null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        //only use AES if the users version is M or greater and the alias is not an existing RSA key
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //supports AES so use it
    /*
    We need to check if the user has a key that was previously generated as RSA but has since upgraded to latest firmware that supports AES, in this case we check
    to see if the last key was RSA and if so continue to use RSA encrypt/decryption
     */
    try {
                if (keyStore.containsAlias(alias)) {

                    try {


                        //key is rsa type
                        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);
                        Log.d(TAG,"key is rsa");
                        return false;

                    }
                    catch (Exception e){
                        Log.d(TAG,"key not rsa");
                        Log.d(TAG,"Using AES");
                        return true;
                    }


                }
            }catch (Exception e){
                Log.e(TAG,Log.getStackTraceString(e));
                Log.d(TAG,"Using RSA");
                return false;
            }
            Log.d(TAG,"Using AES");
            return true;
        }else{ //old OS only supports RSA
            Log.d(TAG,"Using RSA");
            return false;

        }
    }


    public static String createNewKey(String alias, Context ctx) {

        try {

            try {
                if(keyStore == null) {
                    keyStore = KeyStore.getInstance("AndroidKeyStore");
                    keyStore.load(null);
                }
            }
            catch(Exception e) {}


            // Create new key if needed
            if (!keyStore.containsAlias(alias)) {

                if (useAES(alias,ctx)) {

                    KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                    keyGenerator.init(
                            new KeyGenParameterSpec.Builder(alias,
                                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                                    .build());
                    secretKey = keyGenerator.generateKey();
                    Log.d(TAG,"Created new AES key");
                    SecretKeyFactory factory = SecretKeyFactory.getInstance(secretKey.getAlgorithm(), "AndroidKeyStore");
                    KeyInfo keyInfo = (KeyInfo) factory.getKeySpec(secretKey, KeyInfo.class);

                    if (!keyInfo.isInsideSecureHardware()) {
                        // is this acceptable? Depends on the app
                        Log.e(TAG,"not secure hardware");

                    }
                }
                else{ //AES not supported so use RSA

                    Calendar start = Calendar.getInstance();
                    Calendar end = Calendar.getInstance();
                    end.add(Calendar.YEAR, 100);

                    KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(ctx)
                            .setAlias(alias)
                            .setSubject(new X500Principal("CN=Sample Name, O=Android Authority"))
                            .setSerialNumber(BigInteger.ONE)
                            .setStartDate(start.getTime())
                            .setEndDate(end.getTime())
                            .build();
                    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                    generator.initialize(spec);

                    KeyPair keyPair = generator.generateKeyPair();

                Log.d(TAG,"Created new RSA key");


                }

                return "created key";
            }else {
                try {


                    //key is rsa type
                    PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);
                    Log.d(TAG,"key is rsa 1");

                }
                catch (Exception e){
                    Log.d(TAG,"key not rsa 1");
                    secretKey = (SecretKey) keyStore.getKey(alias, null);
                }

                return "key already exists";
            }
        } catch (Exception e) { ;
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return "error";

    }


    public static boolean saveToKeyStore(String alias,String sensitiveData, String dataKey, Context ctx){
        String key = createNewKey(alias,ctx);



    if(key != "error") {
        if (useAES(alias,ctx)) {
            String[] encryptedDataIV = encryptString(alias, sensitiveData);

            if (encryptedDataIV[0] != "error") {
                SharedPreferences.Editor editor = ctx.getSharedPreferences("SP", Activity.MODE_PRIVATE).edit();

                editor.putString(dataKey, encryptedDataIV[0]);
                editor.putString(dataKey + "_IV", encryptedDataIV[1]);
                editor.apply();
                return true;
            }

        }else{

            String  encryptedData = encryptStringRSA(alias,sensitiveData);
            if(encryptedData != "error") {

                SharedPreferences.Editor editor = ctx.getSharedPreferences("SP", Activity.MODE_PRIVATE).edit();

                editor.putString(dataKey, encryptedData);
                editor.apply();
                return true;
            }

        }
    }
    return false;



    }

    public static boolean removeAllData(Context ctx){
        SharedPreferences settings = ctx.getSharedPreferences("SP", Context.MODE_PRIVATE);
        settings.edit().clear().apply();
        return true;
    }

    public static String loadFromKeyStore(String alias,String dataKey,Context ctx) {

        SharedPreferences sharedPreferences = ctx.getSharedPreferences("SP", Activity.MODE_PRIVATE);

        if( !sharedPreferences.contains(dataKey)){
            return "does not exist";
        }
        String encryptedDataString = sharedPreferences.getString(dataKey, null);

        if (useAES(alias,ctx)) {

            if (!sharedPreferences.contains(dataKey + "_IV")) {
                return "IV does not exist";
            }



            String IV = sharedPreferences.getString(dataKey + "_IV", null);

            String key = createNewKey(alias, ctx);

            if (key != "error") {
                String decryptedData = decryptString(alias, encryptedDataString, IV);

                if(decryptedData != "error"){
                    return decryptedData;
                }

            }
                return "error";


        }
        else{
            String key = createNewKey(alias, ctx);

            if (key != "error") {

                String decryptedData = decryptStringRSA(alias,encryptedDataString);
                if(decryptedData != "error"){
                    return decryptedData;
                }


            }
                return "error";


        }

    }

    public static String[] encryptString(String alias,String data) {
        try {

            Cipher cipher = Cipher.getInstance(cipherParams);
            cipher.init(Cipher.ENCRYPT_MODE,secretKey);

            byte[] encryptionIV = cipher.getIV();
            byte[] sensitiveDataBytes = data.getBytes("UTF-8");
            byte[] encryptedSensitiveDataBytes = cipher.doFinal(sensitiveDataBytes);
            String encryptedSensitiveDataString = Base64.encodeToString(encryptedSensitiveDataBytes,Base64.DEFAULT);
            String encryptionIVString = Base64.encodeToString(encryptionIV,Base64.DEFAULT);
            return new String[]{
                    encryptedSensitiveDataString, encryptionIVString
            };
        } catch (Exception e) {

            Log.e(TAG, Log.getStackTraceString(e));
        }
        return new String[]{
                "error", "error"
        };
    }

    public static String decryptString(String alias, String encryptedDataStr, String IV) {
        try {

            byte[] encryptionIV = Base64.decode(IV, Base64.DEFAULT);
            byte[] encryptedData = Base64.decode(encryptedDataStr, Base64.DEFAULT);


            Cipher cipher = Cipher.getInstance(cipherParams);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(encryptionIV));

            byte[] decryptedDataBytes = cipher.doFinal(encryptedData);

            String decryptedData = new String(decryptedDataBytes, "UTF-8");

            return decryptedData;

        } catch (Exception e) {

            Log.e(TAG, Log.getStackTraceString(e));
        }
        return "error";
    }


    public static String encryptStringRSA(String alias,String data) {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();

            Cipher inCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            inCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    outputStream, inCipher);
            cipherOutputStream.write(data.getBytes("UTF-8"));
            cipherOutputStream.close();

            byte [] vals = outputStream.toByteArray();
            return Base64.encodeToString(vals, Base64.DEFAULT);


        } catch (Exception e) {

            Log.e(TAG, Log.getStackTraceString(e));
            return "error";
        }
    }


    public static String decryptStringRSA(String alias,String encryptedDataStr) {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
            RSAPrivateKey privateKey = (RSAPrivateKey) privateKeyEntry.getPrivateKey();

            Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            output.init(Cipher.DECRYPT_MODE, privateKey);

            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(Base64.decode(encryptedDataStr, Base64.DEFAULT)), output);
            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte)nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for(int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i).byteValue();
            }

            String decryptedString = new String(bytes, 0, bytes.length, "UTF-8");
            return decryptedString;

        } catch (Exception e) {

            Log.e(TAG, Log.getStackTraceString(e));
            return "error";
        }
    }


}
