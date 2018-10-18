package com.indiesquare.androidmodules;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.indiesquare.androidcrypto.AndroidCrypto;
import com.indiesquare.androidkeystore.AndroidKeyStore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("test2", AndroidCrypto.generateRandomBytes());

        String alias = "keyRSA4";
         if(AndroidKeyStore.saveToKeyStore(alias,"it vorked afdfd11","test3",this)){
            Log.d("KeyStore","saved");
        }else{
            Log.e("KeyStore","not saved");
        }


        String savedData = AndroidKeyStore.loadFromKeyStore(alias, "test3", this);

        Log.d("Keystoresd2", savedData);
/*
        if(AndroidKeyStore.saveToKeyStore("userdata2","my sensitive data2","test2",this)){
            Log.d("KeyStore","saved");
        }else{
            Log.e("KeyStore","not saved");
        }



        String savedData2 = AndroidKeyStore.loadFromKeyStore("userdata2","test2",this);

        Log.d("Keystore2",savedData2);

*/

    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

}
