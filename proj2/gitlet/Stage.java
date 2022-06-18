package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gitlet.Repository.OBJECT_DIR;
import static gitlet.Utils.*;

public class Stage implements Serializable {


    private Map<String, String> pathToBlobID = new HashMap<>();



    public boolean isNewBlob(Blob blob) {
        if (!pathToBlobID.containsValue((blob.getBlobID()))) {
            return true;
        }
        return false;
    }

    public boolean isFilePathExists(String path) {
        if (pathToBlobID.containsKey(path)) {
            return true;
        }
        return false;
    }

    public void delete(Blob blob) {
        pathToBlobID.remove(blob.getPath());
    }

    public void delete(String path) {
       pathToBlobID.remove(path);
    }

    public void add(Blob blob) {
        if (!pathToBlobID.containsKey(blob.getPath())) {
            pathToBlobID.put(blob.getPath(), blob.getBlobID());
        }
    }

    public void save() {
        writeObject(Repository.ADDSTAGE_FILE, this);
    }

    public void clear() {
        pathToBlobID.clear();
    }

    public List<Blob> getBlobList() {
        Blob blob;
        List<Blob> blobList = new ArrayList<>();
        for (String id : pathToBlobID.values()) {
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

    public Map<String,String> getBlobMap() {
        return this.pathToBlobID;
    }

    public boolean exists(String fileName) {
        return getBlobMap().containsKey(fileName);
    }
}
