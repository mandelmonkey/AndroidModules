package com.indiesquare.googledrive;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.auth.oauth2.Credential;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import com.google.api.services.drive.model.File;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

public class GoogleDrive extends Activity implements ServiceListener {

    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static DriveServiceHelper mDriveServiceHelper = null;
    String TAG = "google drive";

    private GoogleDriveService googleDriveService;
    private Activity activity;

    public String folderName;
    public String fileName;
    public String fileData;
    public boolean isOpen;
    public CallbackInterface mCallback;
    public GoogleDrive(final Activity currentActivity){

        activity = currentActivity;
       googleDriveService = new GoogleDriveService(activity);
       googleDriveService.serviceListener = this;


    }


    public void uploadFile(String theFolderName, String theFilename, String theData, CallbackInterface callback){

        mCallback = callback;
        folderName = theFolderName;
        fileName = theFilename;
        fileData = theData;

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
                            uploadFile(theFolderName, theFilename, theData,callback);
                        }
                    },
                    5000
            );
        }else{
            isOpen = false;
            uploadFileStart();
        }



    }

    public void downloadFile(String theFolderName, String theFilename, CallbackInterface callback){

        mCallback = callback;
        folderName = theFolderName;
        fileName = theFilename;

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
                            downloadFile(theFolderName, theFilename, callback);
                        }
                    },
                    5000
            );
        }else{
            isOpen = false;
            downloadFileStart();
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

        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        activity.getApplicationContext(), Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());


        com.google.api.services.drive.Drive googleDriveService =
                new com.google.api.services.drive.Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName("Pebble")
                        .build();


        mDriveServiceHelper = new DriveServiceHelper(googleDriveService,this);

        mDriveServiceHelper.createFolderAndUpload(folderName,fileName,fileData);

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
                        .setApplicationName("Pebble")
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
