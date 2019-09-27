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

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.nio.channels.FileChannel;
import java.io.File;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;



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
    public double  unzipProgress;
    public double  downloadProgress;
    public CallbackInterface mCallback;
    public GoogleDrive(final Activity currentActivity){

        activity = currentActivity;
       googleDriveService = new GoogleDriveService(activity);
       googleDriveService.serviceListener = this;


    }
    public double getDownloadProgress(){
        return downloadProgress;
    }

    public double setDownloadProgress(){
        return downloadProgress;
    }
    public boolean isWifi(){

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(activity.getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isConnected();
    }

    public void unpackZip(String zipname, CallbackInterface callback)
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String network = "";

                unzipProgress = 0;

                if(zipname.equals("testnet3.zip")){
                    network = "testnet3";

                }else if(zipname.equals("mainnet.zip")){
                    network = "mainnet";
                }


                Log.i(TAG,"network is "+network+" "+zipname);

                String path = activity.getApplicationContext().getNoBackupFilesDir().getPath() + "/";
                String outputPath = path + "bitcoinDirec/";

                if(network.equals("testnet3")) {
                    File directory = new File(outputPath +  network);
                    if (!directory.exists()) {
                        directory.mkdir();
                        Log.i(TAG, "making directory " + outputPath +  network);
                    }
                }


                FileInputStream is;
                ZipInputStream zis;
                try {
                    String filename;
                    Log.i(TAG,"file info "+path + zipname);
                    java.io.File file = new java.io.File(path + zipname);

                    is = new FileInputStream(file.getCanonicalFile());

                    FileChannel channel = is.getChannel();

                    zis = new ZipInputStream(new BufferedInputStream(is));

                    ZipEntry ze;
                    byte[] buffer = new byte[1024];
                    int count = 0;

                    boolean foundValidFile = false;

                    try {

                        foundValidFile = true;
                        double progress = (double)channel.position() / (double)file.length();
                        Log.i(TAG, "unzipProgress " + progress + channel.position() + "/" + file.length());


                        JSONObject obj = new JSONObject();
                        obj.put("type", "unzip");
                        obj.put("progress", unzipProgress);
                        obj.put("complete", false);

                        callback.eventFired(null, obj.toString());
                    } catch (Exception e) {
                        Log.e(TAG,"progress error");
                        e.printStackTrace();

                    }

                    while ((ze = zis.getNextEntry()) != null) {
                        filename = ze.getName();

                        // Need to create directories if not exists, or
                        // it will generate an Exception...

                        String fullOutputPath =  outputPath + filename;

                        if(network.equals("mainnet")) {
                            fullOutputPath = fullOutputPath.replace("mainnet/", "");
                        }else {

                            if (!filename.startsWith(network + "/")) {
                                Log.i(TAG, "file is not valid");
                                continue;
                            }
                        }
                        Log.i(TAG, "unzipping file "+filename+"   to   " +fullOutputPath );
                        if (ze.isDirectory()) {
                            java.io.File fmd = new java.io.File(fullOutputPath );
                            fmd.mkdirs();
                            continue;
                        }

                        FileOutputStream fout = new FileOutputStream(fullOutputPath );

                        while ((count = zis.read(buffer)) != -1) {
                            unzipProgress = (double)channel.position() / (double)file.length();

/*
                            try {

                                double progress = (double)channel.position() / (double)file.length();
                                Log.i(TAG, "unzipProgress " + progress + channel.position() + "/" + file.length());


                                JSONObject obj = new JSONObject();
                                obj.put("type", "unzip");
                                obj.put("progress", progress);
                                obj.put("complete", false);

                                callback.eventFired(null, obj.toString());
                            } catch (Exception e) {
                                Log.e(TAG,"progress error");
                                e.printStackTrace();

                            }*/

                            fout.write(buffer, 0, count);
                        }

                        fout.close();
                        zis.closeEntry();
                    }

                    zis.close();
                    try {


                        if(unzipProgress > 0) {
                            JSONObject obj = new JSONObject();
                            obj.put("type", "unzip");
                            obj.put("progress", 1);
                            obj.put("complete", true);

                            callback.eventFired(null, obj.toString());
                        }else{
                            callback.eventFired("file not found", null);
                        }
                    } catch (Exception e) {
                        Log.e(TAG,"json error");
                        e.printStackTrace();

                    }
                } catch (IOException e) {
                    Log.e(TAG,"file error");
                    e.printStackTrace();

                }

            }
        });
        thread.start();
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
                        activity.getApplicationContext(), Collections.singleton(DriveScopes.DRIVE));
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
