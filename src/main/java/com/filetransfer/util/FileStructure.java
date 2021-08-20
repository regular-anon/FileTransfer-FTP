package com.filetransfer.util;

import com.filetransfer.Client;
import com.filetransfer.util.files.DirectoryInstance;
import com.filetransfer.util.files.FileInstance;
import com.filetransfer.util.files.FileStructureInstance;
import fxcontrollers.MainFXMLController;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileStructure
{
    private DirectoryInstance rootDirectory = new DirectoryInstance("");
    private DirectoryInstance currentDirectory = rootDirectory;
    private FTPClient ftp;

    public FileStructure(FTPClient client) {
        ftp = client;
    }

    public void init() throws IOException {
        rootDirectory.parentDirectory = rootDirectory;
        assignContents(rootDirectory);
    }

    public void deleteStructure(FileStructureInstance fsi) {
        //Delete a tree structure - automatic garbage collection ?
        if(fsi instanceof DirectoryInstance) {
            DirectoryInstance dir = (DirectoryInstance) fsi;
            for(int i = 0;i < dir.getContents().size();++i) {
                deleteStructure(dir.getContents().get(i));
            }
            dir.getContents().clear();
        }
    }


    public void cd (String rp) throws Exception {
        currentDirectory = (DirectoryInstance)search(rp);
    }

    public void cd(DirectoryInstance dir) throws Exception {
        currentDirectory = dir;
    }
    public FileStructureInstance search(String rp) throws IOException {
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
            if(!((DirectoryInstance)next).hasContent())
                assignContents((DirectoryInstance)(next));
            current = (DirectoryInstance)(next);
        }
        return current;
    }

    private List<FileStructureInstance> sff(String query, DirectoryInstance root, List<FileStructureInstance> files, int depth) throws IOException {
        if(depth == 0)
            return files;
        for(int i = 0;i < root.getContents().size(); ++i) {
            FileStructureInstance file = root.getContents().get(i);
            if(file.getName().contains(query))
                files.add(file);
            if(file instanceof DirectoryInstance) {
                DirectoryInstance dir = (DirectoryInstance) file;
                if(!dir.hasContent())
                    assignContents(dir);
                sff(query, dir, files, depth - 1);
            }
        }
        return files;
    }

    public void addFile(FileStructureInstance fsi, Client cl) throws IOException {
        String path = fsi.getPath();
        if(path.length() == 0)
            return;
        DirectoryInstance dir = (DirectoryInstance) search(path.substring(0, path.lastIndexOf('/')));
        dir.getContents().add(fsi);
        if(dir == cl.getFileStructure().getCurrentDirectory())
            if(MainFXMLController.instance.currentTabManager.getClient() == cl)
                MainFXMLController.instance.currentTabManager.setDirectoryView(dir);
    }

    public List<FileStructureInstance> searchForFiles(String query, DirectoryInstance dir, int depth) throws IOException {
        return sff(query, dir, new ArrayList<FileStructureInstance>(), depth);
    }

    public FileStructureInstance searchInContentsOfDirectory(DirectoryInstance dir, String name)
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
    public void assignContents(DirectoryInstance dir) throws IOException {

        if(dir.hasContent())
            dir.getContents().clear();

        ftp.changeWorkingDirectory("/" + dir.getPath());
        FTPFile[] files = ftp.listFiles();
        for(FTPFile f : files)
        {
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
        dir.setHasContent(true);
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

    public DirectoryInstance getRootDirectory() {
        return rootDirectory;
    }

    public DirectoryInstance getCurrentDirectory() {
        return currentDirectory;
    }
}

