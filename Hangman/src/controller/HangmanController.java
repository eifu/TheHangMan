package controller;

import apptemplate.AppTemplate;
import com.sun.javafx.fxml.PropertyNotFoundException;
import com.sun.xml.internal.ws.api.pipe.FiberContextSwitchInterceptor;
import data.GameData;
import gui.Workspace;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import propertymanager.PropertyManager;
import ui.AppMessageDialogSingleton;
import ui.YesNoCancelDialogSingleton;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static settings.AppPropertyType.*;
import static settings.InitializationParameters.APP_WORKDIR_PATH;

/**
 * @author Ritwik Banerjee
 */
public class HangmanController implements FileController {

    public enum GameState {
        UNINITIALIZED,
        INITIALIZED_UNMODIFIED,
        INITIALIZED_MODIFIED,
        ENDED
    }

    private AppTemplate appTemplate; // shared reference to the application
    private GameData    gamedata;    // shared reference to the game being played, loaded or saved
    private GameState   gamestate;   // the state of the game being shown in the workspace
    private StackPane[]      progress;    // reference to the text area for the word
    private boolean     success;     // whether or not player was successful
    private int         discovered;  // the number of letters already discovered
    private Button      gameButton;  // shared reference to the "start game" button
    private Button      hintButton;  //
    private FlowPane    guessedKeys; //
    private Label       remains;     // dynamically updated label that indicates the number of remaining guesses
    private Path        workFile;

    public HangmanController(AppTemplate appTemplate, Button gameButton) {
        this(appTemplate);
        this.gameButton = gameButton;
    }

    public HangmanController(AppTemplate appTemplate) {
        this.appTemplate = appTemplate;
        this.gamestate = GameState.UNINITIALIZED;
    }

    public void enableGameButton() {
        if (gameButton == null) {
            Workspace workspace = (Workspace) appTemplate.getWorkspaceComponent();
            gameButton = workspace.getStartGame();
        }
        gameButton.setDisable(false);
    }

    public void disableGameButton() {
        if (gameButton == null) {
            Workspace workspace = (Workspace) appTemplate.getWorkspaceComponent();
            gameButton = workspace.getStartGame();
        }
        gameButton.setDisable(true);
    }

    public void setVisbleHintButton(boolean isVisible){
        if (hintButton == null){
            Workspace workspace = (Workspace) appTemplate.getWorkspaceComponent();
            hintButton = workspace.getHintGame();
        }
        hintButton.setVisible(isVisible);
    }

    public void setGameState(GameState gamestate) {
        this.gamestate = gamestate;
    }

    public GameState getGamestate() {
        return this.gamestate;
    }

    /**
     * In the homework code given to you, we had the line
     * gamedata = new GameData(appTemplate, true);
     * This meant that the 'gamedata' variable had access to the app, but the data component of the app was still
     * the empty game data! What we need is to change this so that our 'gamedata' refers to the data component of
     * the app, instead of being a new object of type GameData. There are several ways of doing this. One of which
     * is to write (and use) the GameData#init() method.
     */
    public void start() {
        gamedata = (GameData) appTemplate.getDataComponent();
        success = false;
        discovered = 0;

        Workspace gameWorkspace = (Workspace) appTemplate.getWorkspaceComponent();

        gamedata.init();
        setGameState(GameState.INITIALIZED_UNMODIFIED);
        HBox remainingGuessBox = gameWorkspace.getRemainingGuessBox();
        HBox guessedLetters    = (HBox) gameWorkspace.getGameTextsPane().getChildren().get(1);
        remains = new Label(Integer.toString(GameData.TOTAL_NUMBER_OF_GUESSES_ALLOWED));
        remainingGuessBox.getChildren().addAll(new Label("Remaining Guesses: "), remains);
        initWordGraphics(guessedLetters);
        hintButton = gameWorkspace.getHintGame();
        hintButton.setVisible(gamedata.getDifficulty());

        guessedKeys = gameWorkspace.getGuessedKeys();
        for (int i = 'a'; i <= 'z'; i++){
            StackPane s = new StackPane(new Rectangle(45,45),new Rectangle(40,40), new Text(Character.toString((char)i)));
            ((Rectangle)s.getChildren().get(0)).setFill(Color.TRANSPARENT);
            ((Rectangle)s.getChildren().get(1)).setFill(Color.LIGHTCYAN);
            guessedKeys.getChildren().add(s);
        }
        play();
    }

    private void end() {
        appTemplate.getGUI().getPrimaryScene().setOnKeyTyped(null);
        gameButton.setDisable(true);
        setGameState(GameState.ENDED);
        appTemplate.getGUI().updateWorkspaceToolbar(gamestate.equals(GameState.INITIALIZED_MODIFIED));
        Platform.runLater(() -> {
            PropertyManager           manager    = PropertyManager.getManager();
            AppMessageDialogSingleton dialog     = AppMessageDialogSingleton.getSingleton();
            String                    endMessage = manager.getPropertyValue(success ? GAME_WON_MESSAGE : GAME_LOST_MESSAGE);

            if (dialog.isShowing())
                dialog.toFront();
            else
                dialog.show(manager.getPropertyValue(GAME_OVER_TITLE), endMessage);
            for (int i = 0; i < progress.length; i++) {
                progress[i].getChildren().get(2).setVisible(true);
                if (!gamedata.getGoodGuesses().contains(((Text)progress[i].getChildren().get(2)).getText().charAt(0))) {
                    ((Rectangle)progress[i].getChildren().get(1)).setFill(Color.LIGHTBLUE);

                }
            }

        });
    }

    private void initWordGraphics(HBox guessedLetters) {
        char[] targetword = gamedata.getTargetWord().toCharArray();
        progress = new StackPane[targetword.length];
        for (int i = 0; i < progress.length; i++) {
            Text t = new Text(Character.toString(targetword[i]));
            t.setVisible(false);
            Rectangle rect_out = new Rectangle(25, 25);
            rect_out.setFill(Color.TRANSPARENT);
            Rectangle rect_in = new Rectangle(20, 20);
            rect_in.setFill(Color.WHITE);
            progress[i] = new StackPane(rect_out,rect_in,t);

        }
        guessedLetters.getChildren().addAll(progress);
    }

    public void play() {
        disableGameButton();

        Workspace gameWorkspace = (Workspace) appTemplate.getWorkspaceComponent();
        BorderPane figurePane = gameWorkspace.getFigurePane();
        figurePane.setPrefSize(500,300);

        guessedKeys = gameWorkspace.getGuessedKeys();


        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                appTemplate.getGUI().updateWorkspaceToolbar(gamestate.equals(GameState.INITIALIZED_MODIFIED));
                appTemplate.getGUI().getPrimaryScene().setOnKeyTyped((KeyEvent event) -> {
                    char guess = event.getCharacter().charAt(0);
                    if (Character.isLetter(guess) && !alreadyGuessed(guess)) {

                        ((Rectangle)((StackPane)guessedKeys.getChildren().get(guess-'a')).getChildren().get(1)).setFill(Color.AQUA);

                        boolean goodguess = false;
                        for (int i = 0; i < progress.length; i++) {
                            if (gamedata.getTargetWord().charAt(i) == guess) {
                                progress[i].getChildren().get(2).setVisible(true);
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
                    setGameState(GameState.INITIALIZED_MODIFIED);
                });
                if (gamedata.getRemainingGuesses() <= 0 || success)
                    stop();

                if (gamedata.getRemainingGuesses() == 1){
                    hintButton.setDisable(true);
                }

                if (gamedata.getHintReserved()){
                    hintButton.setOnMouseClicked(e -> {

                        // find letter in target, AND not in goodGuess
                        char letter_for_hint = hint_letter_finder();
                        for (int i = 0; i < progress.length; i++) {
                            if (gamedata.getTargetWord().charAt(i) == letter_for_hint) {
                                progress[i].getChildren().get(2).setVisible(true);
                                gamedata.addGoodGuess(letter_for_hint);
                                discovered++;
                            }
                        }
                        ((Rectangle)((StackPane)guessedKeys.getChildren().get(letter_for_hint-'a')).getChildren().get(1)).setFill(Color.AQUA);
                        gamedata.setHintReserved(false);
                        gamedata.setRemainingGuesses(gamedata.getRemainingGuesses()-1);

                        hintButton.setDisable(true);
                    });
                }
            }

            @Override
            public void stop() {
                super.stop();
                end();
            }
        };
        timer.start();
    }

    private char hint_letter_finder(){
        for (int letter = 'a'; letter <= 'z'; letter ++){
            if (!gamedata.getGoodGuesses().contains(letter)){
                for (int i = 0; i < gamedata.getTargetWord().length(); i ++) {
                    if (gamedata.getTargetWord().charAt(i) == letter) {
                        return (char)letter;
                    }
                }
            }
        }
        throw new PropertyNotFoundException();
    }

    private void restoreGUI() {
        disableGameButton();
        Workspace gameWorkspace = (Workspace) appTemplate.getWorkspaceComponent();
        gameWorkspace.reinitialize();

        HBox guessedLetters = (HBox) gameWorkspace.getGameTextsPane().getChildren().get(1);
        restoreWordGraphics(guessedLetters);

        HBox remainingGuessBox = gameWorkspace.getRemainingGuessBox();
        remains = new Label(Integer.toString(gamedata.getRemainingGuesses()));
        remainingGuessBox.getChildren().addAll(new Label("Remaining Guesses: "), remains);

        guessedKeys = gameWorkspace.getGuessedKeys();
        for (int i = 'a'; i <= 'z'; i++){
            StackPane s = new StackPane(new Rectangle(45,45),new Rectangle(40,40), new Text(Character.toString((char)i)));
            ((Rectangle)s.getChildren().get(0)).setFill(Color.TRANSPARENT);
            if (alreadyGuessed((char)i)){
                ((Rectangle) s.getChildren().get(1)).setFill(Color.AQUA);
            }else {
                ((Rectangle) s.getChildren().get(1)).setFill(Color.LIGHTCYAN);
            }
            guessedKeys.getChildren().add(s);
        }
        hintButton = (Button)gameWorkspace.getGameTextsPane().getChildren().get(3);
        hintButton.setVisible(gamedata.getDifficulty());
        hintButton.setDisable(!gamedata.getHintReserved());
        success = false;
        play();
    }

    private void restoreWordGraphics(HBox guessedLetters) {
        discovered = 0;
        char[] targetword = gamedata.getTargetWord().toCharArray();
        progress = new StackPane[targetword.length];
        for (int i = 0; i < progress.length; i++) {
            Text t = new Text(Character.toString(targetword[i]));
            t.setVisible(gamedata.getGoodGuesses().contains(t.getText().charAt(0)));
            if (t.isVisible())
                discovered++;
            Rectangle rect_out = new Rectangle(25, 25);
            rect_out.setFill(Color.TRANSPARENT);

            Rectangle rect_in = new Rectangle(20, 20);
            rect_in.setFill(Color.WHITE);
            progress[i] = new StackPane(rect_out, rect_in, t);
        }
        guessedLetters.getChildren().addAll(progress);
    }

    private boolean alreadyGuessed(char c) {
        return gamedata.getGoodGuesses().contains(c) || gamedata.getBadGuesses().contains(c);
    }

    @Override
    public void handleNewRequest() {
        AppMessageDialogSingleton messageDialog   = AppMessageDialogSingleton.getSingleton();
        PropertyManager           propertyManager = PropertyManager.getManager();
        boolean                   makenew         = true;
        if (gamestate.equals(GameState.INITIALIZED_MODIFIED))
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
            ((Workspace) appTemplate.getWorkspaceComponent()).reinitialize();
            enableGameButton();
        }
        if (gamestate.equals(GameState.ENDED)) {
            appTemplate.getGUI().updateWorkspaceToolbar(false);
            Workspace gameWorkspace = (Workspace) appTemplate.getWorkspaceComponent();
            gameWorkspace.reinitialize();
        }

    }

    @Override
    public void handleSaveRequest() throws IOException {
        PropertyManager propertyManager = PropertyManager.getManager();
        if (workFile == null) {
            FileChooser filechooser = new FileChooser();
            Path        appDirPath  = Paths.get(propertyManager.getPropertyValue(APP_TITLE)).toAbsolutePath();
            Path        targetPath  = appDirPath.resolve(APP_WORKDIR_PATH.getParameter());
            filechooser.setInitialDirectory(targetPath.toFile());
            filechooser.setTitle(propertyManager.getPropertyValue(SAVE_WORK_TITLE));
            String description = propertyManager.getPropertyValue(WORK_FILE_EXT_DESC);
            String extension   = propertyManager.getPropertyValue(WORK_FILE_EXT);
            ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (*.%s)", description, extension),
                                                            String.format("*.%s", extension));
            filechooser.getExtensionFilters().add(extFilter);
            File selectedFile = filechooser.showSaveDialog(appTemplate.getGUI().getWindow());
            if (selectedFile != null)
                save(selectedFile.toPath());
        } else
            save(workFile);
    }

    @Override
    public void handleLoadRequest() throws IOException {
        boolean load = true;
        if (gamestate.equals(GameState.INITIALIZED_MODIFIED))
            load = promptToSave();
        if (load) {
            PropertyManager propertyManager = PropertyManager.getManager();
            FileChooser     filechooser     = new FileChooser();
            Path            appDirPath      = Paths.get(propertyManager.getPropertyValue(APP_TITLE)).toAbsolutePath();
            Path            targetPath      = appDirPath.resolve(APP_WORKDIR_PATH.getParameter());
            filechooser.setInitialDirectory(targetPath.toFile());
            filechooser.setTitle(propertyManager.getPropertyValue(LOAD_WORK_TITLE));
            String description = propertyManager.getPropertyValue(WORK_FILE_EXT_DESC);
            String extension   = propertyManager.getPropertyValue(WORK_FILE_EXT);
            ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (*.%s)", description, extension),
                                                            String.format("*.%s", extension));
            filechooser.getExtensionFilters().add(extFilter);
            File selectedFile = filechooser.showOpenDialog(appTemplate.getGUI().getWindow());
            if (selectedFile != null && selectedFile.exists())
                load(selectedFile.toPath());
            restoreGUI(); // restores the GUI to reflect the state in which the loaded game was last saved
        }
    }

    @Override
    public void handleExitRequest() {
        try {
            boolean exit = true;
            if (gamestate.equals(GameState.INITIALIZED_MODIFIED))
                exit = promptToSave();
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
        PropertyManager            propertyManager   = PropertyManager.getManager();
        YesNoCancelDialogSingleton yesNoCancelDialog = YesNoCancelDialogSingleton.getSingleton();

        yesNoCancelDialog.show(propertyManager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE),
                               propertyManager.getPropertyValue(SAVE_UNSAVED_WORK_MESSAGE));

        if (yesNoCancelDialog.getSelection().equals(YesNoCancelDialogSingleton.YES))
            handleSaveRequest();

        return !yesNoCancelDialog.getSelection().equals(YesNoCancelDialogSingleton.CANCEL);
    }

    /**
     * A helper method to save work. It saves the work, marks the current work file as saved, notifies the user, and
     * updates the appropriate controls in the user interface
     *
     * @param target The file to which the work will be saved.
     * @throws IOException
     */
    private void save(Path target) throws IOException {
        appTemplate.getFileComponent().saveData(appTemplate.getDataComponent(), target);
        workFile = target;
        setGameState(GameState.INITIALIZED_UNMODIFIED);
        AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
        PropertyManager           props  = PropertyManager.getManager();
        dialog.show(props.getPropertyValue(SAVE_COMPLETED_TITLE), props.getPropertyValue(SAVE_COMPLETED_MESSAGE));
    }

    /**
     * A helper method to load saved game data. It loads the game data, notified the user, and then updates the GUI to
     * reflect the correct state of the game.
     *
     * @param source The source data file from which the game is loaded.
     * @throws IOException
     */
    private void load(Path source) throws IOException {
        // load game data
        appTemplate.getFileComponent().loadData(appTemplate.getDataComponent(), source);

        // set the work file as the file from which the game was loaded
        workFile = source;

        // notify the user that load was successful
        AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
        PropertyManager           props  = PropertyManager.getManager();
        dialog.show(props.getPropertyValue(LOAD_COMPLETED_TITLE), props.getPropertyValue(LOAD_COMPLETED_MESSAGE));

        setGameState(GameState.INITIALIZED_UNMODIFIED);
        Workspace gameworkspace = (Workspace) appTemplate.getWorkspaceComponent();
        ensureActivatedWorkspace();
        gameworkspace.reinitialize();
        gamedata = (GameData) appTemplate.getDataComponent();
    }
}
