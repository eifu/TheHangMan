package gui;

import apptemplate.AppTemplate;
import components.AppWorkspaceComponent;
import controller.HangmanController;
import data.GameData;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import propertymanager.PropertyManager;
import ui.AppGUI;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static hangman.HangmanProperties.*;

/**
 * This class serves as the GUI component for the Hangman game.
 *
 * @author Ritwik Banerjee
 * @author Eifu Tomita
 */
public class Workspace extends AppWorkspaceComponent {

    AppTemplate app; // the actual application
    AppGUI      gui; // the GUI inside which the application sits

    Label             guiHeadingLabel;   // workspace (GUI) heading label
    HBox              headPane;          // conatainer to display the heading
    HBox              bodyPane;          // container for the main game displays
    ToolBar           footToolbar;       // toolbar for game buttons
    Pane        figurePane;        // container to display the namesake graphic of the (potentially) hanging person
    VBox              gameTextsPane;     // container to display the text-related parts of the game
    HBox              guessedLetters;    // text area displaying all the letters guessed so far
    HBox              remainingGuessBox; // container to display the number of remaining guesses
    Button            startGame;         // the button to start playing a game of Hangman
    Button            hintGame;
    FlowPane          guessedKeys;
    HangmanController controller;

    /**
     * Constructor for initializing the workspace, note that this constructor
     * will fully setup the workspace user interface for use.
     *
     * @param initApp The application this workspace is part of.
     * @throws IOException Thrown should there be an error loading application
     *                     data for setting up the user interface.
     */
    public Workspace(AppTemplate initApp) throws IOException {
        app = initApp;
        gui = app.getGUI();
        controller = (HangmanController) gui.getFileController();    //new HangmanController(app, startGame); <-- THIS WAS A MAJOR BUG!??
        layoutGUI();     // initialize all the workspace (GUI) components including the containers and their layout
        setupHandlers(); // ... and set up event handling
    }

    private void layoutGUI() {
        PropertyManager propertyManager = PropertyManager.getManager();
        guiHeadingLabel = new Label(propertyManager.getPropertyValue(WORKSPACE_HEADING_LABEL));

        headPane = new HBox();
        headPane.getChildren().add(guiHeadingLabel);
        headPane.setAlignment(Pos.CENTER);

        figurePane = new Pane();

        gameTextsPane = new VBox();
        remainingGuessBox = new HBox();
        guessedLetters = new HBox();
        guessedLetters.setStyle("-fx-background-color: transparent;");

        guessedKeys = new FlowPane();


        hintGame = new Button("Hint");
        hintGame.setVisible(false);

        gameTextsPane.getChildren().setAll(remainingGuessBox, guessedLetters,guessedKeys, hintGame);

        bodyPane = new HBox();
        bodyPane.getChildren().addAll(figurePane, gameTextsPane);

        startGame = new Button("Start Playing");
        HBox blankBoxLeft  = new HBox();
        HBox blankBoxRight = new HBox();
        HBox.setHgrow(blankBoxLeft, Priority.ALWAYS);
        HBox.setHgrow(blankBoxRight, Priority.ALWAYS);
        footToolbar = new ToolBar(blankBoxLeft, startGame, blankBoxRight);

        workspace = new VBox();
        workspace.getChildren().addAll(headPane, bodyPane, footToolbar);
    }

    private void setupHandlers() {
        startGame.setOnMouseClicked(e -> controller.start());
    }

    /**
     * This function specifies the CSS for all the UI components known at the time the workspace is initially
     * constructed. Components added and/or removed dynamically as the application runs need to be set up separately.
     */
    @Override
    public void initStyle() {
        PropertyManager propertyManager = PropertyManager.getManager();

        gui.getAppPane().setId(propertyManager.getPropertyValue(ROOT_BORDERPANE_ID));
        gui.getToolbarPane().getStyleClass().setAll(propertyManager.getPropertyValue(SEGMENTED_BUTTON_BAR));
        gui.getToolbarPane().setId(propertyManager.getPropertyValue(TOP_TOOLBAR_ID));

        ObservableList<Node> toolbarChildren = gui.getToolbarPane().getChildren();
        toolbarChildren.get(0).getStyleClass().add(propertyManager.getPropertyValue(FIRST_TOOLBAR_BUTTON));
        toolbarChildren.get(toolbarChildren.size() - 1).getStyleClass().add(propertyManager.getPropertyValue(LAST_TOOLBAR_BUTTON));

        workspace.getStyleClass().add(CLASS_BORDERED_PANE);
        guiHeadingLabel.getStyleClass().setAll(propertyManager.getPropertyValue(HEADING_LABEL));

    }

    /** This function reloads the entire workspace */
    @Override
    public void reloadWorkspace() {
        /* does nothing; use reinitialize() instead */
    }

    public VBox getGameTextsPane() {
        return gameTextsPane;
    }

    public HBox getRemainingGuessBox() {
        return remainingGuessBox;
    }

    public Button getStartGame() {
        return startGame;
    }

    public Button getHintGame(){ return hintGame;}

    public Pane getFigurePane(){return figurePane;}

    public FlowPane getGuessedKeys(){return guessedKeys;}

    public HBox getBodyPane(){return bodyPane;}

    public void reinitialize() {

        remainingGuessBox = new HBox();
        guessedLetters = new HBox();
        guessedLetters.setStyle("-fx-background-color: transparent;");
        gameTextsPane = new VBox();

        guessedKeys = new FlowPane();

        hintGame = new Button("Hint");
        //  TODO check if this works.
        //        hintGame.setDisable(!((GameData)app.getDataComponent()).getHintReserved());
        hintGame.setVisible(((GameData)app.getDataComponent()).getDifficulty());
        gameTextsPane.getChildren().setAll(remainingGuessBox, guessedLetters, guessedKeys, hintGame);

        figurePane = new Pane();


        bodyPane.getChildren().setAll(figurePane, gameTextsPane);
    }
}
