package com.indiesquare.androidkeystore;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

/**
 * Created by a on 2018/10/11.
 */



public class AndroidKeyStore {
    private static final String TAG = "KeyStoreModule";
    static KeyStore keyStore;
    static SecretKey secretKey;
    static String cipherParams = KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7;
    static String aesKeyTag = "RSAEncryptedAESKey";
    static String spTAG = "userSensitiveData";




    public AndroidKeyStore() {


    }

    public static void init(){
        if (keyStore == null) {
            try {
                keyStore = KeyStore.getInstance("AndroidKeyStore");
                keyStore.load(null);
            } catch (Exception e) {
                e.printStackTrace();

                Log.e(TAG, Log.getStackTraceString(e));

            }

        }
    }


    public static String saveToKeyStore(String alias, String sensitiveData, String dataKey, Context ctx) {

        AndroidKeyStoreResponse setAes = setAESKey(alias,ctx);
        if (setAes.error != null) {
            Log.e(TAG, "error generating or recovering AES key");
            return formatForTitanium(setAes);
        }

        AndroidKeyStoreResponse res = encryptString(alias, sensitiveData);

        if(res.error != null){
            return formatForTitanium(res);
        }

        String[] encryptedDataIV = (String[])res.response;


            SharedPreferences.Editor editor = ctx.getSharedPreferences(spTAG, Activity.MODE_PRIVATE).edit();

            editor.putString(dataKey, encryptedDataIV[0]);
            editor.putString(dataKey + "_IV", encryptedDataIV[1]);
            editor.apply();
            return formatForTitanium(new AndroidKeyStoreResponse("success",null));





    }
    static String formatForTitanium(AndroidKeyStoreResponse res){

         JSONObject json = new JSONObject();

        try {
            if(res.error != null) {
                json.put("error", res.error);
            }
            if(res.response != null) {
                json.put("response", res.response);
            }



        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            e.printStackTrace();

        }

        return json.toString();


    }

    public static String loadFromKeyStore(String alias, String dataKey, Context ctx){

        SharedPreferences sharedPreferences = ctx.getSharedPreferences(spTAG, Activity.MODE_PRIVATE);

        if (sharedPreferences.contains(dataKey)) {


            String encryptedDataString = sharedPreferences.getString(dataKey, null);


            AndroidKeyStoreResponse setAES = setAESKey(alias,ctx);
                if(setAES.error != null){
                    return formatForTitanium(setAES);
                }



                if (!sharedPreferences.contains(dataKey + "_IV")) {
                    return formatForTitanium( new AndroidKeyStoreResponse(null, "IV does not exist"));

                }


                String IV = sharedPreferences.getString(dataKey + "_IV", null);

                return formatForTitanium(decryptString(alias, encryptedDataString, IV));



        }

        return formatForTitanium(new AndroidKeyStoreResponse(null,"does not exist"));


    }


    public static String removeAllData(String alias, Context ctx) {

        Log.d(TAG,"Removing all data from keystore and preferences");
        SharedPreferences settings = ctx.getSharedPreferences(spTAG, Context.MODE_PRIVATE);
        settings.edit().clear().apply();
        init();

        try {
            keyStore.deleteEntry(alias);
        } catch (KeyStoreException e) {
            Log.e(TAG,e.toString());
            e.printStackTrace();
           return formatForTitanium(new AndroidKeyStoreResponse(null, Log.getStackTraceString(e)));
        }
        secretKey = null;
        keyStore = null;


        Log.d(TAG,"Removed all data from keystore and preferences");
        return formatForTitanium(new AndroidKeyStoreResponse("success",null));
    }


    static boolean isKeyStoreAES(String alias, Context ctx) {

        init();
        //only use AES if the users version is M or greater and the alias is not an existing RSA key
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //supports AES so use it
    /*
    We need to check if the user has a key that was previously generated as RSA but has since upgraded to latest firmware that supports AES, in this case we check
    to see if the last key was RSA and if so continue to use RSA encrypt/decryption
     */
            try {
                if (keyStore.containsAlias(alias)) {
                    //key exist try and decode asymmetic key, if error its a symmetric AES key if not its RSA

                    try {
                        //key is rsa type
                        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);
                        Log.d(TAG, "key is rsa");
                        return false;

                    } catch (Exception e) {

                        Log.d(TAG, "Not RSA Keystore");
                        Log.d(TAG, "Using AES Keystore");
                        return true;
                    }


                }
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
                Log.d(TAG, "Using RSA Keystore");
                return false;
            }
            Log.d(TAG, "Using AES Keystore");
            return true;
        } else { //old OS only supports RSA for keystore
            Log.d(TAG, "Using RSA Keystore");
            return false;

        }
    }

    static AndroidKeyStoreResponse setAESKey(String alias, Context ctx){

        try {

            init();

            if(secretKey != null){
                //secret key already set!
                Log.d(TAG,"secret key already set");

            return new AndroidKeyStoreResponse("secret key already set",null);
            }

            // Create new key if needed
            if (!keyStore.containsAlias(alias)) {

                if (isKeyStoreAES(alias, ctx)) { //check if we should use the AES keystore or the RSA keystore
                    //generate symmetric AES key from keystore

                    KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                    keyGenerator.init(
                            new KeyGenParameterSpec.Builder(alias,
                                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                                    .build());
                    secretKey = keyGenerator.generateKey();

                    Log.d(TAG, "Created new AES key");

                    SecretKeyFactory factory = SecretKeyFactory.getInstance(secretKey.getAlgorithm(), "AndroidKeyStore");
                    KeyInfo keyInfo = (KeyInfo) factory.getKeySpec(secretKey, KeyInfo.class);

                    if (!keyInfo.isInsideSecureHardware()) {
                        // is this acceptable? Depends on the app
                        Log.e(TAG, "not secure hardware");

                    }
                    return new AndroidKeyStoreResponse("Created new AES key",null);
                } else { //AES keystore not supported so use hybrid method of generatingRSA keystore to encrypt a seperate AES Symmetric Key

                    /*android keystore has an issue on persian lang so set lang to english temporarily*/
                    Locale initialLocale = Locale.getDefault();
                    setLocale(Locale.ENGLISH,ctx);

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

                    setLocale(initialLocale,ctx);

                    Log.d(TAG, "Created new RSA key");

                    //RSA Asymmetric key generated from Keystore, now create an AES key
                    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                    keyGen.init(128);//128 bits is ok for app use case
                    secretKey = keyGen.generateKey();

                    //Encrypt the AES key with the RSA keystore public key
                    String encryptedSecretKey = (String)encryptAESKey(keyPair.getPublic(), secretKey).response;

                    Log.d(TAG, "saving encrypted AES key to preferences");

                    //Save the encrypted AES key to preferences;
                    SharedPreferences.Editor editor = ctx.getSharedPreferences(spTAG, Activity.MODE_PRIVATE).edit();

                    editor.putString(aesKeyTag, encryptedSecretKey);

                    editor.apply();

                    Log.d(TAG, "saved encrypted AES key to preferences: "+encryptedSecretKey);

                    return new AndroidKeyStoreResponse("Created new RSA key",null);


                }

            } else {
                //Keystore already exists so detect if its RSA or AES keystore
                if (isKeyStoreAES(alias, ctx)) {

                    //keystore is AES so use that key directly as the AES key
                    Log.d(TAG, "loading keystore aes");
                    secretKey = (SecretKey) keyStore.getKey(alias, null);

                    return new AndroidKeyStoreResponse("Loaded AES key",null);

                }
                else {
                    //keystore is RSA so use the RSA private key to decrypted the saved AES key
                    Log.d(TAG, "loading keystore RSA");

                    KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);

                    Log.d(TAG, "loading stored AES key");

                    SharedPreferences sharedPreferences = ctx.getSharedPreferences(spTAG, Activity.MODE_PRIVATE);

                    if (!sharedPreferences.contains(aesKeyTag)) {
                        Log.e(TAG, "aes key doest not exist in shared preferences");
                        return new AndroidKeyStoreResponse(null,"aes key doest not exist in shared preferences");

                    }
                    String encryptedAESKeyString = sharedPreferences.getString(aesKeyTag, null);

                    Log.d(TAG, "loaded stored AES key: "+encryptedAESKeyString);

                    secretKey = (SecretKey)decryptAESKey(privateKeyEntry.getPrivateKey(), encryptedAESKeyString).response ;

                    return new AndroidKeyStoreResponse(null,"loaded stored AES key");

                }


            }
        } catch (Exception e) {

            Log.e(TAG, Log.getStackTraceString(e));

            return new AndroidKeyStoreResponse(null, Log.getStackTraceString(e));

        }


    }

    static AndroidKeyStoreResponse encryptString(String alias, String data)  {
        try {

            Cipher cipher = Cipher.getInstance(cipherParams);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptionIV = cipher.getIV();
            byte[] sensitiveDataBytes = data.getBytes("UTF-8");
            byte[] encryptedSensitiveDataBytes = cipher.doFinal(sensitiveDataBytes);
            String encryptedSensitiveDataString = Base64.encodeToString(encryptedSensitiveDataBytes, Base64.DEFAULT);
            String encryptionIVString = Base64.encodeToString(encryptionIV, Base64.DEFAULT);
            String[] response = new String[]{
                    encryptedSensitiveDataString, encryptionIVString
            };
            return new AndroidKeyStoreResponse(response, null);
        } catch (Exception e) {

            Log.e(TAG, Log.getStackTraceString(e));


            Log.e(TAG, Log.getStackTraceString(e));
            return new AndroidKeyStoreResponse(null, Log.getStackTraceString(e));
        }

    }

    static AndroidKeyStoreResponse decryptString(String alias, String encryptedDataStr, String IV){
        try {

            byte[] encryptionIV = Base64.decode(IV, Base64.DEFAULT);
            byte[] encryptedData = Base64.decode(encryptedDataStr, Base64.DEFAULT);


            Cipher cipher = Cipher.getInstance(cipherParams);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(encryptionIV));

            byte[] decryptedDataBytes = cipher.doFinal(encryptedData);

            String decryptedData = new String(decryptedDataBytes, "UTF-8");

            return new AndroidKeyStoreResponse( decryptedData, null);

        } catch (Exception e) {

            return new AndroidKeyStoreResponse(null, Log.getStackTraceString(e));

        }
    }


    static AndroidKeyStoreResponse encryptAESKey(PublicKey publicKey, SecretKey aesKey) {
        try {

            Cipher inCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            inCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    outputStream, inCipher);
            cipherOutputStream.write(aesKey.getEncoded());
            cipherOutputStream.close();

            byte[] vals = outputStream.toByteArray();
            String response = Base64.encodeToString(vals, Base64.DEFAULT);
            return new AndroidKeyStoreResponse(response, null);


        } catch (Exception e) {


            Log.e(TAG, Log.getStackTraceString(e));
            return new AndroidKeyStoreResponse(null, Log.getStackTraceString(e));
        }
    }


    static AndroidKeyStoreResponse decryptAESKey(PrivateKey privateKey, String encryptedAESKey)  {
        try {

            Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            output.init(Cipher.DECRYPT_MODE, privateKey);

            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(Base64.decode(encryptedAESKey, Base64.DEFAULT)), output);
            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte) nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i).byteValue();
            }
            SecretKey originalKey = new SecretKeySpec(bytes, 0, bytes.length, "AES");

            return new AndroidKeyStoreResponse(originalKey, null);

        } catch (Exception e) {

            Log.e(TAG, Log.getStackTraceString(e));
            return new AndroidKeyStoreResponse(null, Log.getStackTraceString(e));
        }
    }


    private static void setLocale(Locale locale,Context ctx) {
        Locale.setDefault(locale);
        Resources resources = ctx.getResources();
        Configuration config = resources.getConfiguration();
        config.locale = locale;
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }



}
