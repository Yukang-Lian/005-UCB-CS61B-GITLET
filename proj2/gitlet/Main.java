package gitlet;

import java.sql.ResultSet;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 * <p>
 * * @author abmdocrt
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch (firstArg) {

            /* * init command */
            case "init":
                // TODO: handle the `init` command
                validArgs(args, 1);

                Repository.init();
                break;

            /* * add command */
            case "add":
                // TODO: handle the `add [filename]` command
                validArgs(args, 2);

                Repository.checkIfInitialized();

                Repository.add(args[1]);
                break;
            // TODO: FILL THE REST IN

            /* * commit command */
            case "commit":
                validArgs(args, 2);

                Repository.checkIfInitialized();

                Repository.commit(args[1]);
                break;

            /* * rm command */
            case "rm":
                validArgs(args, 2);

                Repository.checkIfInitialized();

                Repository.rm(args[1]);
                break;

            /* * log command */
            case "log":
                validArgs(args, 1);

                Repository.checkIfInitialized();

                Repository.log();
                break;

            /* * global-log command */
            case "global-log":
                validArgs(args, 1);

                Repository.checkIfInitialized();

                Repository.global_log();
                break;

            /* * find command */
            case "find":
                validArgs(args, 2);

                Repository.checkIfInitialized();

                Repository.find(args[1]);
                break;

            /* * status command */
            case "status":
                validArgs(args, 1);

                Repository.checkIfInitialized();

                Repository.status();
                break;

            /* * status command */
            case "checkout":

                Repository.checkIfInitialized();

                Repository repository = new Repository();
                switch (args.length) {
                    case 3:
                        if (!args[1].equals("--")) {
                            System.out.println("Incorrect operands.");
                            System.exit(0);
                        }
                        /* * checkout -- [file name] */
                        repository.checkout(args[2]);
                        break;

                    case 4:
                        if (!args[2].equals("--")) {
                            System.out.println("Incorrect operands.");
                            System.exit(0);
                        }
                        /* * checkout [commit id] -- [file name] */
                        repository.checkout(args[1], args[3]);
                        break;

                    case 2:
                        /* * checkout [branch name] */
                        repository.checkoutBranch(args[1]);
                        break;

                    default:
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                }
                break;

            /* * branch command */
            case "branch":
                validArgs(args, 2);

                Repository.checkIfInitialized();

                Repository.branch(args[1]);
                break;

            /* * rm-branch command */
            case "rm-branch":
                validArgs(args, 2);

                Repository.checkIfInitialized();

                Repository.rm_branch(args[1]);
                break;

            /* * reset command */
            case "reset":
                validArgs(args, 2);

                Repository.checkIfInitialized();

                Repository.reset(args[1]);
                break;

            /* * merge command */
            case "merge":
                validArgs(args, 2);

                Repository.checkIfInitialized();
                Repository.merge(args[1]);
                break;

            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    private static void validArgs(String[] args, int num) {
        if (args.length != num) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
