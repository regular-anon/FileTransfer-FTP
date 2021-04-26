package FileTransfer;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.util.ArrayList;

public class FileStructure
{
    static DirectoryInstance rootDirectory = new DirectoryInstance("");
    static DirectoryInstance currentDirectory = rootDirectory;

    public static void init() throws IOException {
        rootDirectory.parentDirectory = rootDirectory;
        assignContents(rootDirectory, Client.getFTPClient());
    }

    public static void deleteStructure(FileStructureInstance fsi) {
        //Delete a tree structure - automatic garbage collection ?
        if(fsi instanceof DirectoryInstance) {
            DirectoryInstance dir = (DirectoryInstance) fsi;
            for(int i = 0;i < dir.getContents().size();++i) {
                deleteStructure(dir.getContents().get(i));
            }
            dir.getContents().clear();
        }
    }


    static void cd (String rp) throws Exception {
        currentDirectory = (DirectoryInstance)search(rp);
    }

    static void cd(DirectoryInstance dir) throws Exception {
        currentDirectory = dir;
    }
    static FileStructureInstance search(String rp) throws IOException {
        DirectoryInstance current = currentDirectory;
        if(rp.charAt(0) == '/')
        {
            if(rp.length() == 1)
            {
                current = rootDirectory;
                return current;
            }
            current = rootDirectory;
            rp = rp.substring(1);
        }
        String[] path = rp.split("/");
        for(int i = 0;i < path.length;++i)
        {
            if(path[i].equals("..")) {
                current = current.parentDirectory;
                continue;
            }
            FileStructureInstance next = searchInContentsOfDirectory(current, path[i]);//getSubdirectoryByName(current, path[i]);
            if(next instanceof FileInstance)
                return next;
            if(next == null)
                throw new IOException("File not found with path: " + rp);
            if(!((DirectoryInstance)next).hasContent)
                assignContents((DirectoryInstance)(next), Client.getFTPClient());
            current = (DirectoryInstance)(next);
        }
        return current;
    }

    static FileStructureInstance searchInContentsOfDirectory(DirectoryInstance dir, String name)
    {
        FileStructureInstance fsi = null;
        for(int i = 0;i < dir.getContents().size();++i)
        {
            if(dir.getContents().get(i).getName().equals(name))
                fsi = dir.getContents().get(i);
        }
        return fsi;
    }

    //Puts the files and subdirectories of a folder in it's list
    public static void assignContents(DirectoryInstance dir, FTPClient ftp) throws IOException {

        if(dir.hasContent)
            dir.getContents().clear();

        ftp.changeWorkingDirectory("/" + dir.getPath());
        FTPFile[] files = ftp.listFiles();
        System.out.println("------" + dir.getName() + "------");
        for(FTPFile f : files)
        {
            System.out.println(f.getName());
            if(f.isDirectory())
            {
                DirectoryInstance d = new DirectoryInstance(dir.getPath() + "/" + f.getName());
                d.setSize(f.getSize());
                d.setLastModified(f.getTimestamp().getTimeInMillis());
                dir.addItem(d);
            }
            else
            {
                FileInstance file = new FileInstance(dir.getPath() + "/" + f.getName());
                file.setSize(f.getSize());
                file.setLastModified(f.getTimestamp().getTimeInMillis());
                dir.addItem(file);
            }
        }
        dir.hasContent = true;
        System.out.println("------------");
    }

    public static String longToSizeString(float size) {
        String[] s = {"B", "KB", "MB", "GB"};
        int i = 0;
        while(size >= 1024 && i < s.length){
            i++;
            size /= 1024;
        }
        return String.format("%.2f %s", size, s[i]);
    }
}

class FileStructureInstance
{
    String remotePath;
    String name;
    DirectoryInstance parentDirectory;
    long size, lastModified;
    public FileStructureInstance(String rp)
    {
        remotePath = rp;
        int index = remotePath.lastIndexOf('/');
        if(index == -1)
            name = remotePath;
        else
            name = remotePath.substring(index + 1);
    }
    public String getPath()
    {
        return remotePath;
    }
    public String getName() { return name; }
    public void setPath(String rp)
    {
        remotePath = rp;
    }
    public void setSize(long size) {
        this.size = size;
    }
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
    public long getSize()
    {
        return size;
    }
}

class FileInstance extends FileStructureInstance
{
    public FileInstance(String rp) {
        super(rp);
    }
}
class DirectoryInstance extends FileStructureInstance
{
    ArrayList<FileStructureInstance> contents;
    boolean hasContent = false;
    public DirectoryInstance(String rp) {
        super(rp);
        contents = new ArrayList<>();
    }
    public void addItem(FileStructureInstance item)
    {
        contents.add(item);
        item.parentDirectory = this;
    }
    public ArrayList<FileStructureInstance> getContents()
    {
        return contents;
    }
}