package com.indiesquare.googledrive;

import java.io.File;

public interface ServiceListener {


    public void loggedIn();
    public void fileDownloaded(String data);
    public void fileDownloadError(String error);
    public void fileUploaded();
    public void fileUploadError();
    public void cancelled();
    public void handleError(Exception exception);


}
