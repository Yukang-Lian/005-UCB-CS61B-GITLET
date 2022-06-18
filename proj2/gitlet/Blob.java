package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.MyUtiles.mkdir;
import static gitlet.Repository.OBJECT_DIR;
import static gitlet.Utils.*;

public class Blob implements Serializable {

    private String id;

    private byte[] bytes;

    private File fileName;

    private String filePath;

    private File blobFileName;


    public Blob(File fileName) {
        this.fileName = fileName;
        this.bytes = readFile();
        this.filePath = fileName.getPath();
        this.id = generateID();
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


    private File generateBlobFileName() {
        return join(OBJECT_DIR, id);
    }

    public void save() {
        writeObject(blobFileName, this);
    }

}
