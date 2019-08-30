package com.mandelduck.testfairysdk;

import android.app.Activity;
import com.testfairy.TestFairy;

public class TestFairyLoader {

    public TestFairyLoader() {




    }

    public static void startTestFairy(Activity activity, String appID){
        TestFairy.begin(activity, appID);
    }

}
