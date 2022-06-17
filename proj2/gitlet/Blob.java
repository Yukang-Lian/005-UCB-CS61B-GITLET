package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.MyUtiles.mkdir;
import static gitlet.Utils.*;

public class Blob implements Serializable {

    private String id;

    private byte[] bytes;

    private File fileName;

    private String filePath;

    private File blobFileName;

    private File blobDirName;


    public Blob(File fileName) {
        this.fileName = fileName;
        this.bytes = readFile();
        this.filePath = fileName.getPath();
        this.id = generateID();
        this.blobDirName = generateBlobDirName();
        this.blobFileName = generateBlobFileName();
    }

    public String getBlobID() {
        return id;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getPath() {
        return filePath;
    }

    private byte[] readFile() {
        return readContents(fileName);
    }

    private String generateID() {
        return sha1(filePath, bytes);
    }

    private File generateBlobDirName() {
        String dirName = id.substring(0, 2);
        return join(Repository.OBJECT_DIR, dirName);
    }

    private File generateBlobFileName() {
        String fileName = id.substring(2);
        return join(this.blobDirName, fileName);
    }

    public void save() {
        if (!blobDirName.exists()) {
            mkdir(blobDirName);
        }
        writeObject(blobFileName, this);
    }

}
