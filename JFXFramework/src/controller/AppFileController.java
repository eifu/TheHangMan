package controller;

import apptemplate.AppTemplate;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import propertymanager.PropertyManager;
import ui.AppMessageDialogSingleton;
import ui.YesNoCancelDialogSingleton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import static settings.AppPropertyType.*;
import static settings.InitializationParameters.APP_WORKDIR_PATH;

/**
 * This class provides the event programmed responses for the file controls
 * that are provided by this framework.
 *
 * @author Richard McKenna, Ritwik Banerjee
 */
public class AppFileController implements FileController {

    public AppTemplate           appTemplate;     // reference to the application
    public SimpleBooleanProperty saved;           // whether or not changes have been saved
    public File                  currentWorkFile; // the file on which currently work is being done

    /**
     * Constructor to just store the reference to the application.
     *
     * @param appTemplate The application within which this controller will provide file toolbar responses.
     */
    public AppFileController(AppTemplate appTemplate) {
        this.saved = new SimpleBooleanProperty(true);
        this.appTemplate = appTemplate;
    }

    /**
     * Starts the process of editing new Work. If work is already being edited, it will prompt the user to save it
     * first.
     */
    public void handleNewRequest() {
        AppMessageDialogSingleton messageDialog   = AppMessageDialogSingleton.getSingleton();
        PropertyManager           propertyManager = PropertyManager.getManager();
        try {
            boolean continueToMakeNew = true;
            if (!saved.getValue())
                continueToMakeNew = promptToSave();

            // IF THE USER REALLY WANTS TO MAKE A NEW COURSE
            if (continueToMakeNew) {
                appTemplate.getDataComponent().reset();                // reset the data (should be reflected in GUI)
                appTemplate.getWorkspaceComponent().reloadWorkspace(); // load data into workspace
                ensureActivatedWorkspace();                            // ensure workspace is activated
                saved.set(false);                                      // new workspace is unsaved
                currentWorkFile = null;                                // new workspace has never been saved to a file
                //                messageDialog.show(propertyManager.getPropertyValue(NEW_COMPLETED_TITLE),
                //                                   propertyManager.getPropertyValue(NEW_COMPLETED_MESSAGE));
            }
        } catch (IOException ioe) {
            // SOMETHING WENT WRONG, PROVIDE FEEDBACK
            messageDialog.show(propertyManager.getPropertyValue(NEW_ERROR_TITLE), propertyManager.getPropertyValue(NEW_ERROR_MESSAGE));
        }
    }

    private void ensureActivatedWorkspace() {
        appTemplate.getWorkspaceComponent().activateWorkspace(appTemplate.getGUI().getAppPane());
    }

    /**
     * This method will save the current course to a file. Note that we already
     * know the name of the file, so we won't need to prompt the user.
     */
    public void handleSaveRequest() {
        PropertyManager propertyManager = PropertyManager.getManager();
        try {
            if (currentWorkFile != null)
                saveWork(currentWorkFile);
            else {
                FileChooser fileChooser = new FileChooser();
                URL         workDirURL  = AppTemplate.class.getClassLoader().getResource(APP_WORKDIR_PATH.getParameter());
                if (workDirURL == null)
                    throw new FileNotFoundException("Work folder not found under resources.");

                File initialDir = new File(workDirURL.getFile());
                fileChooser.setInitialDirectory(initialDir);
                fileChooser.setTitle(propertyManager.getPropertyValue(SAVE_WORK_TITLE));
                fileChooser.getExtensionFilters().addAll(new ExtensionFilter(propertyManager.getPropertyValue(WORK_FILE_EXT_DESC),
                                                                             propertyManager.getPropertyValue(WORK_FILE_EXT)));
                File selectedFile = fileChooser.showSaveDialog(appTemplate.getGUI().getWindow());
                if (selectedFile != null)
                    saveWork(selectedFile);
            }
        } catch (IOException ioe) {
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
            dialog.show(propertyManager.getPropertyValue(SAVE_ERROR_TITLE), propertyManager.getPropertyValue(SAVE_ERROR_MESSAGE));
        }
    }

    @Override
    public void handleLoadRequest() {

    }
    
    /**
     * A helper method to save work. It saves the work, marks the current work file as saved, notifies the user, and
     * updates the appropriate controls in the user interface
     *
     * @param selectedFile The file to which the work will be saved.
     * @throws IOException
     */
    private void saveWork(File selectedFile) throws IOException {
        appTemplate.getFileComponent()
                   .saveData(appTemplate.getDataComponent(), Paths.get(selectedFile.getAbsolutePath()));

        currentWorkFile = selectedFile;
        saved.set(true);

        AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
        PropertyManager           props  = PropertyManager.getManager();
        dialog.show(props.getPropertyValue(SAVE_COMPLETED_TITLE), props.getPropertyValue(SAVE_COMPLETED_MESSAGE));
    }
    
    /** This method will exit the application. If work is unsaved, it will first prompt the user. */
    public void handleExitRequest() {
        try {
            boolean continueToExit = true;
            if (!saved.getValue())
                continueToExit = promptToSave();
            if (continueToExit)
                System.exit(0);
        } catch (IOException ioe) {
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
            PropertyManager           props  = PropertyManager.getManager();
            dialog.show(props.getPropertyValue(SAVE_ERROR_TITLE), props.getPropertyValue(SAVE_ERROR_MESSAGE));
        }
    }

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. Note that it could be used in multiple, like creating new work, or opening another one. The user will be
     * presented with three options:
     * <ol>
     * <li>{@code yes}, to indicate that the user wants to save their work and continue with the action,</li>
     * <li>{@code no}, to indicate that the user wants to continue with the action without saving their work, and</li>
     * <li>{@code cancel}, to indicate that the user does not want to continue with the action, but also does not want
     * to save their work at this point.</li>
     * </ol>
     *
     * @return {@code false} if the user presses the <i>cancel</i>, and {@code true} otherwise.
     */
    private boolean promptToSave() throws IOException {
        PropertyManager            propertyManager   = PropertyManager.getManager();
        YesNoCancelDialogSingleton yesNoCancelDialog = YesNoCancelDialogSingleton.getSingleton();

        yesNoCancelDialog.show(propertyManager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE),
                               propertyManager.getPropertyValue(SAVE_UNSAVED_WORK_MESSAGE));

        if (yesNoCancelDialog.getSelection().equals(YesNoCancelDialogSingleton.YES)) {
            if (currentWorkFile != null)
                saveWork(currentWorkFile);
            else {
                FileChooser filechooser = new FileChooser();
                URL         workDirURL  = AppTemplate.class.getClassLoader().getResource(APP_WORKDIR_PATH.getParameter());
                if (workDirURL == null)
                    throw new FileNotFoundException("Work folder not found under resources.");

                File initialDir = new File(workDirURL.getFile());
                filechooser.setInitialDirectory(initialDir);
                filechooser.setTitle(propertyManager.getPropertyValue(SAVE_WORK_TITLE));

                String description = propertyManager.getPropertyValue(WORK_FILE_EXT_DESC);
                String extension   = propertyManager.getPropertyValue(WORK_FILE_EXT);
                ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (*.%s)", description, extension),
                                                                String.format("*.%s", extension));
                filechooser.getExtensionFilters().add(extFilter);
                File selectedFile = filechooser.showSaveDialog(appTemplate.getGUI().getWindow());
                if (selectedFile != null)
                    saveWork(selectedFile);
            }
        }

        return !yesNoCancelDialog.getSelection().equals(YesNoCancelDialogSingleton.CANCEL);
    }

}
