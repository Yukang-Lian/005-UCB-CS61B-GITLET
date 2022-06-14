package capers;

import java.io.File;
import java.io.Serializable;

import static capers.CapersRepository.CAPERS_FOLDER;
import static capers.Utils.*;

/**
 * Represents a dog that can be serialized.
 *
 * @author TODO
 */
public class Dog implements Serializable { // TODO

    /**
     * Folder that dogs live in.
     */
    static final File DOG_FOLDER = Utils.join(CAPERS_FOLDER, "dogs"); // TODO (hint: look at the `join`
    //      function in Utils)

    /**
     * Age of dog.
     */
    private int age;
    /**
     * Breed of dog.
     */
    private String breed;
    /**
     * Name of dog.
     */
    private String name;

    /**
     * Creates a dog object with the specified parameters.
     *
     * @param name  Name of dog
     * @param breed Breed of dog
     * @param age   Age of dog
     */
    public Dog(String name, String breed, int age) {
        this.age = age;
        this.breed = breed;
        this.name = name;
    }

    /**
     * Reads in and deserializes a dog from a file with name NAME in DOG_FOLDER.
     *
     * @param name Name of dog to load
     * @return Dog read from file
     */
    public static Dog fromFile(String name) {
        // TODO (hint: look at the Utils file)
        File path=Utils.join(DOG_FOLDER,name+".txt");
        if(path.exists()){
            return Utils.readObject(path,Dog.class);
        }else{
            return null;
        }
    }

    /**
     * Increases a dog's age and celebrates!
     */
    public void haveBirthday() {
        age += 1;
        System.out.println(toString());
        System.out.println("Happy birthday! Woof! Woof!");
    }

    /**
     * Saves a dog to a file for future use.
     */
    public void saveDog() {
        // TODO (hint: don't forget dog names are unique)
        if (!DOG_FOLDER.exists()) {
            boolean success = DOG_FOLDER.mkdir();
            if (!success) {
                throw new java.lang.Error("Cannot create dog file");
            }
        }
        File path = Utils.join(DOG_FOLDER, this.name + ".txt");
        Utils.writeObject(path, new Dog(this.name, this.breed, this.age));
    }

    @Override
    public String toString() {
        return String.format(
                "Woof! My name is %s and I am a %s! I am %d years old! Woof!",
                name, breed, age);
    }

}
