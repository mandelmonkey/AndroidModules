package com.indiesquare.androidmodules;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.indiesquare.customwebview.CallbackInterface;
import com.indiesquare.customwebview.CustomWebView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "AndroidModules";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);





        CallbackInterface myCallback = new CallbackInterface() {
            @Override
            public void eventFired(String event) {
                Log.d("event fired",event);
            }
        };

        try {
           String web3Script = getStringFromFile("trust-min.txt");
            String webIndie = getStringFromFile("webIndie.txt");
            String trustConfig = getStringFromFile("trustConfig.txt");

            trustConfig = trustConfig.replace("replaceWithAddress", "0x3b7b6f8c1b9fe9be5b59326a3e8c695fce3a4e08");
            trustConfig = trustConfig.replace("replaceWithRPCURL","https://mainnet.infura.io/a6e34ed067c74f25ba705456d73a471e");

            web3Script = web3Script+webIndie+trustConfig;
           // web3Script = "var web3 = {eth:{getAccounts:function(cb){cb('','rtrt')}}};";
            Log.d("web3is",web3Script);
            //web3Script = "function log(){};";


            byte[] buffer = web3Script.getBytes();
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);



            String checkReq = "if(typeof web3 !== 'undefined' && typeof GET_CURRENT_TASK !== 'undefined'){return {status:'data',data:GET_CURRENT_TASK(),buttons:document.readyState} }";


            String injectjs =
                    "function injectCode() {if(typeof web3 === 'undefined'){var parent = document.getElementsByTagName('head').item(0);" +
                            "var script = document.createElement('script');" +
                            "script.type = 'text/javascript';" +
                            "script.innerHTML =  " + web3Script + ";" +
                            "parent.appendChild(script);  return {status:'injected',buttons:document.readyState}}else { " + checkReq + "}} injectCode();";


            web3Script =
                    "var parent = document.getElementsByTagName('head').item(0);" +
                            "var script = document.createElement('script');" +
                            "script.type = 'text/javascript';" +
                            "script.innerHTML = window.atob('" + encoded + "');" +
                            "parent.appendChild(script)";

           /* ClipboardManager clipboard = (ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("simple text", web3Script);*/


          //  byte[] data = Base64.decode(encoded, Base64.DEFAULT);
           // String text = new String(data, "UTF-8");
            WriteText(web3Script);

          web3Script ="javascript:(function() { " +
                    web3Script
                    +
                    "})()";


            final String newStr = web3Script;

            final WebView mWebView = CustomWebView.createWebView(this,web3Script,myCallback );
mWebView.setWebContentsDebuggingEnabled(true);

            setContentView(mWebView);

            /* final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 100ms

                    mWebView.evaluateJavascript(newStr, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            Log.d("LogName", s); // Log is written, but s is always null
                        }
                    });

                }
            }, 3000);

*/

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("web3text",e.toString());
        }


/*


        Log.d("test2", AndroidCrypto.generateRandomBytes());

        String alias = "keyRSA5";
        String keyValue = "test4";

       // AndroidKeyStore.removeAllData(alias,this);




String res = AndroidKeyStore.saveToKeyStore(alias,"my secret data is new",keyValue,this);

            Log.d(TAG,"saved "+res);




          String response = AndroidKeyStore.loadFromKeyStore(alias, keyValue, this);


                Log.d(TAG, "result is: " + response );


         res = AndroidKeyStore.saveToKeyStore(alias,"my secret data is new",keyValue,this);

        Log.d(TAG,"saved "+res);




          response = AndroidKeyStore.loadFromKeyStore(alias, keyValue, this);


        Log.d(TAG, "result is: " + response );

*/

    }
    public void WriteText(String txt) {


        try {
            BufferedWriter fos = new BufferedWriter(new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+"Fileewwe.txt"));
            fos.write(txt);
            fos.close();
        } catch (Exception e) {
            Log.e("erooor ",e.toString());
        }
    }
    public String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public String getStringFromFile (String filePath) throws Exception {
        InputStream fin =getApplicationContext().getAssets().open(filePath);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }
    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

}
