package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static gitlet.Repository.OBJECT_DIR;
import static gitlet.Utils.*;

public class Stage implements Serializable {

    private List<String> blobIDList = new ArrayList<>();
    private List<String> filePath = new ArrayList<>();

    public List<String> getBIL(){
        return blobIDList;
    }

    public boolean isNewBlob(Blob blob) {
        if (!blobIDList.contains(blob.getBlobID())) {
            return true;
        }
        return false;
    }

    public boolean isFilePathExists(String path) {
        if (filePath.contains(path)) {
            return true;
        }
        return false;
    }

    public void delete(Blob blob) {
        blobIDList.remove(blob.getBlobID());
        filePath.remove(blob.getPath());
    }

    public void add(Blob blob) {
        if (!blobIDList.contains(blob.getBlobID())) {
            blobIDList.add(blob.getBlobID());
        }
        if (!filePath.contains(blob.getPath())) {
            filePath.add(blob.getPath());
        }
    }

    public void save() {
        writeObject(Repository.ADDSTAGE_FILE, this);
    }

    public void clear() {
        blobIDList.clear();
        filePath.clear();
    }

    public List<Blob> getBlobList() {
        Blob blob;
        List<Blob> blobList = new ArrayList<>();
        for (String id : blobIDList) {
            blob = getBlob(id);
            blobList.add(blob);
        }
        return blobList;
    }

    private Blob getBlob(String id) {
        String dirName = id.substring(0, 2);
        String fileName = id.substring(2);
        File BLOB_DIR = join(OBJECT_DIR, dirName);
        File BLOB_FILE = join(BLOB_DIR, fileName);
        return readObject(BLOB_FILE, Blob.class);
    }
}
