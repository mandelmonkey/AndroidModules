package com.indiesquare.lndmobilewrapper;

import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * Created by a on 2018/11/08.
 */

public class LndMobileMessageInterface {
    public com.indiesquare.lndmobilewrapper.CallbackInterface callbackInterface;
    public  LndMobileMessageInterface(com.indiesquare.lndmobilewrapper.CallbackInterface callbackInterface) {
        this.callbackInterface = callbackInterface;
    }

    @JavascriptInterface
    public void postMessage(String jsonData) {
        Log.d("postedMessage",jsonData);
        this.callbackInterface.eventFired(jsonData);

    }
}
