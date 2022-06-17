package gitlet;

import java.util.ArrayList;
import java.util.List;

public class Stage {

    private static List<String> blobList = new ArrayList<>();
    private static List<String> filePath = new ArrayList<>();

    public boolean isNewBlob(Blob blob) {
        if (!blobList.contains(blob.getBlobID())) {
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
        blobList.remove(blob.getBlobID());
        filePath.remove(blob.getPath());
    }

    public void add(Blob blob) {
        if (!blobList.contains(blob.getBlobID())) {
            blobList.add(blob.getBlobID());
        }
        if (!filePath.contains(blob.getPath())) {
            filePath.add(blob.getPath());
        }
    }
}
