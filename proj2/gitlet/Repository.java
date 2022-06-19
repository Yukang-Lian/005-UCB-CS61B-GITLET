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

    public static Commit currCommit;

    public static Stage addStage = new Stage();
    public static Stage removeStage = new Stage();
    public static String currBranch;

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
        currCommit = initCommit;
        initCommit.save();
    }

    private static void initHeads() {
        File HEADS_FILE = join(HEADS_DIR, "master");
        writeContents(HEADS_FILE, currCommit.getID());
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
        removeStage.clear();
        removeStage.saveRemoveStage();
        saveHeads(newCommit);
    }

    private static Commit newCommit(String message) {
        Map<String, String> addBlobMap = findAddBlobMap();
        Map<String, String> removeBlobMap = findRemoveBlobMap();
        checkIfNewCommit(addBlobMap, removeBlobMap);

        currCommit = readCurrCommmit();
        Map<String, String> blobMap = getBlobMapFromCurrCommit(currCommit);

        blobMap = caculateBlobMap(blobMap, addBlobMap, removeBlobMap);
        List<String> parents = findParents();
        return new Commit(message, blobMap, parents);
    }

    private static Map<String, String> findAddBlobMap() {
        Map<String, String> addBlobMap = new HashMap<>();
        addStage = readAddStage();
        List<Blob> addBlobList = addStage.getBlobList();
        for (Blob b : addBlobList) {
            addBlobMap.put(b.getPath(), b.getBlobID());
        }
        return addBlobMap;
    }

    private static Map<String, String> findRemoveBlobMap() {
        Map<String, String> removeBlobMap = new HashMap<>();
        removeStage = readRemoveStage();
        List<Blob> removeBlobList = removeStage.getBlobList();
        for (Blob b : removeBlobList) {
            removeBlobMap.put(b.getPath(), b.getBlobID());
        }
        return removeBlobMap;
    }


    private static void checkIfNewCommit(Map<String, String> addBlobMap,
                                         Map<String, String> removeBlobMap) {
        if (addBlobMap.isEmpty() && removeBlobMap.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
    }

    private static Map<String, String> getBlobMapFromCurrCommit(Commit currCommit) {
        return currCommit.getPathToBlobID();
    }

    private static Map<String, String> caculateBlobMap(Map<String, String> blobMap,
                                                       Map<String, String> addBlobMap,
                                                       Map<String, String> removeBlobMap) {
        if (!addBlobMap.isEmpty()) {
            for (String path : addBlobMap.keySet()) {
                blobMap.put(path, addBlobMap.get(path));
            }
        }
        if (!removeBlobMap.isEmpty()) {
            for (String path : removeBlobMap.keySet()) {
                blobMap.remove(path);
            }
        }
        return blobMap;
    }

    private static List<String> findParents() {
        List<String> parents = new ArrayList<>();
        currCommit = readCurrCommmit();
        parents.add(currCommit.getID());
        return parents;
    }

    private static Commit readCurrCommmit() {
        String currCommmitID = readCurrCommmitID();
        File CURR_COMMIT_FILE = join(OBJECT_DIR, currCommmitID);
        return readObject(CURR_COMMIT_FILE, Commit.class);
    }

    private static String readCurrCommmitID() {
        String currBranch = readCurrBranch();
        File HEADS_FILE = join(HEADS_DIR, currBranch);
        return readContentsAsString(HEADS_FILE);
    }

    private static void saveHeads(Commit newCommit) {
        currCommit = newCommit;
        String currBranch = readCurrBranch();
        File HEADS_FILE = join(HEADS_DIR, currBranch);
        writeContents(HEADS_FILE, currCommit.getID());
    }

    private static String readCurrBranch() {
        return readContentsAsString(HEAD_FILE);
    }

    /* * rm command funtion */
    public static void rm(String fileName) {
        File file = getFileFromCWD(fileName);
        String filePath = file.getPath();
        addStage = readAddStage();
        currCommit = readCurrCommmit();

        if (addStage.exists(filePath)) {
            addStage.delete(filePath);
            addStage.saveAddStage();
        } else if (currCommit.exists(filePath)) {
            removeStage = readRemoveStage();
            Blob removeBlob = getBlobFromCurrCommitByPath(filePath, currCommit);
            removeStage.add(removeBlob);
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

    private static Blob getBlobFromCurrCommitByPath(String filePath, Commit currCommmit) {
        String blobID = currCommmit.getPathToBlobID().get(filePath);
        return Stage.getBlobByID(blobID);
    }

    /* * log command funtion */
    public static void log() {
        currCommit = readCurrCommmit();
        while (!currCommit.getParentsCommit().isEmpty()) {
            if (isMergeCommit(currCommit)) {
                printMergeCommit(currCommit);
            } else {
                printCommit(currCommit);
            }
            List<String> parentsCommitID = currCommit.getParentsCommit();
            currCommit = readCommitByID(parentsCommitID.get(0));
        }
        printCommit(currCommit);
    }

    private static boolean isMergeCommit(Commit currCommmit) {
        return currCommmit.getParentsCommit().size() == 2;
    }

    private static void printCommit(Commit currCommmit) {
        System.out.println("===");
        printCommitID(currCommmit);
        printCommitDate(currCommmit);
        printCommitMessage(currCommmit);
    }

    private static void printMergeCommit(Commit currCommmit) {
        System.out.println("===");
        printCommitID(currCommmit);
        printMergeMark(currCommmit);
        printCommitDate(currCommmit);
        printCommitMessage(currCommmit);
    }

    private static void printCommitID(Commit currCommmit) {
        System.out.println("commit " + currCommmit.getID());
    }

    private static void printMergeMark(Commit currCommmit) {
        List<String> ParentsCommitID = currCommmit.getParentsCommit();
        String parent1 = ParentsCommitID.get(0);
        String parent2 = ParentsCommitID.get(1);
        System.out.println("Merge: " + parent1.substring(0, 7) + parent2.substring(0, 7));
    }

    private static void printCommitDate(Commit currCommmit) {
        System.out.println("Date: " + currCommmit.getTimeStamp());
    }

    private static void printCommitMessage(Commit currCommmit) {
        System.out.println(currCommmit.getMessage() + "\n");
    }

    private static Commit readCommitByID(String commitID) {
        File CURR_COMMIT_FILE = join(OBJECT_DIR, commitID);
        return readObject(CURR_COMMIT_FILE, Commit.class);
    }

    /* * global-log command funtion */
    public static void global_log() {
        List<String> commitList = plainFilenamesIn(OBJECT_DIR);
        Commit commit;
        for (String id : commitList) {
            try {
                commit = readCommitByID(id);
                if (isMergeCommit(commit)) {
                    printMergeCommit(commit);
                } else {
                    printCommit(commit);
                }
            } catch (Exception ignore) {
            }
        }
    }

    /* * find command funtion */
    public static void find(String findMessage) {
        List<String> commitList = plainFilenamesIn(OBJECT_DIR);
        List<String> idList = new ArrayList<String>();
        Commit commit;
        for (String id : commitList) {
            try {
                commit = readCommitByID(id);
                if (findMessage.equals(commit.getMessage())) {
                    idList.add(id);
                }
            } catch (Exception ignore) {
            }
        }
        printID(idList);
    }

    private static void printID(List<String> idList) {
        if (idList.isEmpty()) {
            System.out.println("Found no commit with that message.");
        } else {
            for (String id : idList) {
                System.out.println(id);
            }
        }
    }

    /* * status command funtion */
    public static void status() {
        printBranches();
        printStagedFile();
        printRemovedFiles();
        printModifiedNotStagedFile();
        printUntrackedFiles();
    }

    private static void printBranches() {
        List<String> branchList = plainFilenamesIn(HEADS_DIR);
        currBranch = readCurrBranch();
        System.out.println("=== Branches ===");
        System.out.println("*" + currBranch);
        if (branchList.size() > 1) {
            for (String branch : branchList) {
                if (!branch.equals(currBranch)) {
                    System.out.println(branch);
                }
            }
        }
        System.out.println();
    }

    private static void printStagedFile() {
        System.out.println("=== Staged Files ===");
        addStage = readAddStage();
        for (Blob b : addStage.getBlobList()) {
            System.out.println(b.getFileName());
        }
        System.out.println();
    }

    private static void printRemovedFiles() {
        System.out.println("=== Removed Files ===");
        removeStage = readRemoveStage();
        for (Blob b : removeStage.getBlobList()) {
            System.out.println(b.getFileName());
        }
        System.out.println();
    }

    private static void printModifiedNotStagedFile() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
    }

    private static void printUntrackedFiles() {
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }
}
















