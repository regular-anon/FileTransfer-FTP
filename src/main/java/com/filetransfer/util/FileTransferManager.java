package com.filetransfer.util;

import com.filetransfer.util.files.FileStructureInstance;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileTransferManager
{
    private static ArrayList<FileTransferProcess> processes = new ArrayList<>();
    public static boolean isRunning = false;
    private static int maxBufferSize = 16_384; // maximum buffer size
    private static Thread transferThread;

    public static int getMaxBufferSize() {
        return maxBufferSize;
    }

    public synchronized static void setMaxBufferSize(int size) {
        maxBufferSize = size;
    }

    public synchronized static void addProcess(FileTransferProcess fp)
    {
        processes.add(fp);
        System.out.println("Added new process: " + fp.getRemotePath());
    }

    private FileTransferManager() {
        processes = new ArrayList<>();
    }

//    public static FileTransferProcess getProcessById(int id) {
//        for(int i = 0;i < processes.size();++i) {
//            if(processes.get(i).getId() == id)
//                return processes.get(i);
//        }
//        return null;
//    }

//    public static void startClient() throws IOException {
////        ftp = new FTPClient();
////        ftp.connect(Client.getServer(), Client.getPort());
////        int reply = ftp.getReplyCode();
////        if (!FTPReply.isPositiveCompletion(reply)) {
////            ftp.disconnect();
////            throw new IOException("Exception in connecting to FTP Server");
////        }
////
////        ftp.login(Client.getCredentials()[0], Client.getCredentials()[1]);
////        if (ftp.getReplyCode() == 500)
////            throw new IOException("Invalid credentials!");
////        ftp.setFileType(FTP.BINARY_FILE_TYPE);
////        ftp.enterLocalPassiveMode();
//        ftp = Client.getFTPClient();
//    }

    public synchronized static void startTransfer() {
        if(processes.size() == 0)
            return;
        transferThread = new Thread(() -> {
           run();
        });
        transferThread.start();
    }

    public static synchronized void stopClient() throws Exception {
        try {
            pauseProcesses();
        }
        finally {
            isRunning = false;
        }
    }

    private static synchronized void pauseProcesses()
    {
        for(int i = 0;i < processes.size();++i) {
            processes.get(i).cancelTransfer();
        }
    }

    public static synchronized void resumeProcesses() {
        if(transferThread.isAlive())
            return;
        startTransfer();
    }

    public synchronized static boolean isRunning()
    {
        return isRunning;
    }


    public synchronized static <T> void insert(List<T> ls, int index, T obj) {
        if(index == ls.size())
            ls.add(obj);
        else {
            T temp = ls.get(index);
            ls.set(index, obj);
            insert(ls, ++index, temp);
        }
    }

    public synchronized static int getProcessCount() {
        return processes.size();
    }

    public synchronized static FileTransferProcess getProcess(int index) {
        return processes.get(index);
    }

    public synchronized static void removeProcess(FileTransferProcess fp) {
        processes.remove(fp);
    }

    public static void runOld() {
        isRunning = true;
        System.out.println("Transfer thread started!");
        for(int i = 0;i < getProcessCount(); ++i) {
            FileTransferProcess fp = getProcess(i);
            System.out.println("File: " + fp.getRemotePath());
            if(fp.failedTries() > 2) {
                removeProcess(fp);
                continue;
            }
            if(fp.isCancelled()) {
                System.out.println("Cancelling process");
                continue;
            }
            if(fp.getClient().getTransferState()) {
                System.out.println("Cancelling from client class");
                continue;
            }
            if(fp.isDownload()) {
                System.out.println("Type: download");
                try {
                    System.out.println("Started download!");
                    download(fp);
                    removeProcess(fp);
                } catch (FileTransferCancelled fileTransferCancelled) {
                    fileTransferCancelled.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    fp.incrementTries();
//                    insert(processes, getProcessCount() - 1, fp);
                    removeProcess(fp);
                    addProcess(fp);
                    i ++;
                    continue;
                }
            }
            else {
                System.out.println("Type: upload");
                try {
                    System.out.println("Started upload");
                    upload(fp);
                    removeProcess(fp);
                } catch (FileTransferCancelled fileTransferCancelled) {
                    fileTransferCancelled.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    fp.incrementTries();
//                    insert(processes, 0, fp);
                    removeProcess(fp);
                    addProcess(fp);
                    i ++;
                    continue;
                }
            }
        }
        System.out.println("Finished transfering files.");
        isRunning = false;
//        boolean allCancelled = false;
//        int index = 0;
//        while (!allCancelled) {
//            allCancelled = true;
//            while (getProcessCount() > 0) {
//                FileTransferProcess fp = getProcess(index);
//                if (fp.failedTries() > 2)
//                    removeProcess(fp);
//                if (!fp.isCancelled())
//                    allCancelled = false;
//                else {
//                    index++;
//                    continue;
//                }
//                if (fp.getClient().getTransferState()) {
//                    index++;
//                    continue;
//                }
//                if(fp.isDownload()) {
//                System.out.println("Type: download");
//                try {
//                    System.out.println("Started download!");
//                    download(fp);
//                    removeProcess(fp);
//                } catch (FileTransferCancelled fileTransferCancelled) {
//                    fileTransferCancelled.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    fp.incrementTries();
////                    insert(processes, getProcessCount() - 1, fp);
//                    removeProcess(fp);
//                    addProcess(fp);
//                    continue;
//                }
//            }
//            else {
//                System.out.println("Type: upload");
//                try {
//                    System.out.println("Started upload");
//                    upload(fp);
//                    removeProcess(fp);
//                } catch (FileTransferCancelled fileTransferCancelled) {
//                    fileTransferCancelled.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    fp.incrementTries();
////                    insert(processes, 0, fp);
//                    removeProcess(fp);
//                    addProcess(fp);
//                    continue;
//                }
//            }
//            }
//        }
        System.out.println("FileTransferManager thread stopped!");
    }

    //TODO: remove process if failedtries > 3 in catch block
    public static void run() {
        isRunning = true;

        boolean idleLoop = false;
        while(!idleLoop && getProcessCount() > 0) {
            idleLoop = true;
            for(int i = 0;i < getProcessCount(); ++i) {
                FileTransferProcess fp = getProcess(i);
                if (fp.isCancelled()) {
                    System.out.println("Cancelling process");
                    return;
                }
                if(fp.getClient().getTransferState()) {
                    System.out.println("Cancelling from client class.");
                    return;
                }
                //Found a process that can be transferred
                idleLoop = false;
                if(fp.isDownload()) {
                    System.out.println("Type: download");
                    try {
                        System.out.println("Started download!");
                        download(fp);
                        removeProcess(fp);
                    } catch (FileTransferCancelled fileTransferCancelled) {
                        fileTransferCancelled.printStackTrace();
                        removeProcess(fp);
                    } catch (IOException e) {
                        e.printStackTrace();
                        fp.incrementTries();
                        removeProcess(fp);
                        addProcess(fp);
                        i ++;
                        continue;
                    }
                }
                else {
                    System.out.println("Type: upload");
                    try {
                        System.out.println("Started upload");
                        upload(fp);
                        removeProcess(fp);
                    } catch (FileTransferCancelled fileTransferCancelled) {
                        fileTransferCancelled.printStackTrace();
                        removeProcess(fp);
                    } catch (IOException e) {
                        e.printStackTrace();
                        fp.incrementTries();
                        removeProcess(fp);
                        addProcess(fp);
                        i ++;
                        continue;
                    }
                }
            }
        }
        isRunning = false;
    }

    private static void upload(FileTransferProcess fp) throws IOException, FileTransferCancelled {
        FileInputStream fis = null;
        System.out.println("File name: " + fp.getFile().getName());
//        ProgressBar pb = fp.getProgressBar();
        try {
            fis = new FileInputStream(fp.getFile());
            BufferedOutputStream outputStream = new BufferedOutputStream(fp.getClient().getFTPClient().storeFileStream(fp.getRemotePath()));
            long uploadedSize = fp.getProgress();
            long fileSize = fp.getFile().length();
            fis.skip(uploadedSize);
            while (uploadedSize < fileSize && !fp.isCancelled()) {
                byte[] buffer;
                if(fileSize - uploadedSize < maxBufferSize)
                    buffer = new byte[(int)(fileSize - uploadedSize)];
                else
                    buffer = new byte[maxBufferSize];
                fis.read(buffer, 0, buffer.length);
                outputStream.write(buffer);
                uploadedSize += buffer.length;
                System.out.println("Uploaded bytes: " + uploadedSize);
                fp.updateProgress(uploadedSize);
//                if(pb != null) {
//                    Platform.runLater(() -> {
//                        pb.setProgress((float)fp.getProgress() / (float)fp.getRemoteFileSize());
//                    });
//                }
            }
            if(uploadedSize < fp.getRemoteFileSize()) {
                if(!fp.getClient().getTransferState())
                    throw new FileTransferCancelled("File transfer was cancelled by client class");
                else if(fp.isCancelled())
                    throw new FileTransferCancelled("File transfer was cancelled individually for this process");
            }
            if (outputStream != null)
                outputStream.close();
            if (!fp.getClient().getFTPClient().completePendingCommand())
                throw new IOException("IOException in file upload!");
//            if (uploadedSize < fileSize)
//                throw new RuntimeException("File upload cancelled!");
            System.out.println("Closed outputStream...");

            System.out.println("Stored file.");

            System.out.println("Updating directory...");
            FileStructureInstance fsi = new FileStructureInstance(fp.getRemotePath());
            fsi.setSize(fp.getFile().length());//fp.getRemoteFileSize());
            fsi.setName(fp.getFile().getName());//fp.getRemotePath().substring(fp.getRemotePath().lastIndexOf('\\')));
            fsi.setLastModified(fp.getFile().lastModified());
            fp.getClient().getFileStructure().addFile(fsi, fp.getClient());
//            DirectoryInstance dir = (DirectoryInstance) FileStructure.search(fp.getRemotePath().substring(0, fp.getRemotePath().lastIndexOf('/')));
//            FileStructure.assignContents(dir, Client.getFTPClient());

//            if (FileStructure.currentDirectory.getPath().equals(dir.getPath())) {
//                FileTransferController.instance.showDirectory(dir);
//            }
        }
        finally {
            if(fis != null)
                fis.close();
        }
    }
    //When using retrieveFileStream() method, close the input stream and then call completePendingCommand()
    private static void download(FileTransferProcess fp) throws IOException, FileTransferCancelled {
        FileOutputStream fos = null;
//        ProgressBar pb = fp.getProgressBar();
        try {
            fos = new FileOutputStream(fp.getFile());
            BufferedInputStream inputStream = new BufferedInputStream(fp.getClient().getFTPClient().retrieveFileStream(fp.getRemotePath()));
            int tempByte = inputStream.read();
            long downloadedSize = fp.getProgress();
            inputStream.skip(downloadedSize);
            while(tempByte != -1 && !fp.isCancelled() && !fp.getClient().getTransferState()) {
                int available = inputStream.available();
                byte[] buffer = new byte[Math.min(maxBufferSize, available) + 1];
                buffer[0] = (byte)tempByte;
                inputStream.read(buffer, 1, buffer.length - 1);
                tempByte = inputStream.read();
                fos.write(buffer);
                downloadedSize += buffer.length;
                fp.updateProgress(downloadedSize);
//                if(pb != null) {
//                    Platform.runLater(() -> {
//                        pb.setProgress((float)fp.getProgress() / (float)fp.getRemoteFileSize());
//                    });
//                }
            }
            System.out.println("Total bytes read: " + downloadedSize);
            System.out.println("File size: " + fp.getFile().length());

//            try {
//                System.out.println("Before abort");
////                boolean flag = ftp.abort();
////                System.out.println("Flag: " + flag);
//            }
//            catch (Exception e) {
//                System.out.println(e.getMessage());
//                e.printStackTrace();
//            }
            if(downloadedSize < fp.getRemoteFileSize()) {
                if(fp.getClient().getTransferState())
                    throw new FileTransferCancelled("File transfer was cancelled by client class");
                else if(fp.isCancelled())
                    throw new FileTransferCancelled("File transfer was cancelled individually for this process");
            }
            if(inputStream != null)
                inputStream.close();
            if(!fp.getClient().getFTPClient().completePendingCommand())
                throw new IOException ("IOException in file download!");
//            if(downloadedSize < fp.getFile().length())
//                throw new RuntimeException("File download cancelled!");
        }
        finally {
            if(fos != null)
                fos.close();
        }
    }
}
