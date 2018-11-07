package com.indiesquare.customwebview;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
/**
 * Created by a on 2018/11/07.
 */

public class CustomWebView {
    public CustomWebView(){

    }


    public static WebView createWebView(final Context ctx, final String injectedCode, final CallbackInterface callback) {


        final WebView mWebView = new WebView(ctx);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        //mWebView.setId(0X100);
        mWebView.setScrollContainer(false);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mWebView.setLayoutParams(params);
        mWebView.setWebViewClient( new WebViewClient(){

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onPageFinished(final WebView view, String url) {

                Log.d("INJECTED CODE",url);
                Log.d("INJECTED CODE",injectedCode);
                view.evaluateJavascript(injectedCode, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        Log.d("LogName", s); // Log is written, but s is always null
                    }
                }); }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onPageStarted (WebView view,
                                       String url,
                                       Bitmap favicon){



            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, String url) {

                 view.evaluateJavascript(injectedCode, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        Log.d("LogName", s); // Log is written, but s is always null
                    }
                });
                /*
                Log.d("Custom Web",url);
                callback.eventFired(url);

                RequestQueue queue = Volley.newRequestQueue(ctx);


// Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                Log.d("load request",response);

                                view.loadData(response, "text/html; charset=UTF-8", null);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

// Add the request to the RequestQueue.
                queue.add(stringRequest);

*/
                return false;
            }
        });
        mWebView.loadUrl("https://www.cryptokitties.co");
        return mWebView;

    }
}


