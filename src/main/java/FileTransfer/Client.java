package FileTransfer;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.ArrayList;


public class Client {
    static private String server;
    static private int port;
    static private String user;
    static private String password;
    static private FTPClient ftp;
    static private PrintWriter pw;
    static private boolean connected = false;
    static private String downloadsPath = "C:/Users/Cristian/Desktop";

    public static String getDownloadsPath()
    {
        return downloadsPath;
    }

    public static String[] getCredentials()
    {
        return new String[] {user, password};
    }

    public static int getPort()
    {
        return port;
    }

    public static String getServer()
    {
        return server;
    }

    static void setCredentials(String server, int port, String user, String password) {
        Client.server = server;
        Client.port = port;
        Client.user = user;
        Client.password = password;
        try {
            System.out.println();
            pw = new PrintWriter(new FileWriter("src/main/resources/Other/FtpClientLogs.txt", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void connect() throws IOException {
            ftp = new FTPClient();
            ftp.setControlEncoding("UTF-8");
            pw.write("=======================Started connection to " + server + " port " + port + " with user " + user + " and password " + password + "=======================\n");
            pw.write("Time: " + new java.util.Date(System.currentTimeMillis()).toString());
            ftp.addProtocolCommandListener(new PrintCommandListener(pw));

            ftp.connect(server, port);
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                throw new IOException("Exception in connecting to FTP Server");
            }
            ftp.setControlKeepAliveTimeout(5);
            connected = true;
    }

    public static void login() throws IOException {
        ftp.login(user, password);
        if (ftp.getReplyCode() == 500)
            throw new IOException("Invalid credentials!");
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.enterLocalPassiveMode();
    }

    public static void uploadTest(File f) throws IOException
    {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            ftp.storeFile("folder1/FileTransfertest", fis);
            System.out.println("Stored file.");

            System.out.println("Updating directory...");
            DirectoryInstance dir = (DirectoryInstance)FileStructure.search("/folder1/FileTransferTest");
            FileStructure.assignContents(dir, Client.getFTPClient());
            if(FileStructure.currentDirectory.getPath().equals(dir.getPath()))
            {
                FileTransferController.instance.showDirectory(dir);
            }
            System.out.println("Everything went smoothly :/");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
                fis.close();
        }
    }

    public static void close() throws Exception {
        System.out.println("Closing FTP Client...");
        try {
            ftp.logout();
            ftp.disconnect();
        } catch (IOException e) {
            throw new Exception("Error in closing client");
        } finally {
            ftp = null;
            connected = false;
        }
        System.out.println("Done closing!");
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

    public static void download(FileInstance fi, String fileName) throws Exception {
        System.out.println("Downloading file...");
        File newFile = new File(downloadsPath + "/" + fileName);
        int i = 1;
        int index = fileName.lastIndexOf('.');
        while(newFile.exists())
        {
            newFile = new File(downloadsPath + "/" + fileName.substring(0, index) + i++ + fileName.substring(index));
        }
        if(fi.getSize() > new File(downloadsPath).getFreeSpace())
        {
            System.out.printf("remote file size: %d\nFree space on pc: %d", fi.getSize(), new File(downloadsPath).getFreeSpace());
            throw new IOException("Not enough free space!");
        }
        FileTransferProcess fp =  new FileTransferProcess(true, newFile, fi.getPath());
        fp.setId(FileTransferProcessesController.instance.addProcess(newFile.getName(), ((FileInstance)(FileStructure.search(fp.getRemotePath()))).getSize()));
        FileTransferManager.instance.addProcess(fp);
        if(!FileTransferManager.instance.isRunning())
        {
            new Thread(FileTransferManager.instance).start();
        }
    }
    public static void upload(File f) throws IOException {
        FileTransferProcess fp = new FileTransferProcess(false, f, FileStructure.currentDirectory.getPath() + "/" + f.getName());
        fp.setId(FileTransferProcessesController.instance.addProcess(f.getName(), f.length()));
        ftp.allo(fp.getFile().length());
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
    private boolean done;
    private int failedAttempts;

    private String remotePath;
    private int id;

    public FileTransferProcess(boolean bool, File f, String remotePath)
    {
        this.download = bool;
        this.file = f;
        done = false;
        failedAttempts = 0;
        this.remotePath = remotePath;
    }

    public void setId(int id)
    {
        this.id = id;
    }
    public int getId()
    {
        return id;
    }

    public boolean isDone()
    {
        return done;
    }
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
}

class FileTransferManager implements Runnable
{
    private ArrayList<FileTransferProcess> processes;
    public boolean isRunning = false;
    public FTPClient ftp;
    static FileTransferManager instance;

    public void addProcess(FileTransferProcess fp)
    {
        processes.add(fp);
    }

    private FileTransferManager() {
        processes = new ArrayList<>();
    }

    public void startClient() throws IOException {
        ftp = new FTPClient();
        ftp.connect(Client.getServer(), Client.getPort());
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new IOException("Exception in connecting to FTP Server");
        }

        ftp.login(Client.getCredentials()[0], Client.getCredentials()[1]);
        if (ftp.getReplyCode() == 500)
            throw new IOException("Invalid credentials!");
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.enterLocalPassiveMode();
    }

    public void stopClient() throws Exception {
        try {
            ftp.logout();
            ftp.disconnect();
        } catch (IOException e) {
            throw new Exception("Error in closing client");
        } finally {
            ftp = null;
        }
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
        try {
            startClient();
        } catch (IOException e) {
            System.out.println("Could not start transfer client!");
        }
        while(processes.size() > 0 && isRunning)
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
                    FileStructure.assignContents((DirectoryInstance)FileStructure.search(fp.getRemotePath().substring(0, fp.getRemotePath().lastIndexOf('/'))), Client.getFTPClient());
                } catch (IOException e) {
                    processes.remove(fp);
                    fp.incrementTries();
                    processes.add(fp);
                    continue;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                processes.remove(fp);
            }
        }
        isRunning = false;
        System.out.println("Done transfering files!");
        try {
            stopClient();
        } catch (Exception e) {
            System.out.println("Could not stop transfer client!");
        }
    }
    private void upload(FileTransferProcess fp) throws Exception {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fp.getFile());
            ftp.changeWorkingDirectory(fp.getRemotePath().substring(0, fp.getRemotePath().lastIndexOf('/')));
            ftp.storeFile(fp.getRemotePath().substring(fp.getRemotePath().lastIndexOf('/') + 1), fis);
            System.out.println("Stored file.");

            System.out.println("Updating directory...");
            DirectoryInstance dir = (DirectoryInstance)FileStructure.search(fp.getRemotePath().substring(0, fp.getRemotePath().lastIndexOf('/')));
            FileStructure.assignContents(dir, Client.getFTPClient());

            if(FileStructure.currentDirectory.getPath().equals(dir.getPath()))
            {
                FileTransferController.instance.showDirectory(dir);
            }

            System.out.println("Everything went smoothly :/");
        }
        finally {
            fis.close();
        }
    }
    private void download(FileTransferProcess fp) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fp.getFile());
            ftp.retrieveFile(fp.getRemotePath(), fos);
        }
        finally {
            fos.close();
        }
    }
}