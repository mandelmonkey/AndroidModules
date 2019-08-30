package com.mandelduck.qrcodescanner;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ErrorCallback;
import com.google.zxing.Result;

import org.json.JSONObject;

public class CodeScannerActivity extends Activity {
    private static final int RC_PERMISSION = 10;
    private CodeScanner mCodeScanner;
    private boolean mPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_scanner);

        mCodeScanner = new CodeScanner(this, (CodeScannerView) findViewById(R.id.scanner));
        mCodeScanner.setFormats(CodeScanner.TWO_DIMENSIONAL_FORMATS);
        Button closeButton = (Button)findViewById(R.id.closeButton);
        closeButton.setText(CodeScannerLauncher.buttonOneText);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });

        Button optionButton = (Button)findViewById(R.id.optionButton);
        optionButton.setText(CodeScannerLauncher.buttonTwoText);

        optionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response","button 2 clicked");

                    CodeScannerLauncher.currentCallback.eventFired(json.toString());
                } catch (Exception e2) {
                    CodeScannerLauncher.currentCallback.eventFired("");
                }

                finish();
            }

        });

        if(CodeScannerLauncher.hideOptionButton) {
            LinearLayout lin = (LinearLayout) findViewById(R.id.buttons);
            lin.removeView(findViewById(R.id.optionButton));
        }


        DecodeCallback dc = new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {

                try {
                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response",result);


                    CodeScannerLauncher.currentCallback.eventFired(json.toString());
                } catch (Exception e2) {
                    CodeScannerLauncher.currentCallback.eventFired("");
                }

                finish();

            }
        };

        ErrorCallback ec = new ErrorCallback() {
            @Override
            public void onError(@NonNull Exception error) {
                 try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response",error.getLocalizedMessage());

                    CodeScannerLauncher.currentCallback.eventFired(json.toString());
                } catch (Exception e2) {
                    CodeScannerLauncher.currentCallback.eventFired("");
                }

            }
        };

        mCodeScanner.setDecodeCallback(dc);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                mPermissionGranted = false;
                requestPermissions(new String[] {Manifest.permission.CAMERA}, RC_PERMISSION);
            } else {
                mPermissionGranted = true;
            }
        } else {
            mPermissionGranted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == RC_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mPermissionGranted = true;
                mCodeScanner.startPreview();
            } else {
                mPermissionGranted = false;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPermissionGranted) {
            mCodeScanner.startPreview();
        }
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
}
