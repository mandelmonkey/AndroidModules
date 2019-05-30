/**
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.indiesquare.googledrive;

import android.support.annotation.Nullable;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.v4.util.Pair;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.AbstractInputStreamContent;
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

/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
public class DriveServiceHelper {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;
    private GoogleDrive mController;
    private String TAG = "driveServiceHelper";

    public DriveServiceHelper(Drive driveService,GoogleDrive controller) {
        Log.i(TAG,"starting drive service");
        mDriveService = driveService;
        mController = controller;
    }

    public void downloadFile(String folderName,String fileName) {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {

                Log.i(TAG,"start file download");

                try {

                            FileList result = mDriveService.files().list().setSpaces("drive").execute();

                            for (File aFile:result.getFiles()
                            ) {

                                Log.i(TAG, "found file with name " + aFile.getName() + " " + fileName);
                                if(aFile.getName().contains(fileName)) {

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


                }
                catch(Exception e){
                    Log.e(TAG,e.getLocalizedMessage());
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

    public static String getStringFromFile (String filePath) throws Exception {
        java.io.File fl = new java.io.File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    public void createFolderAndUpload(String folderName,String fileName, String data) {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {

                Log.i(TAG,"start folder");

                try {

                    // Retrive the metadata as a File object.
                    FileList result = mDriveService.files().list()
                            .setQ("mimeType = '" + "application/vnd.google-apps.folder" + "' and name = '" + folderName + "' ")
                            .setSpaces("drive")
                            .execute();

                    for (File aFile:result.getFiles()
                         ) {

                        Log.i(TAG,"found folder with name "+aFile.getName());

                        if(aFile.getName().equals(folderName)){
                            Boolean isTrashed = false;
                            if(aFile.getTrashed() != null){
                                if(aFile.getTrashed() == true){
                                    isTrashed = true;
                                }
                            }

                            if(isTrashed == false) {
                                Log.i(TAG,"is not trashed");

                                String folderId = aFile.getId();
                                createFile(fileName, data, folderId);
                                return;
                            }else{
                                Log.i(TAG,"is trashed");

                            }

                        }

                    }


                        Log.i(TAG,"folder not found, creating...");

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

                        Log.i(TAG,"folder made");

                        createFile(fileName,data,googleFile.getId());



                }
                catch(Exception e){
                    Log.e(TAG,e.getLocalizedMessage());
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

                        for (File aFile:result.getFiles()
                        ) {

                            String aFileName = aFile.getName();
                           if(aFileName.equals(fileName)){

                               Log.i(TAG,"deleting file");

                               mDriveService.files().delete(aFile.getId()).execute();
                               createFile(fileName,content,folderId);
                               return;
                           }

                        }
                    }

                    Log.i(TAG,"create and upload file");

                    List<String> root;


                        root = Collections.singletonList(folderId);


                    File metadata = new File()
                            .setParents(root)
                            .setMimeType("text/plain")
                            .setName(fileName);
                    ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);
                    Log.i(TAG,"create and upload file 2");

                    File googleFile = mDriveService.files().create(metadata, contentStream).execute();
                    if (googleFile == null) {
                        mController.fileUploadError();
                        throw new IOException("Null result when requesting file creation.");
                    }
                    Log.i(TAG,"create and upload file 3");
                    mController.fileUploaded();

                }
                catch (Exception e){
                    Log.e(TAG,e.getLocalizedMessage());
                    mController.fileUploadError();

                }
            }
        });
        t1.start();



    }




/*
    public Task<GoogleDriveFileHolder> searchFolder(String folderName) {
        return Tasks.call(mExecutor, () -> {

            // Retrive the metadata as a File object.
            FileList result = mDriveService.files().list()
                    .setQ("mimeType = '" + "application/vnd.google-apps.folder" + "' and name = '" + folderName + "' ")
                    .setSpaces("drive")
                    .execute();
            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
            if (result.getFiles().size() > 0) {
                googleDriveFileHolder.setId(result.getFiles().get(0).getId());
                googleDriveFileHolder.setName(result.getFiles().get(0).getName());

            }
            return googleDriveFileHolder;
        });
    }
    public Task<GoogleDriveFileHolder> createFolder(String folderName, @Nullable String folderId) {
        return Tasks.call(mExecutor, () -> {

            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();

            List<String> root;
            if (folderId == null) {
                root = Collections.singletonList("root");
            } else {

                root = Collections.singletonList(folderId);
            }
            File metadata = new File()
                    .setParents(root)
                    .setMimeType("application/vnd.google-apps.folder")
                    .setName(folderName);

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }
            googleDriveFileHolder.setId(googleFile.getId());
            return googleDriveFileHolder;
        });
    }


    public Task<Void> downloadFile(java.io.File targetFile, String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            OutputStream outputStream = new FileOutputStream(targetFile);
            mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            return null;
        });
    }

    public Task<Void> deleteFolderFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            if (fileId != null) {
                mDriveService.files().delete(fileId).execute();
            }

            return null;

        });
    }
    public Task<GoogleDriveFileHolder> uploadFile(File googleDiveFile, AbstractInputStreamContent content) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            File fileMeta = mDriveService.files().create(googleDiveFile, content).execute();
            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
            googleDriveFileHolder.setId(fileMeta.getId());
            googleDriveFileHolder.setName(fileMeta.getName());
            return googleDriveFileHolder;
        });
    }


    public Task<GoogleDriveFileHolder> createTextFile(String fileName, String content, @Nullable String folderId) {
        return Tasks.call(mExecutor, () -> {


            List<String> root;
            if (folderId == null) {
                root = Collections.singletonList("root");
            } else {

                root = Collections.singletonList(folderId);
            }

            File metadata = new File()
                    .setParents(root)
                    .setMimeType("text/plain")
                    .setName(fileName);
            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

            File googleFile = mDriveService.files().create(metadata, contentStream).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }
            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
            googleDriveFileHolder.setId(googleFile.getId());
            return googleDriveFileHolder;
        });
    }

    public Task<GoogleDriveFileHolder> searchFile(String fileName, String mimeType) {
        return Tasks.call(mExecutor, () -> {

            FileList result = mDriveService.files().list()
                    .setQ("name = '" + fileName + "' and mimeType ='" + mimeType + "'")
                    .setSpaces("drive")
                    .setFields("files(id, name,size,createdTime,modifiedTime,starred)")
                    .execute();
            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
            if (result.getFiles().size() > 0) {

                googleDriveFileHolder.setId(result.getFiles().get(0).getId());
                googleDriveFileHolder.setName(result.getFiles().get(0).getName());
                googleDriveFileHolder.setModifiedTime(result.getFiles().get(0).getModifiedTime());
                googleDriveFileHolder.setSize(result.getFiles().get(0).getSize());
            }


            return googleDriveFileHolder;
        });
    }




    public Task<String> createFile() {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType("text/plain")
                    .setName("Untitled file");

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }


    public Task<Pair<String, String>> readFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            File metadata = mDriveService.files().get(fileId).execute();
            String name = metadata.getName();

            // Stream the file contents to a String.
            try (InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String contents = stringBuilder.toString();

                return Pair.create(name, contents);
            }
        });
    }


    public Task<Void> saveFile(String fileId, String name, String content) {
        return Tasks.call(mExecutor, () -> {
            // Create a File containing any metadata changes.
            File metadata = new File().setName(name);

            // Convert content to an AbstractInputStreamContent instance.
            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

            // Update the metadata and contents.
            mDriveService.files().update(fileId, metadata, contentStream).execute();
            return null;
        });
    }


    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, () ->
                mDriveService.files().list().setSpaces("drive").execute());
    }


    public Intent createFilePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        return intent;
    }


    public Task<Pair<String, String>> openFileUsingStorageAccessFramework(
            ContentResolver contentResolver, Uri uri) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the document's display name from its metadata.
            String name;
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    name = cursor.getString(nameIndex);
                } else {
                    throw new IOException("Empty cursor returned for file.");
                }
            }

            // Read the document's contents as a String.
            String content;
            try (InputStream is = contentResolver.openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                content = stringBuilder.toString();
            }

            return Pair.create(name, content);
        });
    }
    */
}