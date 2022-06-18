package gitlet;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

import static gitlet.Utils.*;

import static gitlet.MyUtiles.*;

// TODO: any imports you need here

/**
 * Represents a gitlet repository.
 * TODO: It's a good idea to give a description here of what else this Class
 * does at a high level.
 *
 * @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /*
     *   .gitlet
     *      |--objects
     *      |     |--commit and blob
     *      |--refs
     *      |    |--heads
     *      |         |--master
     *      |--HEAD
     *      |--stage
     */

    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File OBJECT_DIR = join(GITLET_DIR, "objects");
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    public static final File ADDSTAGE_FILE = join(GITLET_DIR, "add_stage");
    public static final File REMOVESTAGE_FILE = join(GITLET_DIR, "remove_stage");

    public static Commit currCommmit;

    public static Stage addStage = new Stage();
    public static Stage removeStage = new Stage();


    /* TODO: fill in the rest of this class. */

    /* * init command function */
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        mkdir(GITLET_DIR);
        mkdir(OBJECT_DIR);
        mkdir(REFS_DIR);
        mkdir(HEADS_DIR);

        initCommit();
        initHEAD();
        initHeads();
    }

    public static void checkIfInitialized() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    private static void initHEAD() {
        writeContents(HEAD_FILE, "master");
    }

    private static void initCommit() {
        Commit initCommit = new Commit();
        currCommmit = initCommit;
        initCommit.save();
    }

    private static void initHeads() {
        File HEADS_FILE = join(HEADS_DIR, "master");
        writeContents(HEADS_FILE, currCommmit.getID());
    }

    /* * add command funtion */
    public static void add(String file) {
        File fileName = getFileFromCWD(file);
        if (!fileName.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Blob blob = new Blob(fileName);
        storeBlob(blob);
    }

    private static void storeBlob(Blob blob) {
        addStage = readAddStage();
        if (addStage.isNewBlob(blob)) {
            blob.save();
            if (addStage.isFilePathExists(blob.getPath())) {
                addStage.delete(blob);
            }
            addStage.add(blob);
            addStage.saveAddStage();
        }
    }

    private static Stage readAddStage() {
        if (!ADDSTAGE_FILE.exists()) {
            return new Stage();
        }
        return readObject(ADDSTAGE_FILE, Stage.class);
    }

    private static File getFileFromCWD(String file) {
        return Paths.get(file).isAbsolute()
                ? new File(file)
                : join(CWD, file);
    }

    /* * commit command funtion */
    public static void commit(String message) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Commit newCommit = newCommit(message);
        newCommit.save();
        addStage.clear();
        addStage.saveAddStage();
        saveHeads(newCommit);
    }

    private static Commit newCommit(String message) {
        Map<String, String> blobMap = findBlobMap();
        checkIfNewCommit(blobMap);
        List<String> parents = findParents();
        return new Commit(message, blobMap, parents);
    }

    private static Map<String, String> findBlobMap() {
        Map<String, String> blobMap = new HashMap<>();
        addStage = readAddStage();
        List<Blob> blobList = addStage.getBlobList();
        for (Blob b : blobList) {
            blobMap.put(b.getPath(), b.getBlobID());
        }
        return blobMap;
    }

    private static void checkIfNewCommit(Map<String, String> blobMap) {
        if (blobMap.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
    }

    private static List<String> findParents() {
        List<String> parents = new ArrayList<>();
        currCommmit = readCurrCommmit();
        parents.add(currCommmit.getID());
        return parents;
    }

    private static Commit readCurrCommmit() {
        String currCommmitID = readCurrCommmitID();
        String dirName = currCommmitID.substring(0, 2);
        String fileName = currCommmitID.substring(2);
        File CURR_COMMIT_DIR = join(OBJECT_DIR, dirName);
        File CURR_COMMIT_FILE = join(CURR_COMMIT_DIR, fileName);
        return readObject(CURR_COMMIT_FILE, Commit.class);
    }

    private static String readCurrCommmitID() {
        String currBranch = readCurrBranch();
        File HEADS_FILE = join(HEADS_DIR, currBranch);
        return readContentsAsString(HEADS_FILE);
    }

    private static void saveHeads(Commit newCommit) {
        currCommmit = newCommit;
        String currBranch = readCurrBranch();
        File HEADS_FILE = join(HEADS_DIR, currBranch);
        writeContents(HEADS_FILE, currCommmit.getID());
    }

    private static String readCurrBranch() {
        return readContentsAsString(HEAD_FILE);
    }

    /* * rm command funtion */
    public static void rm(String fileName) {
        File file = getFileFromCWD(fileName);
        String filePath = file.getPath();
        addStage = readAddStage();

        currCommmit = readCurrCommmit();
        if (addStage.exists(filePath)) {
            addStage.delete(filePath);
            addStage.saveAddStage();
        } else if (currCommmit.exists(filePath)) {
            removeStage = readRemoveStage();
            removeStage.add(new Blob(file));
            removeStage.saveRemoveStage();
            deleteFile(file);
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    private static Stage readRemoveStage() {
        if (!REMOVESTAGE_FILE.exists()) {
            return new Stage();
        }
        return readObject(REMOVESTAGE_FILE, Stage.class);
    }

}
