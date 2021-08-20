package com.filetransfer.util.files;

import java.util.ArrayList;

public class DirectoryInstance extends FileStructureInstance {
    ArrayList<FileStructureInstance> contents;
    private boolean hasContent = false;

    public boolean hasContent() {
        return hasContent;
    }

    public void setHasContent(boolean b) {
        hasContent = b;
    }

    public DirectoryInstance(String rp) {
        super(rp);
        contents = new ArrayList<>();
    }

    public void addItem(FileStructureInstance item) {
        contents.add(item);
        item.parentDirectory = this;
    }

    public ArrayList<FileStructureInstance> getContents() {
        return contents;
    }
}
