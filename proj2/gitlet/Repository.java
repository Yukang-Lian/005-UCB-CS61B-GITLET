package gitlet;

import java.io.File;
import java.nio.file.Paths;
import java.util.Date;

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
    public static final File STAGE = join(GITLET_DIR, "stage");

    public static Commit currCommmit;

    public static Stage addStage = new Stage();


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
        writeContents(HEAD_FILE, "ref: refs/heads/master");
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
        if (addStage.isNewBlob(blob)) {
            blob.save();
            if (addStage.isFilePathExists(blob.getPath())) {
                addStage.delete(blob);
            }
            addStage.add(blob);
        }
    }

    private static File getFileFromCWD(String file) {
        return Paths.get(file).isAbsolute()
                ? new File(file)
                : join(CWD, file);
    }

    /* * add command funtion */
    public static void commit(String message){

    }
}
