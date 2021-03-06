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
 * @author Eifu Tomita
 */
public class GameData implements AppDataComponent {

    public static final  int TOTAL_NUMBER_OF_GUESSES_ALLOWED = 10;
    private static final int TOTAL_NUMBER_OF_STORED_WORDS    = 330622;

    private String         targetWord;
    private boolean         hint_reserved;
    private Set<Character> goodGuesses;
    private Set<Character> badGuesses;
    private int            remainingGuesses;
    public  AppTemplate    appTemplate;

    public GameData(AppTemplate appTemplate) {
        this(appTemplate, false);
    }

    public GameData(AppTemplate appTemplate, boolean initiateGame) {
        if (initiateGame) {
            this.appTemplate = appTemplate;
            this.targetWord = setTargetWord();
            this.hint_reserved = true;
            this.goodGuesses = new HashSet<>();
            this.badGuesses = new HashSet<>();
            this.remainingGuesses = TOTAL_NUMBER_OF_GUESSES_ALLOWED;
        } else {
            this.appTemplate = appTemplate;
        }
    }

    public void init() {
        this.targetWord = setTargetWord();
        this.hint_reserved = true;
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

    public boolean getDifficulty(){
        Set<Character> diff = new HashSet<>();
        if (targetWord == null){
            return false;
        }
        char[] c_array = targetWord.toCharArray();
        for (int i = 0; i < c_array.length;i++){
            diff.add(c_array[i]);
        }
        return diff.size() > 7;
    }

    public String getTargetWord() {
        return targetWord;
    }

    private String setTargetWord() {
        URL wordsResource = getClass().getClassLoader().getResource("words/words.txt");
        assert wordsResource != null;

        int toSkip = new Random().nextInt(TOTAL_NUMBER_OF_STORED_WORDS);

        String s ="";
        char[] c_array;
        boolean still_not_find = true;
        try {
            Stream<String> lines = Files.lines(Paths.get(wordsResource.toURI()));

            s = lines.skip(toSkip).findFirst().get();

            while (still_not_find){
                c_array = s.toCharArray();
                still_not_find = false;
                for (int i = 0; i < c_array.length;i++){
                    if (!Character.isLetter(c_array[i])) {
                        still_not_find = true;
                        lines.close();
                        lines = Files.lines(Paths.get(wordsResource.toURI()));
                        s = lines.skip((toSkip+1)/TOTAL_NUMBER_OF_STORED_WORDS).findFirst().get();
                        break;
                    }
                }
            }

            return s;

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

    public boolean getHintReserved(){return hint_reserved;}

    public GameData setHintReserved(boolean hint_reserved){
        this.hint_reserved = hint_reserved;
        return this;
    }

    public Set<Character> getGoodGuesses() {
        return goodGuesses;
    }

    public GameData setGoodGuesses(Set<Character> goodGuesses) {
        this.goodGuesses = goodGuesses;
        return this;
    }

    public Set<Character> getBadGuesses() {
        return badGuesses;
    }

    public GameData setBadGuesses(Set<Character> badGuesses) {
        this.badGuesses = badGuesses;
        return this;
    }

    public int getRemainingGuesses() {
        return remainingGuesses;
    }

    public GameData setRemainingGuesses(int remainingGuesses){
        this.remainingGuesses = remainingGuesses;
        return this;
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
