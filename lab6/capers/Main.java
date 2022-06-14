package capers;

import java.io.File;

import static capers.Utils.*;

/**
 * Canine Capers: A Gitlet Prelude.
 *
 * @author TODO
 */
public class Main {
    /**
     * Runs one of three commands:
     * story [text] -- Appends "text" + a newline to a story file in the
     * .capers directory. Additionally, prints out the
     * current story.
     * <p>
     * dog [name] [breed] [age] -- Persistently creates a dog with
     * the specified parameters; should also print
     * the dog's toString(). Assume dog names are
     * unique.
     * <p>
     * birthday [name] -- Advances a dog's age persistently
     * and prints out a celebratory message.
     * <p>
     * All persistent data should be stored in a ".capers"
     * directory in the current working directory.
     * <p>
     * Recommended structure (you do not have to follow):
     * <p>
     * *YOU SHOULD NOT CREATE THESE MANUALLY,
     * YOUR PROGRAM SHOULD CREATE THESE FOLDERS/FILES*
     * <p>
     * .capers/ -- top level folder for all persistent data in your lab12 folder
     * - dogs/ -- folder containing all of the persistent data for dogs
     * - story -- file containing the current story
     *
     * @param args arguments from the command line
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            Utils.exitWithError("Must have at least one argument");
        }

        CapersRepository.setupPersistence();
        String text;
        switch (args[0]) {
            case "story":
                /* This call has been handled for you. The rest will be similar. */
                validateNumArgs("story", args, 2);
                text = args[1];
                CapersRepository.writeStory(text);
                break;
            case "dog":
                validateNumArgs("dog", args, 4);
                // TODO: make a dog
                CapersRepository.makeDog(args[1], args[2], Integer.parseInt(args[3]));
                break;
            case "birthday":
                validateNumArgs("birthday", args, 2);
                // TODO: celebrate this dog's birthday
                CapersRepository.celebrateBirthday(args[1]);
                break;
            default:
                exitWithError(String.format("Unknown command: %s", args[0]));
        }
        return;
    }

    /**
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     *
     * @param cmd  Name of command you are validating
     * @param args Argument array from command line
     * @param n    Number of expected arguments
     */
    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw new RuntimeException(
                    String.format("Invalid number of arguments for: %s.", cmd));
        }
    }
}
