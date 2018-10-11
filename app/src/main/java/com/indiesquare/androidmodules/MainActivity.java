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
        Log.d("test",byteArrayToHex( AndroidCrypto.generateRandomBytes()));

        /*if(AndroidKeyStore.saveToKeyStore("my sensitive data",this)){
            Log.d("KeyStore","saved");
        }else{
            Log.e("KeyStore","not saved");
        }*/

        String savedData = AndroidKeyStore.loadFromKeyStore(this);

        Log.d("Keystore",savedData);


    }
    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

}
