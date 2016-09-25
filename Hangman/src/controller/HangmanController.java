package controller;

import apptemplate.AppTemplate;
import data.GameData;
import gui.Workspace;
import javafx.animation.AnimationTimer;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import propertymanager.PropertyManager;
import ui.AppMessageDialogSingleton;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static settings.AppPropertyType.*;
import static settings.InitializationParameters.APP_WORKDIR_PATH;

/**
 * @author Ritwik Banerjee
 */
public class HangmanController implements FileController {

    private AppTemplate appTemplate; // shared reference to the application
    private GameData    gamedata;    // shared reference to the game being played, loaded or saved
    private Text[]      progress;    // reference to the text area for the word
    private boolean     success;     // whether or not player was successful
    private int         discovered;  // the number of letters already discovered
    private Button      gameButton;  // shared reference to the "start game" button
    private Label       remains;     // dynamically updated label that indicates the number of remaining guesses
    private boolean     gameover;    // whether or not the current game is already over
    private boolean     startable;
    private boolean     savable;
    private Path        workFile;

    public HangmanController(AppTemplate appTemplate, Button gameButton) {
        this(appTemplate);
        this.gameButton = gameButton;
    }

    public HangmanController(AppTemplate appTemplate) {
        this.appTemplate = appTemplate;
    }

    public void enableGameButton() {
        if (gameButton == null) {
            Workspace workspace = (Workspace) appTemplate.getWorkspaceComponent();
            gameButton = workspace.getStartGame();
        }
        gameButton.setDisable(false);
    }

    public void start() {
        gamedata = new GameData(appTemplate);
        gameover = false;
        success = false;
        startable = false;
        savable = true;
        discovered = 0;
        Workspace gameWorkspace = (Workspace) appTemplate.getWorkspaceComponent();
        appTemplate.getGUI().updateWorkspaceToolbar(startable, savable);  // set toolbar save button disable if its not savable
        HBox remainingGuessBox = gameWorkspace.getRemainingGuessBox();
        HBox guessedLetters    = (HBox) gameWorkspace.getGameTextsPane().getChildren().get(1);

        remains = new Label(Integer.toString(GameData.TOTAL_NUMBER_OF_GUESSES_ALLOWED));
        remainingGuessBox.getChildren().addAll(new Label("Remaining Guesses: "), remains);
        // since remainingGuessBox is HBox, as users click start playing, it adds horizontally.
        initWordGraphics(guessedLetters);
        appTemplate.setAppFileController(this);
        play();
    }

    private void end() {
        System.out.println(success ? "You win!" : "Ah, close but not quite there. The word was \"" + gamedata.getTargetWord() + "\".");
        appTemplate.getGUI().getPrimaryScene().setOnKeyTyped(null);
        gameover = true;
        gameButton.setDisable(true);
        startable = true;
        savable = false; // cannot save a game that is already over
        appTemplate.getGUI().updateWorkspaceToolbar(startable,savable);

    }

    private void initWordGraphics(HBox guessedLetters) {
        char[] targetword = gamedata.getTargetWord().toCharArray(); // ??? why do we need to cast to char[]?
        progress = new Text[targetword.length];
        for (int i = 0; i < progress.length; i++) {
            progress[i] = new Text(Character.toString(targetword[i]));
            progress[i].setVisible(false); // make them invisible first.
        }
        guessedLetters.getChildren().addAll(progress);
    }

    public void play() {
        Workspace gameWorkspace = (Workspace) appTemplate.getWorkspaceComponent();
        gameWorkspace.updateWorkspaceStartButton(startable);
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                appTemplate.getGUI().getPrimaryScene().setOnKeyTyped((KeyEvent event) -> {
                    char guess = event.getCharacter().charAt(0);
                    if (!alreadyGuessed(guess)) {
                        boolean goodguess = false; // if guess is not in targetWord, goodguess is false
                        for (int i = 0; i < progress.length; i++) {
                            if (gamedata.getTargetWord().charAt(i) == guess) {
                                progress[i].setVisible(true);
                                gamedata.addGoodGuess(guess);
                                goodguess = true;
                                discovered++;
                            }
                        }
                        if (!goodguess)
                            gamedata.addBadGuess(guess);

                        success = (discovered == progress.length);
                        remains.setText(Integer.toString(gamedata.getRemainingGuesses()));
                    }
                    if (savable == false) {
                        startable = false;
                        savable = true;
                        appTemplate.getGUI().updateWorkspaceToolbar(startable, savable);
                    }
                });

                // if remainingGuess is <= 0 OR success, then it turns stop.
                if (gamedata.getRemainingGuesses() <= 0 || success)
                    stop();


            }
            @Override
            public void stop() {
                super.stop();
                end();
            }
        };
        timer.start();
    }

    private boolean alreadyGuessed(char c) {
        return gamedata.getGoodGuesses().contains(c) || gamedata.getBadGuesses().contains(c);
    }
    
    @Override
    public void handleNewRequest() {
        AppMessageDialogSingleton messageDialog   = AppMessageDialogSingleton.getSingleton();
        PropertyManager           propertyManager = PropertyManager.getManager();
        boolean                   makenew         = true;
        if (savable)
            try {
                makenew = promptToSave();
            } catch (IOException e) {
                messageDialog.show(propertyManager.getPropertyValue(NEW_ERROR_TITLE), propertyManager.getPropertyValue(NEW_ERROR_MESSAGE));
            }
        if (makenew) {
            appTemplate.getDataComponent().reset();                // reset the data (should be reflected in GUI)
            appTemplate.getWorkspaceComponent().reloadWorkspace(); // load data into workspace
            ensureActivatedWorkspace();                            // ensure workspace is activated
            workFile = null;                                       // new workspace has never been saved to a file

            Workspace gameWorkspace = (Workspace) appTemplate.getWorkspaceComponent();
            gameWorkspace.reinitialize();
            enableGameButton();
        }

        if (gameover) {
            savable = false;
            appTemplate.getGUI().updateWorkspaceToolbar(startable,savable);
            Workspace gameWorkspace = (Workspace) appTemplate.getWorkspaceComponent();
            gameWorkspace.reinitialize();
            enableGameButton();
        }

    }
    
    @Override
    public void handleSaveRequest() throws IOException {
        // todo use promptToSave to ask user the name of file to save. saveData from GameDataFile
        boolean saved = false;
        try {

            if (workFile == null){
                saved = promptToSave();
            }else {
                saved = save(workFile);
            }

        } catch (IOException ioe){
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
            PropertyManager prop = PropertyManager.getManager();
            dialog.show(prop.getPropertyValue(SAVE_ERROR_TITLE), prop.getPropertyValue(SAVE_ERROR_MESSAGE));
        }
        if (saved) {
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
            PropertyManager prop = PropertyManager.getManager();
            dialog.show(prop.getPropertyValue(SAVE_COMPLETED_TITLE), prop.getPropertyValue(SAVE_COMPLETED_MESSAGE));

            savable = false;
            startable = true;
            appTemplate.getGUI().updateWorkspaceToolbar(startable, savable);
        }
    }

    @Override
    public void handleLoadRequest() {

    }
    
    @Override
    public void handleExitRequest() {
        try {
            boolean exit = true;
            if (savable && workFile == null)
                exit = promptToSave();
            if (savable)
                exit = save(workFile);
            if (exit)
                System.exit(0);
        } catch (IOException ioe) {
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
            PropertyManager           props  = PropertyManager.getManager();
            dialog.show(props.getPropertyValue(SAVE_ERROR_TITLE), props.getPropertyValue(SAVE_ERROR_MESSAGE));
        }
    }

    private void ensureActivatedWorkspace() {
        appTemplate.getWorkspaceComponent().activateWorkspace(appTemplate.getGUI().getAppPane());
    }
    
    private boolean promptToSave() throws IOException {
        // todo setup input box and button box for user to type the name of saving file.
        PropertyManager           propertyManager = PropertyManager.getManager();


        FileChooser fileChooser = new FileChooser();

        URL workDirURL  = AppTemplate.class.getClassLoader().getResource("");///Users/eifu/IdeaProjects/TheHangmanGame/out/production/Hangman/
        File dir_f = new File(workDirURL.getPath()+"/saved");

        if(!dir_f.exists() ) {
            try {
                dir_f.mkdir();
            }catch (Exception e){
                e.printStackTrace();
            }
        }


        fileChooser.setInitialDirectory(dir_f);
        fileChooser.setTitle(propertyManager.getPropertyValue(SAVE_WORK_TITLE));

        FileChooser.ExtensionFilter fileExtensions = new FileChooser.ExtensionFilter("JSON file","*.json");

        fileChooser.getExtensionFilters().add(fileExtensions);

        File f = fileChooser.showSaveDialog(appTemplate.getGUI().getWindow());

        if ( f != null ){
            save(f.toPath());
            workFile = f.toPath();
            return true;
        }else{
            throw new IOException();
        }
    }

    /**
     * A helper method to save work. It saves the work, marks the current work file as saved, notifies the user, and
     * updates the appropriate controls in the user interface
     *
     * @param target The file to which the work will be saved.
     * @throws IOException
     */
    private boolean save(Path target) throws IOException {
        // todo save data, update the tool bar, (disable save button, enable exit button)

        try{
            appTemplate.getFileComponent().saveData(gamedata, target);
        }catch (IOException ioe){
            return false;
        }
        return true;
    }
}
