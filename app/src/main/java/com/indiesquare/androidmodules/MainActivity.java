package com.indiesquare.androidmodules;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.indiesquare.androidcrypto.AndroidCrypto;
import com.indiesquare.androidkeystore.AndroidKeyStore;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "AndroidModules";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("test2", AndroidCrypto.generateRandomBytes());

        String alias = "keyRSA5";
        String keyValue = "test4";
      /*  AndroidKeyStore.removeAllData(alias,this);

        if(AndroidKeyStore.saveToKeyStore(alias,"my secret data is new",keyValue,this)){
            Log.d(TAG,"saved");
        }else{
            Log.e(TAG,"not saved");
        }

*/
        String savedData = AndroidKeyStore.loadFromKeyStore(alias, keyValue, this);

        Log.d(TAG, "result is: "+savedData);



    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

}
