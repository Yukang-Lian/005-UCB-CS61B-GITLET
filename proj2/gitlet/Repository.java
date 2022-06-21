package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

import static gitlet.Stage.getBlobByID;
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
        currCommit = readCurrCommmit();
        addStage = readAddStage();
        removeStage = readRemoveStage();
        if (!currCommit.getPathToBlobID().containsValue(blob.getBlobID()) ||
                !removeStage.isNewBlob(blob)) {
            if (addStage.isNewBlob(blob)) {
                if (removeStage.isNewBlob(blob)) {
                    blob.save();
                    if (addStage.isFilePathExists(blob.getPath())) {
                        addStage.delete(blob);
                    }
                    addStage.add(blob);
                    addStage.saveAddStage();
                } else {
                    removeStage.delete(blob);
                    removeStage.saveRemoveStage();
                }
            }
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
        saveNewCommit(newCommit);
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

    private static void saveNewCommit(Commit newCommit) {
        newCommit.save();
        addStage.clear();
        addStage.saveAddStage();
        removeStage.clear();
        removeStage.saveRemoveStage();
        saveHeads(newCommit);
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
        return getBlobByID(blobID);
    }

    /* * log command funtion */
    public static void log() {
        currCommit = readCurrCommmit();
        while (!currCommit.getParentsCommitID().isEmpty()) {
            if (isMergeCommit(currCommit)) {
                printMergeCommit(currCommit);
            } else {
                printCommit(currCommit);
            }
            List<String> parentsCommitID = currCommit.getParentsCommitID();
            currCommit = readCommitByID(parentsCommitID.get(0));
        }
        printCommit(currCommit);
    }

    private static boolean isMergeCommit(Commit currCommmit) {
        return currCommmit.getParentsCommitID().size() == 2;
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
        List<String> parentsCommitID = currCommmit.getParentsCommitID();
        String parent1 = parentsCommitID.get(0);
        String parent2 = parentsCommitID.get(1);
        System.out.println("Merge: " + parent1.substring(0, 7) + " " + parent2.substring(0, 7));
    }

    private static void printCommitDate(Commit currCommmit) {
        System.out.println("Date: " + currCommmit.getTimeStamp());
    }

    private static void printCommitMessage(Commit currCommmit) {
        System.out.println(currCommmit.getMessage() + "\n");
    }

    private static Commit readCommitByID(String commitID) {
        if (commitID.length() == 40) {
            File CURR_COMMIT_FILE = join(OBJECT_DIR, commitID);
            if (!CURR_COMMIT_FILE.exists()) {
                return null;
            }
            return readObject(CURR_COMMIT_FILE, Commit.class);
        } else {
            List<String> objectID = plainFilenamesIn(OBJECT_DIR);
            for (String o : objectID) {
                if (commitID.equals(o.substring(0, commitID.length()))) {
                    return readObject(join(OBJECT_DIR, o), Commit.class);
                }
            }
            return null;
        }
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

    /* * checkout command funtion */
    /* * case 1 */
    public static void checkout(String fileName) {
        Commit currCommmit = readCurrCommmit();
        List<String> fileNames = currCommmit.getFileNames();
        if (fileNames.contains(fileName)) {
            Blob blob = currCommmit.getBlobByFileName(fileName);
            writeBlobToCWD(blob);
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }

    private static void writeBlobToCWD(Blob blob) {
        File fileName = join(CWD, blob.getFileName());
        byte[] bytes = blob.getBytes();
        writeContents(fileName, new String(bytes, StandardCharsets.UTF_8));
    }

    /* * case 2 */
    public static void checkout(String commitID, String fileName) {
        Commit commit = readCommitByID(commitID);
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        List<String> fileNames = commit.getFileNames();
        if (fileNames.contains(fileName)) {
            Blob blob = commit.getBlobByFileName(fileName);
            writeBlobToCWD(blob);
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }

    /* * case 3 */
    public static void checkoutBranch(String branchName) {
        checkIfCheckedCurrBranch(branchName);
        checkIfCheckedBranchExists(branchName);

        currCommit = readCurrCommmit();
        Commit newCommit = readCommitByBranchName(branchName);
        changeCommitTo(newCommit);

        changeBranchTo(branchName);
    }

    private static void checkIfCheckedBranchExists(String branchName) {
        List<String> allBranch = readAllBranch();
        if (!allBranch.contains(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
    }

    private static void checkIfCheckedCurrBranch(String branchName) {
        currBranch = readCurrBranch();
        if (branchName.equals(currBranch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
    }

    private static List<String> readAllBranch() {
        return plainFilenamesIn(HEADS_DIR);
    }

    private static Commit readCommitByBranchName(String branchName) {
        File branchFileName = join(HEADS_DIR, branchName);
        String newCommitID = readContentsAsString(branchFileName);
        return readCommitByID(newCommitID);
    }

    private static void changeCommitTo(Commit newCommit) {
        List<String> onlyCurrCommitTracked = findOnlyCurrCommitTracked(newCommit);
        List<String> bothCommitTracked = findBothCommitTracked(newCommit);
        List<String> onlyNewCommitTracked = findOnlyNewCommitTracked(newCommit);
        deleteFiles(onlyCurrCommitTracked);
        overwriteFiles(bothCommitTracked, newCommit);
        writeFiles(onlyNewCommitTracked, newCommit);
        clearAllStage();
    }

    private static List<String> findOnlyCurrCommitTracked(Commit newCommit) {
        List<String> newCommitFiles = newCommit.getFileNames();
        List<String> onlyCurrCommitTracked = currCommit.getFileNames();
        for (String s : newCommitFiles) {
            onlyCurrCommitTracked.remove(s);
        }
        return onlyCurrCommitTracked;
    }

    private static List<String> findBothCommitTracked(Commit newCommit) {
        List<String> newCommitFiles = newCommit.getFileNames();
        List<String> currCommitFiles = currCommit.getFileNames();
        List<String> bothCommitTracked = new ArrayList<>();
        for (String s : newCommitFiles) {
            if (currCommitFiles.contains(s)) {
                bothCommitTracked.add(s);
            }
        }
        return bothCommitTracked;
    }

    private static List<String> findOnlyNewCommitTracked(Commit newCommit) {
        List<String> currCommitFiles = currCommit.getFileNames();
        List<String> onlyNewCommitTracked = newCommit.getFileNames();
        for (String s : currCommitFiles) {
            onlyNewCommitTracked.remove(s);
        }
        return onlyNewCommitTracked;
    }

    private static void deleteFiles(List<String> onlyCurrCommitTracked) {
        if (onlyCurrCommitTracked.isEmpty()) {
            return;
        }
        for (String fileName : onlyCurrCommitTracked) {
            File file = join(CWD, fileName);
            restrictedDelete(file);
        }
    }

    private static void overwriteFiles(List<String> bothCommitTracked, Commit newCommit) {
        if (bothCommitTracked.isEmpty()) {
            return;
        }
        for (String fileName : bothCommitTracked) {
            Blob blob = newCommit.getBlobByFileName(fileName);
            writeBlobToCWD(blob);
        }
    }

    private static void writeFiles(List<String> onlyNewCommitTracked, Commit newCommit) {
        if (onlyNewCommitTracked.isEmpty()) {
            return;
        }
        for (String fileName : onlyNewCommitTracked) {
            File file = join(CWD, fileName);
            if (file.exists()) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        overwriteFiles(onlyNewCommitTracked, newCommit);
    }

    private static void clearAllStage() {
        addStage = readAddStage();
        addStage.clear();
        addStage.saveAddStage();
        removeStage = readRemoveStage();
        removeStage.clear();
        removeStage.saveRemoveStage();
    }

    private static void changeBranchTo(String headName) {
        writeContents(HEAD_FILE, headName);
    }

    /* * branch command funtion */
    public static void branch(String branchName) {
        checkIfNewBranch(branchName);
        addNewBranchToHeads(branchName);
    }

    private static void checkIfNewBranch(String branchName) {
        List<String> allBranches = plainFilenamesIn(HEADS_DIR);
        if (allBranches.contains(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
    }

    private static void addNewBranchToHeads(String branchName) {
        File newBranchFile = join(HEADS_DIR, branchName);
        currCommit = readCurrCommmit();
        writeContents(newBranchFile, currCommit.getID());
    }

    /* * rm_branch command funtion */
    public static void rm_branch(String branchName) {
        checkIfCurrentBranch(branchName);
        checkIfBranchExists(branchName);
        removeBranch(branchName);
    }

    private static void checkIfCurrentBranch(String branchName) {
        currBranch = readCurrBranch();
        if (currBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
    }

    private static void checkIfBranchExists(String branchName) {
        List<String> allBranch = readAllBranch();
        if (!allBranch.contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
    }

    private static void removeBranch(String branchName) {
        File fileName = join(HEADS_DIR, branchName);
        if (!fileName.isDirectory()) {
            fileName.delete();
        }
    }

    /* * reset command funtion */
    public static void reset(String commitID) {
        checkIfCommitIDExists(commitID);

        currCommit = readCurrCommmit();
        Commit newCommit = readCommitByID(commitID);
        changeCommitTo(newCommit);

        currBranch = readCurrBranch();
        changeBranchHeadTo(commitID, currBranch);
    }

    private static void checkIfCommitIDExists(String commitID) {
        Commit commit = readCommitByID(commitID);
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
    }

    private static void changeBranchHeadTo(String commitID, String branchName) {
        File branchFile = join(HEADS_DIR, branchName);
        writeContents(branchFile, commitID);
    }

    /* * merge command funtion */
    public static void merge(String mergeBranch) {
        currBranch = readCurrBranch();
        checkIfStageEmpty();
        checkIfBranchExists(mergeBranch);
        checkIfMergeWithSelf(mergeBranch);

        currCommit = readCurrCommmit();
        Commit mergeCommit = readCommitByBranchName(mergeBranch);
        Commit splitPoint = findSplitPoint(currCommit, mergeCommit);
        checkIfSplitPintIsGivenBranch(splitPoint, mergeCommit);
        checkIfSplitPintIsCurrBranch(splitPoint, mergeBranch);
        Map<String, String> currCommitBlobs = currCommit.getPathToBlobID();

        String message = "Merged " + mergeBranch + " into " + currBranch + ".";
        String currBranchCommitID = readCommitByBranchName(currBranch).getID();
        String mergeBranchCommitID = readCommitByBranchName(mergeBranch).getID();
        List<String> parents = new ArrayList<>(List.of(currBranchCommitID, mergeBranchCommitID));
        Commit newCommit = new Commit(message, currCommitBlobs, parents);

        Commit mergedCommit = mergeFilesToNewCommit(splitPoint, newCommit, mergeCommit);
        saveNewCommit(mergedCommit);
    }

    private static void checkIfStageEmpty() {
        addStage = readAddStage();
        removeStage = readRemoveStage();
        if (!(addStage.isEmpty() && removeStage.isEmpty())) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
    }

    private static void checkIfMergeWithSelf(String branchName) {
        if (currBranch.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    private static Commit findSplitPoint(Commit commit1, Commit commit2) {
        Map<String, Integer> commit1IDToLength = caculateCommitMap(commit1, 0);
        Map<String, Integer> commit2IDToLength = caculateCommitMap(commit2, 0);
        return caculateSplitPoint(commit1IDToLength, commit2IDToLength);
    }

    private static Map<String, Integer> caculateCommitMap(Commit commit, int length) {
        Map<String, Integer> map = new HashMap<>();
        if (commit.getParentsCommitID().isEmpty()) {
            map.put(commit.getID(), length);
            return map;
        }
        map.put(commit.getID(), length);
        length++;
        for (String id : commit.getParentsCommitID()) {
            Commit parent = readCommitByID(id);
            map.putAll(caculateCommitMap(parent, length));
        }
        return map;
    }

    private static Commit caculateSplitPoint(Map<String, Integer> map1, Map<String, Integer> map2) {
        int minLength = Integer.MAX_VALUE;
        String minID = "";
        for (String id : map1.keySet()) {
            if (map2.containsKey(id) && map2.get(id) < minLength) {
                minID = id;
                minLength = map2.get(id);
            }
        }
        return readCommitByID(minID);
    }

    private static void checkIfSplitPintIsGivenBranch(Commit splitPoint, Commit mergeCommit) {
        if (splitPoint.getID().equals(mergeCommit.getID())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
    }

    private static void checkIfSplitPintIsCurrBranch(Commit splitPoint, String mergeBranch) {
        if (splitPoint.getID().equals(currCommit.getID())) {
            System.out.println("Current branch fast-forwarded.");
            checkoutBranch(mergeBranch);
        }
    }

    private static Commit mergeFilesToNewCommit(Commit splitPoint, Commit newCommit, Commit mergeCommit) {

        List<String> allFiles = caculateAllFiles(splitPoint, newCommit, mergeCommit);

        /*
         * case 1 5 6: write mergeCommit files into newCommit
         * case 1: overwrite files
         * case 5: write files
         * case 6: delete files
         */
        List<String> overwriteFiles = caculateOverwriteFiles(allFiles, splitPoint, newCommit, mergeCommit);
        List<String> writeFiles = caculateWriteFiles(allFiles, splitPoint, newCommit, mergeCommit);
        List<String> deleteFiles = caculateDeleteFiles(allFiles, splitPoint, newCommit, mergeCommit);

        overwriteFiles(changeBlobIDListToFileNameList(overwriteFiles), mergeCommit);
        writeFiles(changeBlobIDListToFileNameList(writeFiles), mergeCommit);
        deleteFiles(changeBlobIDListToFileNameList(deleteFiles));

        /* * case 3-1: deal conflict */
        checkIfConflict(allFiles, splitPoint, newCommit, mergeCommit);

        /* * case 2 4 7 3-1: do nothing */
        //nothing to do here

        return caculateMergedCommit(newCommit, overwriteFiles, writeFiles, deleteFiles);
    }

    private static List<String> caculateAllFiles(Commit splitPoint, Commit newCommit, Commit mergeCommit) {
        List<String> allFiles = new ArrayList<String>(splitPoint.getBlobIDList());
        allFiles.addAll(newCommit.getBlobIDList());
        allFiles.addAll(mergeCommit.getBlobIDList());
        Set<String> set = new HashSet<String>(allFiles);
        allFiles.clear();
        allFiles.addAll(set);
        return allFiles;
    }

    private static void checkIfConflict(List<String> allFiles, Commit splitPoint, Commit newCommit, Commit mergeCommit) {
        Map<String, String> splitPointMap = splitPoint.getPathToBlobID();
        Map<String, String> newCommitMap = newCommit.getPathToBlobID();
        Map<String, String> mergeCommitMap = mergeCommit.getPathToBlobID();

        boolean conflict = false;
        for (String blobID : allFiles) {
            String path = getBlobByID(blobID).getPath();
            int commonPath = 0;
            if (splitPointMap.containsKey(path)) {
                commonPath += 1;
            }
            if (newCommitMap.containsKey(path)) {
                commonPath += 2;
            }
            if (mergeCommitMap.containsKey(path)) {
                commonPath += 4;
            }
            if ((commonPath == 3 && (!splitPointMap.get(path).equals(newCommitMap.get(path)))) ||
                    (commonPath == 5 && (!splitPointMap.get(path).equals(mergeCommitMap.get(path)))) ||
                    (commonPath == 6 && (!newCommitMap.get(path).equals(mergeCommitMap.get(path)))) ||
                    (commonPath == 7 &&
                            (!splitPointMap.get(path).equals(newCommitMap.get(path))) &&
                            (!splitPointMap.get(path).equals(mergeCommitMap.get(path))) &&
                            (!newCommitMap.get(path).equals(mergeCommitMap.get(path))))) {

                conflict = true;
                String currBranchContents = "";
                if (newCommitMap.containsKey(path)) {
                    Blob newCommitBlob = getBlobByID(newCommitMap.get(path));
                    currBranchContents = new String(newCommitBlob.getBytes(), StandardCharsets.UTF_8);
                }

                String givenBranchContents = "";
                if (mergeCommitMap.containsKey(path)) {
                    Blob mergeCommitBlob = getBlobByID(mergeCommitMap.get(path));
                    givenBranchContents = new String(mergeCommitBlob.getBytes(), StandardCharsets.UTF_8);
                }

                String conflictContents = "<<<<<<< HEAD\n" + currBranchContents + "=======\n" + givenBranchContents + ">>>>>>>\n";
                String fileName = getBlobByID(blobID).getFileName();
                File conflictFile = join(CWD, fileName);
                writeContents(conflictFile, conflictContents);
            }

        }

        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }


    private static List<String> caculateOverwriteFiles(List<String> allFiles, Commit splitPoint, Commit
            newCommit, Commit mergeCommit) {
        Map<String, String> splitPointMap = splitPoint.getPathToBlobID();
        Map<String, String> newCommitMap = newCommit.getPathToBlobID();
        Map<String, String> mergeCommitMap = mergeCommit.getPathToBlobID();
        List<String> overwriteFiles = new ArrayList<>();
        for (String path : splitPointMap.keySet()) {
            if (newCommitMap.containsKey(path) && mergeCommitMap.containsKey(path)) {
                if ((splitPointMap.get(path).equals(newCommitMap.get(path))) && (!splitPointMap.get(path).equals(mergeCommitMap.get(path)))) {
                    overwriteFiles.add(mergeCommitMap.get(path));
                }
            }
        }
        return overwriteFiles;
    }


    private static List<String> caculateWriteFiles(List<String> allFiles, Commit splitPoint, Commit
            newCommit, Commit mergeCommit) {
        Map<String, String> splitPointMap = splitPoint.getPathToBlobID();
        Map<String, String> newCommitMap = newCommit.getPathToBlobID();
        Map<String, String> mergeCommitMap = mergeCommit.getPathToBlobID();
        List<String> writeFiles = new ArrayList<>();
        for (String path : mergeCommitMap.keySet()) {
            if ((!splitPointMap.containsKey(path)) && (!newCommitMap.containsKey(path))) {
                writeFiles.add(mergeCommitMap.get(path));
            }
        }
        return writeFiles;
    }

    private static List<String> caculateDeleteFiles(List<String> allFiles, Commit splitPoint, Commit
            newCommit, Commit mergeCommit) {
        Map<String, String> splitPointMap = splitPoint.getPathToBlobID();
        Map<String, String> newCommitMap = newCommit.getPathToBlobID();
        Map<String, String> mergeCommitMap = mergeCommit.getPathToBlobID();
        List<String> deleteFiles = new ArrayList<>();
        for (String path : splitPointMap.keySet()) {
            if (newCommitMap.containsKey(path) && (!mergeCommitMap.containsKey(path))) {
                deleteFiles.add(newCommitMap.get(path));
            }
        }
        return deleteFiles;
    }

    private static List<String> changeBlobIDListToFileNameList(List<String> blobIDList) {
        List<String> fileNameList = new ArrayList<>();
        Blob b;
        for (String id : blobIDList) {
            b = getBlobByID(id);
            fileNameList.add(b.getFileName());
        }
        return fileNameList;
    }

    private static Commit caculateMergedCommit(Commit newCommit, List<String> overwriteFiles, List<String> writeFiles, List<String> deleteFiles) {
        Map<String, String> mergedCommitBlobs = newCommit.getPathToBlobID();
        if (!overwriteFiles.isEmpty()) {
            for (String blobID : overwriteFiles) {
                Blob b = getBlobByID(blobID);
                mergedCommitBlobs.put(b.getPath(), blobID);
            }
        }
        if (!writeFiles.isEmpty()) {
            for (String blobID : writeFiles) {
                Blob b = getBlobByID(blobID);
                mergedCommitBlobs.put(b.getPath(), blobID);
            }
        }
        if (!deleteFiles.isEmpty()) {
            for (String blobID : overwriteFiles) {
                Blob b = getBlobByID(blobID);
                mergedCommitBlobs.remove(b.getPath());
            }
        }
        return new Commit(newCommit.getMessage(), mergedCommitBlobs, newCommit.getParentsCommitID());
    }
}
