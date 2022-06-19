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

    private File blobSaveFileName;


    public Blob(File fileName) {
        this.fileName = fileName;
        this.bytes = readFile();
        this.filePath = fileName.getPath();
        this.id = generateID();
        this.blobSaveFileName = generateBlobSaveFileName();
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

    public File getBlobSaveFileName() {
        return blobSaveFileName;
    }

    public String getFileName(){
        return fileName.getName();
    }

    private byte[] readFile() {
        return readContents(fileName);
    }

    private String generateID() {
        return sha1(filePath, bytes);
    }


    private File generateBlobSaveFileName() {
        return join(OBJECT_DIR, id);
    }

    public void save() {
        writeObject(blobSaveFileName, this);
    }

}
