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
                dir.addItem(d);
            }
            else
            {
                FileInstance file = new FileInstance(dir.getPath() + "/" + f.getName(), f.getSize());
                dir.addItem(file);
            }
        }
        dir.hasContent = true;
        System.out.println("------------");
    }
    private static void initStructure(DirectoryInstance dir, FTPClient ftp) throws IOException {
        ftp.changeWorkingDirectory("/" + dir.getPath());
        FTPFile[] files = ftp.listFiles();
        DirectoryInstance[] dirs = new DirectoryInstance[ftp.listDirectories().length];
        int index = 0;
        for (FTPFile f : files) {
            if (f.isDirectory()) {
                DirectoryInstance d = new DirectoryInstance(dir.getPath() + "/" + f.getName());
                dirs[index++] = d;
                dir.addItem(d);
            } else {
                FileInstance file = new FileInstance(dir.getPath() + "/" + f.getName(), f.getSize());
                dir.addItem(file);
            }
        }
        for (DirectoryInstance directory : dirs) {
            initStructure(directory, ftp);
        }
    }

//    static class FileDiscoverer implements Runnable
//    {
//        public void run()
//        {
//            try {
//                FileStructure.initStructure(FileStructure.rootDirectory, Client.getFTPClient());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

}

class FileStructureInstance
{
    String remotePath;
    String name;
    DirectoryInstance parentDirectory;
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
}

class FileInstance extends FileStructureInstance
{
    private long size;
    public FileInstance(String rp, long size) {
        super(rp);
        this.size = size;
    }

    public long getSize()
    {
        return size;
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