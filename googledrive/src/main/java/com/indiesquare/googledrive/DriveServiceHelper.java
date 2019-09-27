/**
 * Copyright 2018 Google LLC
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.indiesquare.googledrive;

import android.content.Context;
import android.util.Log;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.api.client.googleapis.media.*;

import java.security.MessageDigest;

import org.json.JSONObject;


import java.security.NoSuchAlgorithmException;


import java.io.FileNotFoundException;
import java.math.BigInteger;


/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */


class CheckSummer {
    private static final String TAG = "CheckSummer";

    public static String calculateChecksum(String path) {

        java.io.File updateFile = new java.io.File(path);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Exception while getting digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }
}

//custom listener for download progress
class DownloadProgressListener implements MediaHttpDownloaderProgressListener {
    private String TAG = "DownloadProgressListener";

    public CallbackInterface mCallback;
    public GoogleDrive mController;
    public String mPath;

    public void setCallback(GoogleDrive controller) {
        mController = controller;
        mCallback = mController.mCallback;
    }

    public void setPath(String path) {
        mPath = path;
    }


    @Override
    public void progressChanged(MediaHttpDownloader downloader) throws IOException {
        switch (downloader.getDownloadState()) {

            //Called when file is still downloading
            //ONLY CALLED AFTER A CHUNK HAS DOWNLOADED,SO SET APPROPRIATE CHUNK SIZE
            case MEDIA_IN_PROGRESS:
                mController.downloadProgress = downloader.getProgress();
                //Add code for showing progress
                break;
            //Called after download is complete
            case MEDIA_COMPLETE:
                try {


                    Log.i(TAG, "calculating checksum");
                    JSONObject obj = new JSONObject();
                    obj.put("type", "download");
                    obj.put("progress", downloader.getProgress());
                    obj.put("complete", true);
                    obj.toString();
                    mCallback.eventFired(null, obj.toString());

                    String checksum = CheckSummer.calculateChecksum(mPath);

                    Log.i(TAG, "checksum is " + checksum);

                    obj = new JSONObject();
                    obj.put("type", "download");
                    obj.put("progress", downloader.getProgress());
                    obj.put("checksum", checksum);
                    obj.put("complete", true);
                    obj.toString();
                    mCallback.eventFired(null, obj.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Add code for download completion
                break;
        }
    }
}


public class DriveServiceHelper {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;
    private GoogleDrive mController;
    private String TAG = "driveServiceHelper";

    public DriveServiceHelper(Drive driveService, GoogleDrive controller) {
        Log.i(TAG, "starting drive service");
        mDriveService = driveService;
        mController = controller;
    }


    public void downloadZipFile(String folderName, String fileName, Context context) {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {

                Log.i(TAG, "start zip file download");


                String path = context.getNoBackupFilesDir().getPath() + "/" + fileName;

                java.io.File tempFile = new java.io.File(path);
                if (tempFile.exists()) {

                    Log.i(TAG, "file already exists");


                    try {


                        Log.i(TAG, "calculating checksum");
                        JSONObject obj = new JSONObject();
                        obj.put("type", "download");
                        obj.put("progress", 1);
                        obj.put("complete", true);
                        obj.toString();
                        mController.mCallback.eventFired(null, obj.toString());

                        String checksum = CheckSummer.calculateChecksum(path);

                        Log.i(TAG, "checksum is " + checksum);

                        obj = new JSONObject();
                        obj.put("type", "download");
                        obj.put("progress", 1);
                        obj.put("checksum", checksum);
                        obj.put("complete", true);
                        obj.toString();
                        mController.mCallback.eventFired(null, obj.toString());
                    } catch (Exception e) {

                        e.printStackTrace();
                        return;
                    }

                    return;
                }

                try {

                    FileList result = mDriveService.files().list().setSpaces("drive").execute();

                    for (File aFile : result.getFiles()
                    ) {

                        Log.i(TAG, "found file with name " + aFile.getName() + " " + fileName);

                        if (aFile.getName().equals(fileName)) {

                            if (aFile.getTrashed() == null || aFile.getTrashed() == false) {
                                Log.i(TAG, "downloading file " + aFile.getName() + " " + fileName);

                                Log.i(TAG, "path is " + path);
                                OutputStream out = new FileOutputStream(path);
                                mController.downloadProgress = 0;
                                Log.i(TAG, "start download");

                                JSONObject obj = new JSONObject();
                                obj.put("type", "download");
                                obj.put("progress", 0);
                                obj.put("complete", false);
                                obj.toString();
                                mController.mCallback.eventFired(null, obj.toString());

                                Log.i(TAG, "sent start event");

                                Drive.Files.Get request = mDriveService.files().get(aFile.getId());
                                DownloadProgressListener lis = new DownloadProgressListener();
                                lis.setCallback(mController);
                                lis.setPath(path);
                                request.getMediaHttpDownloader().setProgressListener(lis).setChunkSize(1000000);
                                request.executeMediaAndDownloadTo(out);




                                return;
                            }


                        }

                    }


                    Log.i(TAG, "file not found");

                    mController.fileDownloadError("file not found");


                } catch (Exception e) {
                    e.printStackTrace();
                    mController.fileDownloadError(e.getLocalizedMessage());
                }
            }
        });
        t1.start();


    }

    public void downloadFile(String folderName, String fileName) {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {

                Log.i(TAG, "start file download");

                try {

                    FileList result = mDriveService.files().list().setSpaces("drive").execute();

                    for (File aFile : result.getFiles()
                    ) {

                        Log.i(TAG, "found file with name " + aFile.getName() + " " + fileName);
                        if (aFile.getName().contains(fileName)) {

                            // Stream the file contents to a String.
                            try (InputStream is = mDriveService.files().get(aFile.getId()).executeMediaAsInputStream();
                                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                                StringBuilder stringBuilder = new StringBuilder();
                                String line;

                                while ((line = reader.readLine()) != null) {
                                    stringBuilder.append(line);
                                }
                                String contents = stringBuilder.toString();

                                mController.fileDownloaded(contents);

                                return;
                            }


                        }

                    }


                    mController.fileDownloadError("file not found");


                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    mController.fileDownloadError(e.getLocalizedMessage());
                }
            }
        });
        t1.start();


    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile(String filePath) throws Exception {
        java.io.File fl = new java.io.File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    public void createFolderAndUpload(String folderName, String fileName, String data) {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {

                Log.i(TAG, "start folder " + folderName);

                try {

                    // Retrive the metadata as a File object.
                    FileList result = mDriveService.files().list()
                            .setQ("mimeType = '" + "application/vnd.google-apps.folder" + "' and name = '" + folderName + "' ")
                            .setSpaces("drive")
                            .execute();

                    Log.i(TAG, "loading folder");


                    for (File aFile : result.getFiles()
                    ) {

                        Log.i(TAG, "found folder with name " + aFile.getName());

                        if (aFile.getName().equals(folderName)) {
                            Boolean isTrashed = false;
                            if (aFile.getTrashed() != null) {
                                if (aFile.getTrashed() == true) {
                                    isTrashed = true;
                                }
                            }

                            if (isTrashed == false) {
                                Log.i(TAG, "is not trashed");

                                String folderId = aFile.getId();
                                createFile(fileName, data, folderId);
                                return;
                            } else {
                                Log.i(TAG, "is trashed");

                            }

                        }

                    }


                    Log.i(TAG, "folder not found, creating...");

                    List<String> root;

                    root = Collections.singletonList("root");

                    File metadata = new File()
                            .setParents(root)
                            .setMimeType("application/vnd.google-apps.folder")
                            .setName(folderName);

                    File googleFile = mDriveService.files().create(metadata).execute();
                    if (googleFile == null) {
                        throw new IOException("Null result when requesting file creation.");
                    }

                    Log.i(TAG, "folder made");

                    createFile(fileName, data, googleFile.getId());


                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    mController.fileUploadError();
                }
            }
        });
        t1.start();


    }

    public void createFile(String fileName, String content, String folderId) {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {

                try {


                    FileList result = mDriveService.files().list()
                            .setQ("name = '" + fileName + "' and mimeType ='" + "text/plain" + "'")
                            .setSpaces("drive")
                            .setFields("files(id, name,size,createdTime,modifiedTime,starred)")
                            .execute();
                    if (result.getFiles().size() > 0) {

                        for (File aFile : result.getFiles()
                        ) {

                            String aFileName = aFile.getName();
                            if (aFileName.equals(fileName)) {

                                Log.i(TAG, "deleting file");

                                mDriveService.files().delete(aFile.getId()).execute();
                                createFile(fileName, content, folderId);
                                return;
                            }

                        }
                    }

                    Log.i(TAG, "create and upload file");

                    List<String> root;


                    root = Collections.singletonList(folderId);


                    File metadata = new File()
                            .setParents(root)
                            .setMimeType("text/plain")
                            .setName(fileName);
                    ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);
                    Log.i(TAG, "create and upload file 2");

                    File googleFile = mDriveService.files().create(metadata, contentStream).execute();
                    if (googleFile == null) {
                        mController.fileUploadError();
                        throw new IOException("Null result when requesting file creation.");
                    }
                    Log.i(TAG, "create and upload file 3");
                    mController.fileUploaded();

                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    mController.fileUploadError();

                }
            }
        });
        t1.start();


    }

}