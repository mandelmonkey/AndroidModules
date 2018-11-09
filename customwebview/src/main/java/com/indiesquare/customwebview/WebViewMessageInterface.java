package com.indiesquare.customwebview;

import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * Created by a on 2018/11/08.
 */

public class WebViewMessageInterface {
    public CallbackInterface callbackInterface;
    public WebViewMessageInterface(CallbackInterface callbackInterface) {
        this.callbackInterface = callbackInterface;
    }

    @JavascriptInterface
    public void postMessage(String jsonData) {
        Log.d("postedMessage",jsonData);
        this.callbackInterface.eventFired(jsonData);

    }
}
