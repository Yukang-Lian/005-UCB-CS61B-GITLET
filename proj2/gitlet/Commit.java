package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.MyUtiles.mkdir;
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

    private Map<String, String> map = new HashMap<>();

    private List<String> parents;

    private Date currentTime;

    private String id;

    private File fileName;

    private File dirName;


    /* TODO: fill in the rest of this class. */

    public Commit(String message, Map<String, String> map, List<String> parents) {
        this.message = message;
        this.map = map;
        this.parents = parents;
        this.currentTime = new Date();
        this.id = generateID();
        this.dirName = generateDirName();
        this.fileName = generateFileName();
    }

    public Commit() {
        this.currentTime = new Date(0);
        this.message = "initial commit";
        this.map = new HashMap<>();
        this.parents = new ArrayList<>();
        this.id = generateID();
        this.dirName = generateDirName();
        this.fileName = generateFileName();
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public List<String> getPrevCommit() {
        return parents;
    }

    public Date getCurrentTime() {
        return currentTime;
    }

    public String getID() {
        return id;
    }

    private String generateTimeStamp() {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
        return dateFormat.format(currentTime);
    }

    private String generateID() {
        return Utils.sha1(generateTimeStamp(), message, parents.toString(), map.toString());
    }

    private File generateDirName() {
        String dirName = id.substring(0, 2);
        return join(Repository.OBJECT_DIR, dirName);
    }

    private File generateFileName() {
        String fileName = id.substring(2);
        return join(this.dirName, fileName);
    }

    public void save() {
        if (!dirName.exists()) {
            mkdir(dirName);
        }
        writeObject(fileName, this);
    }
}
