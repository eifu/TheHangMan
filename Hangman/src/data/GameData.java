package data;

import apptemplate.AppTemplate;
import components.AppDataComponent;
import controller.GameError;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Ritwik Banerjee
 */
public class GameData implements AppDataComponent {

    public static final  int TOTAL_NUMBER_OF_GUESSES_ALLOWED = 10;
    private static final int TOTAL_NUMBER_OF_STORED_WORDS    = 330622;  // number of lines in words/words.txt

    private String         targetWord;
    private Set<Character> goodGuesses;
    private Set<Character> badGuesses;
    private int            remainingGuesses;
    public  AppTemplate    appTemplate;

    public GameData(AppTemplate appTemplate) {
        this.appTemplate = appTemplate;
        this.targetWord = setTargetWord();
        this.goodGuesses = new HashSet<>();
        this.badGuesses = new HashSet<>();
        this.remainingGuesses = TOTAL_NUMBER_OF_GUESSES_ALLOWED;
    }

    @Override
    public void reset() {
        this.targetWord = null;
        this.goodGuesses = new HashSet<>();
        this.badGuesses = new HashSet<>();
        this.remainingGuesses = TOTAL_NUMBER_OF_GUESSES_ALLOWED;
        appTemplate.getWorkspaceComponent().reloadWorkspace();
    }

    public String getTargetWord() {
        return targetWord;
    }

    private String setTargetWord() {
        /* This function is called when GameData class is initialized.
         * getClass(): Returns the runtime class of this Object.
         * getClassLoader(): Returns the class loader for the class.
         * getResource(): Finds the resource with the given name, returns a URL object for reading the resource
         */
        URL wordsResource = getClass().getClassLoader().getResource("words/words.txt");
        assert wordsResource != null;

        int toSkip = new Random().nextInt(TOTAL_NUMBER_OF_STORED_WORDS);
        try (Stream<String> lines = Files.lines(Paths.get(wordsResource.toURI()))) {
            return lines.skip(toSkip).findFirst().get();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            System.exit(1);
        }

        throw new GameError("Unable to load initial target word.");
    }

    public GameData setTargetWord(String targetWord) {
        this.targetWord = targetWord;
        return this;
    }

    public Set<Character> getGoodGuesses() {
        return goodGuesses;
    }

    @SuppressWarnings("unused")
    public GameData setGoodGuesses(Set<Character> goodGuesses) {
        this.goodGuesses = goodGuesses;
        return this;
    }

    public Set<Character> getBadGuesses() {
        return badGuesses;
    }

    @SuppressWarnings("unused")
    public GameData setBadGuesses(Set<Character> badGuesses) {
        this.badGuesses = badGuesses;
        return this;
    }

    public int getRemainingGuesses() {
        return remainingGuesses;
    }

    public void addGoodGuess(char c) {
        goodGuesses.add(c);
    }

    public void addBadGuess(char c) {
        if (!badGuesses.contains(c)) {
            badGuesses.add(c);
            remainingGuesses--;
        }
    }


}
