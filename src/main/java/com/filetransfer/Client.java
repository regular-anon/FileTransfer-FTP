package com.filetransfer;

import com.filetransfer.util.FTPTask;
import com.filetransfer.util.FileStructure;
import com.filetransfer.util.FileTransferManager;
import com.filetransfer.util.FileTransferProcess;
import com.filetransfer.util.files.FileInstance;
import fxcontrollers.FileTransferLogsController;
import fxcontrollers.MainFXMLController;
import fxcontrollers.TransferStageController;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;


public class Client
{
    //Credentials
    private String server;
    private int port;
    private String user;
    private String password;
    //=================

    private FTPClient ftp;
    private PrintWriter pw;
    private boolean connected = false;
    private String downloadsPath = System.getProperty("user.home") + "/Downloads";
    private String logs = "";
    private FileStructure fileStructure;
    private final Client instance = this;

    private Queue<FTPTask> commandQueue = new LinkedList<>();
    private boolean commandCarrierRunning = false;
    private Thread commandCarrier = new Thread() {
        public void run() {
            while(commandQueue.size() > 0) {
                FTPTask nextTask  = commandQueue.poll();
                try {
                    nextTask.doTask(ftp);
                    nextTask.onSuccessful(ftp);
                }
                catch(Exception e) {
                    nextTask.onError(ftp);
                    e.printStackTrace();
                }
                finally {
                    nextTask.onFinished(ftp);
                }
            }
            commandCarrierRunning = false;
        }
    };
    private boolean pausedTransfers = false;

    public void setTransferState(boolean state) {
        pausedTransfers = state;
    }

    public boolean getTransferState() {
        return pausedTransfers;
    }

    public FileStructure getFileStructure() {
        return fileStructure;
    }

    public Client(String hostname, String username, String password, int port) throws IOException {
        this.server = hostname;
        this.user = username;
        this.password = password;
        this.port = port;

        ftp = new FTPClient();
        fileStructure = new FileStructure(ftp);
    }

    public void addTask(FTPTask task) {
        commandQueue.add(task);
        if(!commandCarrierRunning) {
            commandCarrierRunning = true;
            new Thread(commandCarrier).start();
        }
    }

    public String getDownloadsPath() {
        return downloadsPath;
    }

    public String[] getCredentials() {
        return new String[] {user, password, server, port + ""};
    }

    public int getPort() {
        return port;
    }

    public String getServer() {
        return server;
    }

    public void setCredentials(String server, int port, String user, String password) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public String getLogs() {
        return logs;
    }

    public void connect() throws IOException {
            ftp.setControlEncoding("UTF-8");
        try {
            File f = new File("src/main/resources/Other/Logs/" + user + "@" + server + ".txt");
            if(!f.exists()) {
                System.out.println(f.getPath());
                File tempFile = new File(f.getPath().substring(0, f.getPath().lastIndexOf('\\')));
                System.out.println(tempFile.getPath());
                tempFile.mkdirs();
                f.createNewFile();
            }
            pw = new PrintWriter(new FileWriter(f, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
            OutputStream os = new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    pw.write(b);
                    pw.flush();
                    logs += (char)b;
                    if(MainFXMLController.instance.currentTabManager == null)
                        return;
                    if(instance == MainFXMLController.instance.currentTabManager.getClient())
                        FileTransferLogsController.instance.addChar((char)b);
//                        FileTransferLogsController.instance.textArea.setText(logs);
//                    FileTransferLogsController.addTextToLogs((char)b);
                }

            };
            pw.write("=======================Started connection to " + server + " port " + port + " with user " + user + " and password " + password + "=======================\n");
            pw.write("Time: " + new java.util.Date(System.currentTimeMillis()).toString());
            ftp.addProtocolCommandListener(new ProtocolCommandListener() {
                @Override
                public void protocolCommandSent(ProtocolCommandEvent protocolCommandEvent) {
                    try {
                        os.write(protocolCommandEvent.getMessage().getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void protocolReplyReceived(ProtocolCommandEvent protocolCommandEvent) {
                    try {
                        os.write(protocolCommandEvent.getMessage().getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            ftp.connect(server, port);
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                throw new IOException("Exception in connecting to FTP Server");
            }
            ftp.setControlKeepAliveTimeout(5);
            connected = true;
            login();
//            FileStructure.init();
//            FileTransferController.instance.showDirectory(FileStructure.rootDirectory);
//            FileTransferManager.init();
    }

    private void login() throws IOException {
        ftp.login(user, password);
        if (ftp.getReplyCode() == 500)
            throw new IOException("Invalid credentials!");
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.enterLocalPassiveMode();
    }

//    public static void uploadTest(File f) throws IOException
//    {
//        FileInputStream fis = null;
//        try {
//            fis = new FileInputStream(f);
//            ftp.storeFile("folder1/FileTransfertest", fis);
//
//            DirectoryInstance dir = (DirectoryInstance)FileStructure.search("/folder1/FileTransferTest");
//            FileStructure.assignContents(dir, Client.getFTPClient());
//            if(FileStructure.currentDirectory.getPath().equals(dir.getPath()))
//            {
//                FileTransferController.instance.showDirectory(dir);
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//                fis.close();
//        }
//    }

//    public static void deleteFile(String fileName) throws IOException {
//        if(fileName.length() == 0)
//            return;
//        ftp.deleteFile(FileStructure.currentDirectory.getPath() + "/" + fileName);
//        FileStructure.assignContents(FileStructure.currentDirectory, ftp);
//        FileTransferController.instance.showDirectory(FileStructure.currentDirectory);
//    }

//    public static void deleteFile(FileStructureInstance fi) throws IOException {
//        if(fi.getName().length() == 0)
//            return;
//        ftp.deleteFile(fi.getPath());
//        FileStructure.assignContents(FileStructure.currentDirectory, Client.getFTPClient());
//        FileTransferController.instance.showDirectory(FileStructure.currentDirectory);
//    }

    public void close() throws IOException {

//            if(FileTransferManager.instance.isRunning()) {
//                //Cancel transfers
//                FileTransferManager.instance.stopClient();
//            }
            ftp.logout();
            ftp.disconnect();
//            FileStructure.currentDirectory = FileStructure.rootDirectory;
//            FileStructure.deleteStructure(FileStructure.rootDirectory);
//            ftp = null;
//            connected = false;
    }

    public FTPClient getFTPClient() {
        return ftp;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setDownloadsPath(String path) {
        downloadsPath = path;
    }

    public void download(FileInstance fi) throws IOException {
        System.out.println("Downloading file...");
        File newFile = new File(downloadsPath + "/" + fi.getName());
        int i = 1;
        int index = fi.getName().lastIndexOf('.');
        while(newFile.exists())
        {
            newFile = new File(downloadsPath + "/" + fi.getName().substring(0, index) + i++ + fi.getName().substring(index));
        }
        if(fi.getSize() > new File(downloadsPath).getFreeSpace())
        {
            System.out.printf("remote file size: %d\nFree space on pc: %d", fi.getSize(), new File(downloadsPath).getFreeSpace());
            throw new IOException("Not enough free space!");
        }
        FileTransferProcess fp =  new FileTransferProcess(true, newFile, fi.getPath(), fi.getSize(), this);
//        fp.setId(FileTransferProcessesController.instance.addProcess(fp));
        FileTransferManager.addProcess(fp);
        TransferStageController.instance.addFileTransferProcess(fp);
        if(!FileTransferManager.isRunning())
        {
            FileTransferManager.startTransfer();
        }
    }

    public void upload(File f) throws IOException {
        FileTransferProcess fp = new FileTransferProcess(false, f, fileStructure.getCurrentDirectory().getPath() + "/" + f.getName(), f.length(), this);
//        fp.setId(FileTransferProcessesController.instance.addProcess(fp));
        ftp.allo(fp.getFile().length());
        FileTransferManager.addProcess(fp);
        if(!FileTransferManager.isRunning())
        {
            FileTransferManager.startTransfer();
        }
    }

//    public static void createFolder(String folderName) throws IOException {
//        ftp.makeDirectory(FileStructure.currentDirectory.getPath() + "/" + folderName);
//        FileStructure.currentDirectory.addItem(new DirectoryInstance(FileStructure.currentDirectory.getPath() + "/" + folderName));
//        FileTransferController.instance.showDirectory(FileStructure.currentDirectory);
//    }
}