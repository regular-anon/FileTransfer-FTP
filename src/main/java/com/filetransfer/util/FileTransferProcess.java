package com.filetransfer.util;

import com.filetransfer.Client;
import javafx.scene.control.ProgressBar;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;

public class FileTransferProcess
{

    private boolean download;
    private File file;
    private boolean done, cancelled = false;
    private int failedAttempts;
    private long transferredBytes = 0L;
    private ProgressBar pb;
    private long remoteFileSize;
    private Client client;
    private String remotePath;
//    private int id;

    public void setProgressBar(ProgressBar pb) {
        this.pb = pb;
    }

    public Client getClient() {
        return client;
    }

    public ProgressBar getProgressBar() {
        return pb;
    }

    public FileTransferProcess(boolean bool, File f, String remotePath, long remoteFileSize, Client client) {
        this.download = bool;
        this.file = f;
        done = false;
        failedAttempts = 0;
        this.remotePath = remotePath;
        this.remoteFileSize = remoteFileSize;
        this.client = client;
    }

    public void updateProgress(long l) {
        transferredBytes = l;
    }

    public long getRemoteFileSize() {
        return remoteFileSize;
    }

    public long getProgress() {
        return transferredBytes;
    }

//    public void setId(int id) {
//        this.id = id;
//    }

//    public int getId() {
//        return id;
//    }

    public boolean isDone() {
        return done;
    }

    public int failedTries() {
        return failedAttempts;
    }

    public void incrementTries() {
        failedAttempts++;
    }

    public boolean isDownload() {
        return download;
    }

    public File getFile() {
        return file;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancelTransfer() {
        cancelled = true;
    }
}
