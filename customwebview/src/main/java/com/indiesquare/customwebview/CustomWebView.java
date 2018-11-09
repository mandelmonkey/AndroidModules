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


    public static WebView createWebView(final Context ctx, final String injectedCode, int height, boolean debug, final CallbackInterface callback) {


        final WebView mWebView = new WebView(ctx);

        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.setWebChromeClient(new WebChromeClient());

        mWebView.setWebContentsDebuggingEnabled(debug);

        final WebViewMessageInterface interFace = new WebViewMessageInterface(callback);


        mWebView.addJavascriptInterface(interFace, "WebViewMessageInterface");

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, height);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        mWebView.setLayoutParams(params);
        mWebView.setWebViewClient( new WebViewClient(){

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onPageFinished(final WebView view, String url) {
                callback.eventFired("loaded");
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onPageStarted (WebView view,
                                       String url,
                                       Bitmap favicon){
                callback.eventFired("started");
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onLoadResource(final WebView view, String url)
            {

                view.evaluateJavascript(injectedCode, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        Log.d("LogName", s); // Log is written, but s is always null
                    }
                });

            }

        });

        return mWebView;

    }
}


