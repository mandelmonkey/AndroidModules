package com.indiesquare.customwebview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.format.DateUtils;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import java.util.Date;

import java.io.File;

/**
 * Created by a on 2018/11/07.
 */

public class CustomWebView {
    public CustomWebView(){

    }

    static int clearCacheFolder(final File dir, final int numDays) {

        int deletedFiles = 0;
        if (dir!= null && dir.isDirectory()) {
            try {
                for (File child:dir.listFiles()) {

                    //first delete subdirectories recursively
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, numDays);
                    }

                    //then delete the files and subdirectories in this dir
                    //only empty directories can be deleted, so subdirs have been done first
                    if (child.lastModified() < new Date().getTime() - numDays * DateUtils.DAY_IN_MILLIS) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            }
            catch(Exception e) {
                Log.e("webview", String.format("Failed to clean the cache, error %s", e.getMessage()));
            }
        }
        return deletedFiles;
    }

    /*
     * Delete the files older than numDays days from the application cache
     * 0 means all files.
     */
    public static void clearCache(final Context context, final int numDays) {
        Log.i("webview", String.format("Starting cache prune, deleting files older than %d days", numDays));
        int numDeletedFiles = clearCacheFolder(context.getCacheDir(), numDays);
        Log.i("webview", String.format("Cache pruning completed, %d files deleted", numDeletedFiles));
    }


    public static WebView createWebView(final Context ctx, final String injectedCode, int height, boolean debug, final CallbackInterface callback) {


        final WebView mWebView = new WebView(ctx);

        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.setWebChromeClient(new WebChromeClient());

        mWebView.setWebContentsDebuggingEnabled(debug);

        final WebViewMessageInterface interFace = new WebViewMessageInterface(callback);

       //mWebView.clearCache(true);
       //clearCache(ctx,100000);

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

                view.evaluateJavascript(injectedCode, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        Log.d("LogName", s); // Log is written, but s is always null
                    }
                });
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onPageStarted (WebView view,
                                       String url,
                                       Bitmap favicon){
                callback.eventFired("started");

                view.evaluateJavascript(injectedCode, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        Log.d("LogName", s); // Log is written, but s is always null
                    }
                });
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
                callback.eventFired("error");
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


            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.evaluateJavascript(injectedCode, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        Log.d("LogName", s); // Log is written, but s is always null
                    }
                });
                String url = request.getUrl().toString();
                if(url.startsWith("lightning:") || url.startsWith("bitcoin:") || url.startsWith("counterparty:"))
                {
                    return true;
                }
                return false;
            }

        });

        return mWebView;

    }
}


