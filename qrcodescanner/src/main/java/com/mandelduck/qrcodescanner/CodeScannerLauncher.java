package com.mandelduck.qrcodescanner;

import android.content.Context;
import android.content.Intent;

public class CodeScannerLauncher {

    public CodeScannerLauncher() {


    }

    public static CallbackInterface currentCallback;
    public static String buttonOneText;
    public static String buttonTwoText;
    public static Boolean hideOptionButton;

    public static void launchQrCodeActivity(Context context, boolean hideOption, String button1Text, String button2Text, CallbackInterface callback){

        buttonOneText = button1Text;
        buttonTwoText = button2Text;
        hideOptionButton = hideOption;

        currentCallback = callback;
        Intent i = new Intent(context, CodeScannerActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }


}
