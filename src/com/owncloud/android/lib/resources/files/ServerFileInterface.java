package com.owncloud.android.lib.resources.files;

public interface ServerFileInterface {
    
    String getFileName();
    
    String getMimeType();
    
    String getRemotePath();
    
    String getLocalId();
    
    String getRemoteId();
    
    boolean isFavorite();
    
    boolean isFolder();
    
    long getFileLength();
}
