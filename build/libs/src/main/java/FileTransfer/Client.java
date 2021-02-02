package FileTransfer;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.ArrayList;

//TODO: transfer abort

public class Client {
    static private String server;
    static private int port;
    static private String user;
    static private String password;
    static private FTPClient ftp;
    static private PrintWriter pw;
    static private boolean connected = false;
    static private String downloadsPath = "C:/Users/Cristian/Desktop";

    static void setCredentials(String server, int port, String user, String password) {
        Client.server = server;
        Client.port = port;
        Client.user = user;
        Client.password = password;
        try {
            pw = new PrintWriter(new FileWriter("src/main/resources/Logs/FtpClientLogs.txt", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void connect() throws IOException {
            ftp = new FTPClient();
            pw.write("=======================Started connection to " + server + " port " + port + " with user " + user + " and password " + password + "=======================\n");
            pw.write("Time: " + new java.util.Date(System.currentTimeMillis()).toString());
            ftp.addProtocolCommandListener(new PrintCommandListener(pw));

            ftp.connect(server, port);
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                throw new IOException("Exception in connecting to FTP Server");
            }
            connected = true;
    }
    public static void login() throws IOException {
        ftp.login(user, password);
        if (ftp.getReplyCode() == 500)
            throw new IOException("Invalid credentials!");
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.enterLocalPassiveMode();
    }
    public static void close() throws Exception {
        try {
            ftp.logout();
            ftp.disconnect();
        } catch (IOException e) {
            throw new Exception("Error in closing client");
        } finally {
            ftp = null;
            connected = false;
        }
    }
    public static FTPClient getFTPClient()
    {
        return ftp;
    }

    public static boolean isConnected()
    {
        return connected;
    }

    public static void setDownloadsPath(String path)
    {
        downloadsPath = path;
    }

    public static void download(FileInstance fi, String fileName) throws IOException {
        System.out.println("Downloading file...");
        File newFile = new File(downloadsPath + "/" + fileName);
        int i = 1;
        while(newFile.exists())
        {
            newFile = new File(downloadsPath + "/" + fileName + i++);
        }
        File f = new File(downloadsPath + "/" + newFile.getName() + "filetransferdownload");
        if(fi.getSize() > new File(downloadsPath).getFreeSpace())
        {
            System.out.printf("remote file size: %d\nFree space on pc: %d", fi.getSize(), new File(downloadsPath).getFreeSpace());
            throw new IOException("Not enough free space!");
        }
        if(newFile.exists())
            newFile.createNewFile();
        FileTransferProcess fp =  new FileTransferProcess(true, newFile, fi.getPath());
        FileTransferManager.instance.addProcess(fp);
        if(!FileTransferManager.instance.isRunning())
        {
            new Thread(FileTransferManager.instance).start();
        }
//        newFile.createNewFile();
//        f.renameTo(newFile);
    }
    public static void upload(File f)
    {
        FileTransferProcess fp = new FileTransferProcess(false, f, FileStructure.currentDirectory.getPath());
        FileTransferManager.instance.addProcess(fp);
        if(!FileTransferManager.instance.isRunning())
        {
            new Thread(FileTransferManager.instance).start();
        }
    }
}

class FileTransferProcess
{
    private boolean download;
    private File file;
    //private boolean isExecuting;
    private boolean done;
    private int failedAttempts;

    private String remotePath;

    public FileTransferProcess(boolean bool, File f, String remotePath)
    {
        this.download = bool;
        this.file = f;
        //isExecuting = false;
        done = false;
        failedAttempts = 0;
        this.remotePath = remotePath;
    }

    public boolean isDone()
    {
        return done;
    }
//    public boolean isExecuting()
//    {
//        return isExecuting;
//    }
    public int failedTries()
    {
        return failedAttempts;
    }

    public void incrementTries()
    {
        failedAttempts++;
    }

    public boolean isDownload()
    {
        return download;
    }

    public File getFile()
    {
        return file;
    }

    public String getRemotePath()
    {
        return remotePath;
    }

    public void delete()
    {

    }
//    @Override
//    public void run()
//    {
//        isExecuting = true;
//        try {
//            Client.getFTPClient().storeFile(FileStructure.currentDirectory.getPath(), new FileInputStream(file));
//            done = true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        finally {
//            isExecuting = false;
//        }
//    }
}

class FileTransferManager implements Runnable
{
    private ArrayList<FileTransferProcess> processes;
    private boolean isRunning = false;
    static FileTransferManager instance;

    public void addProcess(FileTransferProcess fp)
    {
        processes.add(fp);
    }

    private FileTransferManager()
    {
        processes = new ArrayList<>();
    }

    public void emptyProcesses()
    {
        processes = new ArrayList<>();
    }

    public boolean isRunning()
    {
        return isRunning;
    }

    public static void init()
    {
        instance = new FileTransferManager();
    }

    @Override
    public void run()
    {
        isRunning = true;
        while(processes.size() > 0)
        {
            FileTransferProcess fp = processes.get(0);
            if(fp.failedTries() > 3)
            {
                System.out.print("Could not ");
                if(fp.isDownload())
                    System.out.print("download ");
                else
                    System.out.print("upload ");
                System.out.println(fp.getFile().getName());
                processes.remove(fp);
                continue;
            }
            if(fp.isDownload())
            {
                try {
                    download(fp);
                } catch (IOException e) {
                    processes.remove(fp);
                    fp.incrementTries();
                    processes.add(fp);
                    continue;
                }
                processes.remove(fp);
            }
            else
            {
                try {
                    upload(fp);
                } catch (IOException e) {
                    processes.remove(fp);
                    fp.incrementTries();
                    processes.add(fp);
                    continue;
                }
                processes.remove(fp);
            }
        }
        isRunning = false;
        System.out.println("Done transfering files!");
    }

    private void upload(FileTransferProcess fp) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fp.getFile());
            Client.getFTPClient().storeFile(fp.getRemotePath(), fis);
        } catch (FileNotFoundException e) {
            System.out.println("Could not open file.");
            e.printStackTrace();
        }
        finally {
            fis.close();
        }
    }
    private void download(FileTransferProcess fp) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fp.getFile());
            Client.getFTPClient().retrieveFile(fp.getRemotePath(), fos);
        } catch (FileNotFoundException e) {
            System.out.println("Could not open file.");
            e.printStackTrace();
        }
        finally {
            fos.close();
        }
    }
}