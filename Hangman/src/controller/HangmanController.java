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
import settings.InitializationParameters;
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
    private Button startGameButton;  // shared reference to the "start game" button
    private Label       remains;     // dynamically updated label that indicates the number of remaining guesses
    private boolean     gameover;    // whether or not the current game is already over
    private boolean     startable;
    private boolean     loadable;
    private boolean     savable;
    private Path        workFile;
    private AnimationTimer timer;

    public HangmanController(AppTemplate appTemplate, Button startGameButton) {
        this(appTemplate);
        this.startGameButton = startGameButton;
    }

    public HangmanController(AppTemplate appTemplate) {
        this.appTemplate = appTemplate;
    }

    public void enableGameButton() {
        if (startGameButton == null) {
            Workspace workspace = (Workspace) appTemplate.getWorkspaceComponent();
            startGameButton = workspace.getStartGame();
        }
        startGameButton.setDisable(false);
    }

    public void start() {
        gamedata = new GameData(appTemplate);
        gameover = false;
        success = false;
        startable = false;
        loadable = false;
        savable = true;
        discovered = 0;
        Workspace gameWorkspace = (Workspace) appTemplate.getWorkspaceComponent();
        appTemplate.getGUI().updateWorkspaceToolbar(startable, loadable, savable);  // set toolbar save button disable if its not savable
        HBox remainingGuessBox = gameWorkspace.getRemainingGuessBox();
        HBox guessedLetters    = (HBox) gameWorkspace.getGameTextsPane().getChildren().get(1);

        remains = new Label(Integer.toString(GameData.TOTAL_NUMBER_OF_GUESSES_ALLOWED));
        remainingGuessBox.getChildren().addAll(new Label("Remaining Guesses: "), remains);
        // since remainingGuessBox is HBox, as users click start playing, it adds horizontally.
        initWordGraphics(guessedLetters);
        appTemplate.setAppFileController(this);
        gameWorkspace.updateWorkspaceStartButton(startable);
        play();
    }

    private void end() {
        if (gamedata.getRemainingGuesses() <= 0 ||(discovered == progress.length) ) {
            System.out.println(success ? "You win!" : "Ah, close but not quite there. The word was \"" + gamedata.getTargetWord() + "\".");
        }
        appTemplate.getGUI().getPrimaryScene().setOnKeyTyped(null);
        gameover = true;
        startGameButton.setDisable(true);
        startable = true;
        loadable = true;
        savable = false; // cannot save a game that is already over
        appTemplate.getGUI().updateWorkspaceToolbar(startable, loadable, savable);

    }

    private void initWordGraphics(HBox guessedLetters) {
        char[] targetword = gamedata.getTargetWord().toCharArray(); // ??? why do we need to cast to char[]?
        progress = new Text[targetword.length];
        for (int i = 0; i < progress.length; i++) {
            progress[i] = new Text(Character.toString(targetword[i]));
            progress[i].setVisible(false); // make them invisible first.
        }
        guessedLetters.getChildren().setAll(progress);
    }

    private void reinitWordGraphics(HBox guessedLetters){
        char[] targetword = gamedata.getTargetWord().toCharArray();
        progress = new Text[targetword.length];
        discovered = 0;
        for (int i = 0; i<progress.length; i++){
            progress[i] = new Text(Character.toString(targetword[i]));
            if (gamedata.getGoodGuesses().contains(targetword[i])){
                progress[i].setVisible(true);
                discovered ++;
            }else{
                progress[i].setVisible(false);
            }
        }
        guessedLetters.getChildren().setAll(progress);
    }

    public void play() {
        timer = new AnimationTimer() {
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

                        if (!savable) {
                            startable = false;
                            loadable = false;
                            savable = true;
                            appTemplate.getGUI().updateWorkspaceToolbar(startable, loadable, savable);
                        }
                        success = (discovered == progress.length);
                        remains.setText(Integer.toString(gamedata.getRemainingGuesses()));
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
            startable = true;
            loadable = true;
            savable = false;
            appTemplate.getGUI().updateWorkspaceToolbar(startable,loadable, savable);
            Workspace gameWorkspace = (Workspace) appTemplate.getWorkspaceComponent();
            gameWorkspace.reinitialize();
            enableGameButton();
        }

    }
    
    @Override
    public void handleSaveRequest() throws IOException {
        // check if user has saved before,
        // if not, then prompt user to name the data.
        // otherwise, just call save func.
        // saved is true if saving completes without error.
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
            dialog.setCloseButtonText(InitializationParameters.CLOSE_DIALOG_BUTTON_LABEL.getParameter());
            dialog.show(prop.getPropertyValue(SAVE_ERROR_TITLE), prop.getPropertyValue(SAVE_ERROR_MESSAGE));
        }
        if (saved) {
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
            PropertyManager prop = PropertyManager.getManager();
            dialog.setCloseButtonText(InitializationParameters.CLOSE_DIALOG_BUTTON_LABEL.getParameter());
            dialog.show(prop.getPropertyValue(SAVE_COMPLETED_TITLE), prop.getPropertyValue(SAVE_COMPLETED_MESSAGE));
            savable = false;
            loadable = true;
            startable = true;
            appTemplate.getGUI().updateWorkspaceToolbar(startable, loadable, savable);
        }
    }

    private FileChooser savedDefaultFileChooser(){
        PropertyManager           propertyManager = PropertyManager.getManager();
        FileChooser   fileChooser = new FileChooser();
        URL workDirURL  = AppTemplate.class.getClassLoader().getResource("");
        File dir_f = new File(workDirURL.getPath()+APP_WORKDIR_PATH.getParameter());
        // make a file path to default directory

        // if the default directory does not exist, make the directory
        if(!dir_f.exists() ) {
            try {
                dir_f.mkdir();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        // set /saved directory as a default initial directory
        fileChooser.setInitialDirectory(dir_f);
        fileChooser.setTitle(propertyManager.getPropertyValue(SAVE_WORK_TITLE));

        FileChooser.ExtensionFilter fileExtensions = new FileChooser.ExtensionFilter("JSON file", "*.json");
        fileChooser.getExtensionFilters().add(fileExtensions);

        return fileChooser;

    }

    @Override
    public void handleLoadRequest() throws IOException {
        boolean loaded = false;
        File f_open = null;

        try{
            if (!savable){
                FileChooser   fileChooser = savedDefaultFileChooser();
                gamedata = new GameData(appTemplate);
                f_open = fileChooser.showOpenDialog(appTemplate.getGUI().getWindow());
                if (f_open != null) {
                    if (startGameButton!=null && !gameover) {
                        timer.stop();
                    }
                    loaded = load(f_open.toPath());
                }
            }
        }catch (IOException ioe){
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
            PropertyManager           props  = PropertyManager.getManager();
            dialog.setCloseButtonText(InitializationParameters.CLOSE_DIALOG_BUTTON_LABEL.getParameter());
            dialog.show(props.getPropertyValue(PROPERTIES_LOAD_ERROR_TITLE), props.getPropertyValue(PROPERTIES_LOAD_ERROR_MESSAGE));
        }
        if (loaded){
            startable = true;
            loadable = true;
            savable = false;
            appTemplate.getGUI().updateWorkspaceToolbar(startable, loadable, savable);  // update tool bar

            gameover = false;
            success = false;

            Workspace workspace = (Workspace) appTemplate.getWorkspaceComponent();
            HBox guessedLetters = (HBox) workspace.getGameTextsPane().getChildren().get(1);

            if (startGameButton==null){  // not played before
                startGameButton = workspace.getStartGame();

                HBox remainingGuessBox = workspace.getRemainingGuessBox();
                remains = new Label(Integer.toString(gamedata.getRemainingGuesses()));
                remainingGuessBox.getChildren().setAll(new Label("Remaining Guesses: "), remains);

            }else {  // played before
                remains.setText(Integer.toString(gamedata.getRemainingGuesses()));
            }

            reinitWordGraphics(guessedLetters);
            ensureActivatedWorkspace();
            appTemplate.setAppFileController(this);

            workFile = f_open.toPath();

            workspace.updateWorkspaceStartButton(false);
            play();
        }else{
            if (workFile!=null) {
                appTemplate.getFileComponent().loadData(gamedata, workFile);
            }
        }
    }
    
    @Override
    public void handleExitRequest() {
        try {
            boolean exit = true;
            if (savable){
                if( workFile == null){
                    exit = promptToSave();
                }else {
                    exit = save(workFile);
                }

                if (exit){
                    AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
                    PropertyManager props = PropertyManager.getManager();
                    dialog.show(props.getPropertyValue(SAVE_COMPLETED_TITLE), props.getPropertyValue(SAVE_COMPLETED_MESSAGE));
                }
            }

            if (exit) {
                System.exit(0);
            }
        } catch (IOException ioe) {
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
            PropertyManager           props  = PropertyManager.getManager();
            dialog.setCloseButtonText(InitializationParameters.CLOSE_DIALOG_BUTTON_LABEL.getParameter());
            dialog.show(props.getPropertyValue(SAVE_ERROR_TITLE), props.getPropertyValue(SAVE_ERROR_MESSAGE));
        }
    }

    private void ensureActivatedWorkspace() {
        appTemplate.getWorkspaceComponent().activateWorkspace(appTemplate.getGUI().getAppPane());
    }
    
    private boolean promptToSave() throws IOException {
        // this is called only when user has not saved the gameData yet.

        FileChooser fileChooser = savedDefaultFileChooser();
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
        // save gameData(targetWord, goodGuess, badGuess, remainingGuesses)
        // return true only when it successfully saved
        // return false otherwise.

        try{
            appTemplate.getFileComponent().saveData(gamedata, target);
        }catch (IOException ioe){
            return false;
        }
        return true;
    }


    private boolean load(Path target) throws IOException{
        try{
            if (gamedata == null){
                gamedata = new GameData(appTemplate);
            }
            appTemplate.getFileComponent().loadData(gamedata,target);
        }catch (IOException ioe){
            return false;
        }
        return true;
    }
}
