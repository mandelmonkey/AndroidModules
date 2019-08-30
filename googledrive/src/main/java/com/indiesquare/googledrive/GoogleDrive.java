package com.indiesquare.googledrive;

import android.accounts.Account;
import android.app.Activity;
import android.util.Log;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import java.util.Collections;

public class GoogleDrive extends Activity implements ServiceListener {


    private static DriveServiceHelper mDriveServiceHelper = null;
    String TAG = "google drive";

    private GoogleDriveService googleDriveService;
    public Activity activity;

    public String folderName;
    public String fileName;
    public String fileData;
    public String appName;
    public boolean isOpen;
    public CallbackInterface mCallback;
    public GoogleDrive(final Activity currentActivity){

        activity = currentActivity;
       googleDriveService = new GoogleDriveService(activity);
       googleDriveService.serviceListener = this;


    }


    public void uploadFile(String theAppName, String theFolderName, String theFilename, String theData, CallbackInterface callback){

        mCallback = callback;
        folderName = theFolderName;
        fileName = theFilename;
        fileData = theData;
        appName = theAppName;

        Log.i(TAG,"start drive");
        if(googleDriveService.checkLoginStatus() == false) {
            if(isOpen == false) {
                googleDriveService.auth();
            }
            isOpen = true;
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Log.i(TAG,"check logged in a bit later");
                            uploadFile(theAppName, theFolderName, theFilename, theData, callback);
                        }
                    },
                    5000
            );
        }else{
            isOpen = false;
            uploadFileStart();
        }



    }

    public void downloadFile(String theAppName, String theFolderName, String theFilename, CallbackInterface callback){

        mCallback = callback;
        folderName = theFolderName;
        fileName = theFilename;
        appName = theAppName;

        Log.i(TAG,"start drive");
        if(googleDriveService.checkLoginStatus() == false) {
            if(isOpen == false) {
                googleDriveService.auth();
            }
            isOpen = true;
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Log.i(TAG,"check logged in a bit later");
                            downloadFile(theAppName, theFolderName, theFilename, callback);
                        }
                    },
                    5000
            );
        }else{
            isOpen = false;
            downloadFileStart();
        }



    }

    public void downloadZipFile(String theAppName, String theFolderName, String theFilename, CallbackInterface callback){

        mCallback = callback;
        folderName = theFolderName;
        fileName = theFilename;
        appName = theAppName;

        Log.i(TAG,"start drive");
        if(googleDriveService.checkLoginStatus() == false) {
            if(isOpen == false) {
                googleDriveService.auth();
            }
            isOpen = true;
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Log.i(TAG,"check logged in a bit later");
                            downloadZipFile(theAppName, theFolderName, theFilename, callback);
                        }
                    },
                    5000
            );
        }else{
            isOpen = false;
            downloadZipFileStart();
        }



    }

    public void linkGoogleDrive(CallbackInterface callback){

        mCallback = callback;


        Log.i(TAG,"start drive");
        if(googleDriveService.checkLoginStatus() == false) {
            if(isOpen == false) {
                googleDriveService.auth();
            }
            isOpen = true;
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Log.i(TAG,"check logged in a bit later");
                            linkGoogleDrive(callback);
                        }
                    },
                    5000
            );
        }else{
            isOpen = false;
            loggedIn();
        }



    }

    public void fileDownloaded(String data){
        mCallback.eventFired(null,data);

    }

    public void fileDownloadError(String error){
        mCallback.eventFired(error,null);

    }

    public void fileUploaded(){
        mCallback.eventFired(null,"file uploaded");

    }

    public void fileUploadError(){
        mCallback.eventFired("file upload error",null);

    }

    public void uploadFileStart(){

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);

        Log.i(TAG,"account display name:"+account.getDisplayName());
        Log.i(TAG,"account scopes:"+account.getGrantedScopes().toString());

        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        activity.getApplicationContext(), Collections.singleton(DriveScopes.DRIVE_FILE));

       Account userAccount = account.getAccount();

       if(userAccount == null){

           mCallback.eventFired("user account not found",null);

           return;
       }
        Log.i(TAG,"account name:"+userAccount.name);
        Log.i(TAG,"account type:"+userAccount.type);

        credential.setSelectedAccount(userAccount);


        com.google.api.services.drive.Drive googleDriveService =
                new com.google.api.services.drive.Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName(appName)
                        .build();


        mDriveServiceHelper = new DriveServiceHelper(googleDriveService,this);

        mDriveServiceHelper.createFolderAndUpload(folderName,fileName,fileData);

    }

    public void downloadZipFileStart(){

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);

        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        activity.getApplicationContext(), Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());


        com.google.api.services.drive.Drive googleDriveService =
                new com.google.api.services.drive.Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName(appName)
                        .build();


        mDriveServiceHelper = new DriveServiceHelper(googleDriveService,this);

        mDriveServiceHelper.downloadZipFile(folderName,fileName,activity.getApplicationContext());

    }

    public void downloadFileStart(){

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);

        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        activity.getApplicationContext(), Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());


        com.google.api.services.drive.Drive googleDriveService =
                new com.google.api.services.drive.Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName(appName)
                        .build();


        mDriveServiceHelper = new DriveServiceHelper(googleDriveService,this);

        mDriveServiceHelper.downloadFile(folderName,fileName);

    }

    public void loggedIn(){
        Log.i(TAG,"logged in");
        mCallback.eventFired(null,"linked");

    }

    public void signOut(){
        googleDriveService.signOut();
    }

    public void cancelled(){

        Log.i(TAG,"cancelled");


    }

    public void handleError(Exception exception){

        Log.e(TAG,"exception "+exception.getLocalizedMessage());


    }


}
