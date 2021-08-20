package com.filetransfer.util.files;

public class FileStructureInstance {
    String remotePath;
    String name;
    public DirectoryInstance parentDirectory;
    public long size, lastModified;

    public FileStructureInstance(String rp) {
        remotePath = rp;
        int index = remotePath.lastIndexOf('/');
        if (index == -1)
            name = remotePath;
        else
            name = remotePath.substring(index + 1);
        size = 0;
    }

    public String getPath() {
        return remotePath;
    }

    public String getName() {
        return name;
    }

    public void setPath(String rp) {
        remotePath = rp;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getSize() {
        return size;
    }

    public void setName(String name) {
        this.name = name;
    }
}
