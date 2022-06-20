package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.MyUtiles.mkdir;
import static gitlet.Repository.CWD;
import static gitlet.Repository.OBJECT_DIR;
import static gitlet.Stage.getBlobByID;
import static gitlet.Utils.join;
import static gitlet.Utils.writeObject;

/**
 * Represents a gitlet commit object.
 * TODO: It's a good idea to give a description here of what else this Class
 * does at a high level.
 * <p>
 * * author abmdocrt
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    private String message;

    private Map<String, String> pathToBlobID = new HashMap<>();

    private List<String> parents;

    private Date currentTime;

    private String id;

    private File commitSaveFileName;

    private String timeStamp;


    /* TODO: fill in the rest of this class. */

    public Commit(String message, Map<String, String> pathToBlobID, List<String> parents) {
        this.message = message;
        this.pathToBlobID = pathToBlobID;
        this.parents = parents;
        this.currentTime = new Date();
        this.timeStamp = dateToTimeStamp(this.currentTime);
        this.id = generateID();
        this.commitSaveFileName = generateFileName();
    }

    public Commit() {
        this.currentTime = new Date(0);
        this.timeStamp = dateToTimeStamp(this.currentTime);
        this.message = "initial commit";
        this.pathToBlobID = new HashMap<>();
        this.parents = new ArrayList<>();
        this.id = generateID();
        this.commitSaveFileName = generateFileName();
    }

    private static String dateToTimeStamp(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return dateFormat.format(date);
    }

    public String getMessage() {
        return message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public Map<String, String> getPathToBlobID() {
        return pathToBlobID;
    }

    public List<String> getBlobIDList(){
        List<String> list = new ArrayList<>(pathToBlobID.values());
        return list;
    }


    public List<String> getParentsCommitID() {
        return parents;
    }

    public Date getCurrentTime() {
        return currentTime;
    }

    public String getID() {
        return id;
    }

    private String generateTimeStamp() {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.CHINA);
        return dateFormat.format(currentTime);
    }

    private String generateID() {
        return Utils.sha1(generateTimeStamp(), message, parents.toString(), pathToBlobID.toString());
    }


    private File generateFileName() {
        return join(OBJECT_DIR, id);
    }

    public void save() {
        writeObject(commitSaveFileName, this);
    }

    public boolean exists(String filePath) {
        return pathToBlobID.containsKey(filePath);
    }

    public List<String> getFileNames() {
        List<String> fileName = new ArrayList<>();
        List<Blob> blobList = getBlobList();
        for (Blob b : blobList) {
            fileName.add(b.getFileName());
        }
        return fileName;
    }

    private List<Blob> getBlobList() {
        Blob blob;
        List<Blob> blobList = new ArrayList<>();
        for (String id : pathToBlobID.values()) {
            blob = getBlobByID(id);
            blobList.add(blob);
        }
        return blobList;
    }

    public Blob getBlobByFileName(String fileName) {
        File file = join(CWD, fileName);
        String path = file.getPath();
        String blobID = pathToBlobID.get(path);
        return getBlobByID(blobID);
    }
}
