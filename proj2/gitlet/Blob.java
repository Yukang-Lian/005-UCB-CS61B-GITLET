package gitlet;

import java.io.Serializable;

public class Blob implements Serializable {

    private String blobName;
    private byte[] bytes;

    public Blob(String blobName, byte[] bytes) {
        this.blobName = blobName;
        this.bytes = bytes;
    }

    public String getBlobName() {
        return blobName;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
