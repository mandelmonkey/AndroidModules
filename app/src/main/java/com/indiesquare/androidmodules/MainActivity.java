package com.indiesquare.androidmodules;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebView;
import android.widget.RelativeLayout;

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


    //test playground to test the other modules


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

            web3Script =
                    "var parent = document.getElementsByTagName('head').item(0);" +
                            "var script = document.createElement('script');" +
                            "script.type = 'text/javascript';" +
                            "script.innerHTML = window.atob('" + encoded + "');" +
                            "parent.appendChild(script)";


          web3Script ="javascript:(function() { " +
                    web3Script
                    +
                    "})()";

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            final WebView mWebView = CustomWebView.createWebView(this,web3Script,100,true,myCallback );

           // mWebView.loadUrl("https://www.mandelduck.com/ethereumSample?sd=e");
            mWebView.loadUrl("https://www.cryptokitties.co");
            setContentView(mWebView);


        } catch (Exception e) {
            e.printStackTrace();
            Log.e("web3text",e.toString());
        }


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
