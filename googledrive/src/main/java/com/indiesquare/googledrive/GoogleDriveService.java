package com.indiesquare.googledrive;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;


import java.util.ArrayList;
import java.util.List;



public class GoogleDriveService {

    ServiceListener serviceListener;

    String TAG = "google drive service";
    private static int REQUEST_CODE_SIGN_IN = 101;
    private Activity activity;
    private GoogleSignInClient googleSignInClient;
    public GoogleDriveService(Activity currentActivity){

        Log.i(TAG,"INITATING SERVICE");

        activity = currentActivity;
        Log.i(TAG,"create client");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes( new Scope("https://www.googleapis.com/auth/drive")) // "https://www.googleapis.com/auth/plus.login"
                .requestEmail()
                .build();



        Log.i(TAG,"create client 2");

        googleSignInClient =  GoogleSignIn.getClient(activity, gso);
        Log.i(TAG,"create client 3");


    }

    private void initializeDriveClient(GoogleSignInAccount signInAccount) {

    }

    public void signOut(){

        googleSignInClient.revokeAccess();
        googleSignInClient.signOut();
    }

    public boolean checkLoginStatus(){
        Log.i(TAG,"check login");

        List<Scope> requiredScopes = new ArrayList<>();
        requiredScopes.add(new Scope("https://www.googleapis.com/auth/drive"));
        Log.i(TAG,"check login2");

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(activity.getApplicationContext());
        Log.i(TAG,"check login3");
        if(signInAccount != null) {
            Boolean containsScope = signInAccount.getGrantedScopes().containsAll(requiredScopes);
            Log.i(TAG,"check login4");

            if (signInAccount != null && containsScope == true) {
                Log.i(TAG,"check login5");
                initializeDriveClient(signInAccount);
                return true;

            }
        }
        Log.i(TAG,"check login6");

        return false;
    }

    public void auth(){
        Log.i(TAG,"starting auth");

         activity.startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);


    }


    private void handleSignIn(Intent data) {
        Log.i(TAG,"handle sign in1");

        Task<GoogleSignInAccount> getAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
        if (getAccountTask.isSuccessful()) {
            Log.i(TAG,"handle sign in2");

            initializeDriveClient(getAccountTask.getResult());
        } else {
            Log.i(TAG,"handle sign in3");

            serviceListener.handleError(new Exception("Sign-in failed.", getAccountTask.getException()));
        }
    }




    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        Log.i(TAG,"activit result "+ requestCode);

        if (requestCode == REQUEST_CODE_SIGN_IN) {
            // Make sure the request was successful
            if (data != null) {
                Log.i(TAG,"start handel sign in "+ requestCode);

                handleSignIn(data);
            } else {
                Log.i(TAG,"cancell service "+ requestCode);

                serviceListener.cancelled();
            }
        }
    }


}

