package FileTransfer;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
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
    static private String downloadsPath = System.getProperty("user.home") + "/Downloads";
    static private String logs = "";

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
            File f = new File("src/main/resources/Other/Logs/" + user + "@" + server + ".txt");
            if(!f.exists())
                f.createNewFile();
            pw = new PrintWriter(new FileWriter(f, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getLogs() {
        return logs;
    }

    public static void connect() throws IOException {
            ftp = new FTPClient();
            ftp.setControlEncoding("UTF-8");
            OutputStream os = new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    pw.write(b);
                    pw.flush();

                    FileTransferLogsController.addTextToLogs((char)b);
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
            FileStructure.init();
            FileTransferController.instance.showDirectory(FileStructure.rootDirectory);
            FileTransferManager.init();
    }

    private static void login() throws IOException {
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

    public static void deleteFile(String fileName) throws IOException {
        if(fileName.length() == 0)
            return;
        ftp.deleteFile(FileStructure.currentDirectory.getPath() + "/" + fileName);
        FileStructure.assignContents(FileStructure.currentDirectory, ftp);
        FileTransferController.instance.showDirectory(FileStructure.currentDirectory);
    }

    public static void close() throws Exception {
        try {
            if(FileTransferManager.instance.isRunning()) {
                //Cancel transfers
                FileTransferManager.instance.stopClient();
            }
            ftp.logout();
            System.out.println("Logged out from server");
            ftp.disconnect();
            System.out.println("Disconnected from server");
            FileStructure.currentDirectory = FileStructure.rootDirectory;
            FileStructure.deleteStructure(FileStructure.rootDirectory);
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Error in closing client (close())");
        } catch (NullPointerException e) {
            System.out.println("It was null! (close())");
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
        FileTransferProcess fp =  new FileTransferProcess(true, newFile, fi.getPath(), fi.getSize());
        fp.setId(FileTransferProcessesController.instance.addProcess(newFile.getName(), ((FileInstance)(FileStructure.search(fp.getRemotePath()))).getSize()));
        FileTransferManager.instance.addProcess(fp);
        if(!FileTransferManager.instance.isRunning())
        {
            new Thread(FileTransferManager.instance).start();
        }
    }
    public static void upload(File f) throws IOException {
        FileTransferProcess fp = new FileTransferProcess(false, f, FileStructure.currentDirectory.getPath() + "/" + f.getName(), f.length());
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
    private boolean done, cancelled = false;
    private int failedAttempts;
    private float progressValue = 0.0f; // 0 -> 1.0
    private long remoteFileSize;

    private String remotePath;
    private int id;

    public FileTransferProcess(boolean bool, File f, String remotePath, long remoteFileSize)
    {
        this.download = bool;
        this.file = f;
        done = false;
        failedAttempts = 0;
        this.remotePath = remotePath;
        this.remoteFileSize = remoteFileSize;
    }

    public void updateProgress(float f) {
        progressValue = f;
    }

    public long getRemoteFileSize() {
        return remoteFileSize;
    }

    public float getProgress() {
        return progressValue;
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

//    public void delete()
//    {
//        if(file.exists())
//            file.delete();
//    }
//
//    public void finishTransfer() {
//        done = true;
//    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancelTransfer() {
        cancelled = true;

    }
}

class FileTransferManager implements Runnable
{
    private ArrayList<FileTransferProcess> processes;
    public boolean isRunning = false;
    public FTPClient ftp;
    static FileTransferManager instance;
    private int maxBufferSize = 16_384; // maximum buffer size

    public void addProcess(FileTransferProcess fp)
    {
        processes.add(fp);
    }

    private FileTransferManager() {
        processes = new ArrayList<>();
    }

    public FileTransferProcess getProcessById(int id) {
        for(int i = 0;i < processes.size();++i) {
            if(processes.get(i).getId() == id)
                return processes.get(i);
        }
        return null;
    }

    public void startClient() throws IOException {
//        ftp = new FTPClient();
//        ftp.connect(Client.getServer(), Client.getPort());
//        int reply = ftp.getReplyCode();
//        if (!FTPReply.isPositiveCompletion(reply)) {
//            ftp.disconnect();
//            throw new IOException("Exception in connecting to FTP Server");
//        }
//
//        ftp.login(Client.getCredentials()[0], Client.getCredentials()[1]);
//        if (ftp.getReplyCode() == 500)
//            throw new IOException("Invalid credentials!");
//        ftp.setFileType(FTP.BINARY_FILE_TYPE);
//        ftp.enterLocalPassiveMode();
        ftp = Client.getFTPClient();
    }

    public void startTransfer() {
        new Thread(instance).start();
    }

    public void stopClient() throws Exception {
        if(ftp == null)
            return;
        try {
            cancelProcesses();
//            if(ftp.isConnected()) {
//                System.out.println("Trying to disconnect transfer ftp client... (line 351)");
//                ftp.logout();
//                ftp.disconnect();
//            }
            System.out.println("Stopped client without any errors! (stopClient())");
        }
//        catch (IOException e) {
//            e.printStackTrace();
//            throw new Exception("Error in closing client");
//        }
        finally {
            ftp = null;
            isRunning = false;
        }
    }

    private void cancelProcesses()
    {
        for(int i = 0;i < processes.size();++i) {
            processes.get(i).cancelTransfer();
        }
//        processes.clear();
        System.out.println("Managed to cancel processes! (cancelProcesses())");
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
            if(fp.failedTries() > 3 || fp.isCancelled())
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
                    System.out.println("IOException caught in transfer thread!");
                    e.printStackTrace();
                    processes.remove(fp);
                    fp.incrementTries();
                    processes.add(fp);
                    continue;
                }
                catch (RuntimeException e) {
//                    System.out.println("Got RuntimeException with message: " + e.getMessage());
//                    if(e.getMessage().equals("File download cancelled!")) {
//                        processes.remove(fp);
//                        continue;
//                    }
                }
                catch(Exception e) {
                    System.out.println("Caught general exception with message: " + e.getMessage());
                }
                processes.remove(fp);
            }
            else
            {
                try {
                    upload(fp);
                    FileStructure.assignContents((DirectoryInstance)FileStructure.search(fp.getRemotePath().substring(0, fp.getRemotePath().lastIndexOf('/'))), Client.getFTPClient());
                } catch (IOException e) {
                    e.printStackTrace();
                    processes.remove(fp);
                    fp.incrementTries();
                    processes.add(fp);
                    continue;
                } catch (RuntimeException e) {
                    if(e.getMessage().equals("File download cancelled!")) {
                        processes.remove(fp);
                        System.out.println("Upload cancelled!");
                        try {
                            ftp.abort();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                        continue;
                    }
                }
                processes.remove(fp);
            }
        }
        isRunning = false;
        System.out.println("Done transfering files! (end of run())");
//        try {
//            stopClient();
//        } catch (Exception e) {
//            System.out.println("Could not stop transfer client!");
//        }
        System.out.println("Transfer thread finally ended!");
    }
    private void upload(FileTransferProcess fp) throws IOException {
        FileInputStream fis = null;
        System.out.println("File name: " + fp.getFile().getName());
        try {
            fis = new FileInputStream(fp.getFile());
            BufferedOutputStream outputStream = new BufferedOutputStream(ftp.storeFileStream(fp.getRemotePath()));
            long uploadedSize = 0L;
            long fileSize = fp.getFile().length();
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
                fp.updateProgress(uploadedSize / fileSize);
            }
            if (outputStream != null)
                outputStream.close();
            if (!ftp.completePendingCommand())
                throw new IOException("IOException in file upload!");
            if (uploadedSize < fileSize)
                throw new RuntimeException("File upload cancelled!");
            System.out.println("Closed outputStream...");

            System.out.println("Stored file.");

            System.out.println("Updating directory...");
            DirectoryInstance dir = (DirectoryInstance) FileStructure.search(fp.getRemotePath().substring(0, fp.getRemotePath().lastIndexOf('/')));
            FileStructure.assignContents(dir, Client.getFTPClient());

            if (FileStructure.currentDirectory.getPath().equals(dir.getPath())) {
                FileTransferController.instance.showDirectory(dir);
            }
        }
        finally {
            if(fis != null)
                fis.close();
        }
    }
    //When using retrieveFileStream() method, close the input stream and then call completePendingCommand()
    private void download(FileTransferProcess fp) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fp.getFile());
            BufferedInputStream inputStream = new BufferedInputStream(ftp.retrieveFileStream(fp.getRemotePath()));
            int tempByte = inputStream.read();
            long downloadedSize = 0;
            while(tempByte != -1 && !fp.isCancelled()) {
                int available = inputStream.available();
                byte[] buffer = new byte[Math.min(maxBufferSize, available) + 1];
                buffer[0] = (byte)tempByte;
                inputStream.read(buffer, 1, buffer.length - 1);
                tempByte = inputStream.read();
                fos.write(buffer);
                downloadedSize += buffer.length;
                fp.updateProgress(downloadedSize / fp.getRemoteFileSize());
            }
            if(inputStream != null)
                inputStream.close();
            if(!ftp.completePendingCommand())
                throw new IOException ("IOException in file download!");
            if(downloadedSize < fp.getFile().length())
                throw new RuntimeException("File download cancelled!");

        }
        finally {
            if(fos != null)
                fos.close();
        }
    }
}