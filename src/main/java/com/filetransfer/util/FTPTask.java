package com.filetransfer.util;

import org.apache.commons.net.ftp.FTPClient;
import java.io.IOException;

public abstract class FTPTask
{
    public Object parameter;
    boolean hasParameter = false;
    public abstract void doTask(FTPClient client) throws IOException;
    public abstract void onError(FTPClient client);
    public abstract void onFinished(FTPClient client);
    public abstract void onSuccessful(FTPClient client);

    public FTPTask(Object parameter) {
        this.parameter = parameter;
        hasParameter = true;
    }

    public FTPTask() { }
}
