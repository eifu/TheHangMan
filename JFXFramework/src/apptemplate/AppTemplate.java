package apptemplate;

import com.sun.org.apache.xml.internal.security.Init;
import com.sun.tools.internal.xjc.Language;
import components.AppComponentsBuilder;
import components.AppDataComponent;
import components.AppFileComponent;
import components.AppWorkspaceComponent;
import javafx.application.Application;
import javafx.stage.Stage;
import propertymanager.PropertyManager;
import settings.InitializationParameters;
import ui.AppGUI;
import ui.AppMessageDialogSingleton;
import ui.LanguageDialogSingleton;
import ui.YesNoCancelDialogSingleton;
import xmlutils.InvalidXMLFileFormatException;

import java.io.File;
import java.net.URL;

import static settings.AppPropertyType.*;
import static settings.InitializationParameters.*;

/**
 * @author Ritwik Banerjee
 */
public abstract class AppTemplate extends Application {

    private final PropertyManager propertyManager = PropertyManager.getManager();
    private AppDataComponent      dataComponent; // to manage the app's data
    private AppFileComponent      fileComponent; // to manage the app's file I/O
    private AppWorkspaceComponent workspaceComponent; // to manage the app's GUI workspace
    private AppGUI                gui;
    private static InitializationParameters language;

    public String getFileControllerClass() {
        return "AppFileController";
    }

    public abstract AppComponentsBuilder makeAppBuilderHook();

    public AppDataComponent getDataComponent() {
        return dataComponent;
    }

    public AppFileComponent getFileComponent() {
        return fileComponent;
    }

    public AppWorkspaceComponent getWorkspaceComponent() {
        return workspaceComponent;
    }

    public AppGUI getGUI() {
        return gui;
    }

    @Override
    public void start(Stage primaryStage) {
        AppMessageDialogSingleton  messageDialog = AppMessageDialogSingleton.getSingleton();
        YesNoCancelDialogSingleton yesNoDialog   = YesNoCancelDialogSingleton.getSingleton();
        messageDialog.init(primaryStage);
        yesNoDialog.init(primaryStage);

        LanguageDialogSingleton langDialog = LanguageDialogSingleton.getSingleton();
        langDialog.init(primaryStage);
        langDialog.show("Language", "which one to choose?");
        if (langDialog.getSelection()!=null && langDialog.getSelection().equals(LanguageDialogSingleton.English)){
            language = APP_PROPERTIES_XML;
        } else {
            language = JAPANESE_APP_PROPERTIES_XML;
        }
        try {
            if (loadProperties(language) && loadProperties(WORKSPACE_PROPERTIES_XML)) {
                AppComponentsBuilder builder = makeAppBuilderHook();

                fileComponent = builder.buildFileComponent();
                dataComponent = builder.buildDataComponent();
                gui = (propertyManager.hasProperty(APP_WINDOW_WIDTH) && propertyManager.hasProperty(APP_WINDOW_HEIGHT))
                      ? new AppGUI(primaryStage, propertyManager.getPropertyValue(APP_TITLE.toString()), this,
                                   Integer.parseInt(propertyManager.getPropertyValue(APP_WINDOW_WIDTH)),
                                   Integer.parseInt(propertyManager.getPropertyValue(APP_WINDOW_HEIGHT)))
                      : new AppGUI(primaryStage, propertyManager.getPropertyValue(APP_TITLE.toString()), this);
                workspaceComponent = builder.buildWorkspaceComponent();
                initStylesheet();
                gui.initStyle();
                workspaceComponent.initStyle();
            }
        } catch (Exception e) {
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
            dialog.show(propertyManager.getPropertyValue(PROPERTIES_LOAD_ERROR_TITLE.toString()),
                        propertyManager.getPropertyValue(PROPERTIES_LOAD_ERROR_MESSAGE.toString()));
        }
    }

    public boolean loadProperties(InitializationParameters propertyParameter) {
        try {
            propertyManager.loadProperties(AppTemplate.class, propertyParameter.getParameter(), PROPERTIES_SCHEMA_XSD.getParameter());
        } catch (InvalidXMLFileFormatException e) {
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
            dialog.show(propertyManager.getPropertyValue(PROPERTIES_LOAD_ERROR_TITLE.toString()),
                        propertyManager.getPropertyValue(PROPERTIES_LOAD_ERROR_MESSAGE.toString()));
            return false;
        }

        return true;
    }

    public void initStylesheet() {
        URL cssResource = getClass().getClassLoader().getResource(propertyManager.getPropertyValue(APP_PATH_CSS) +
                                                                  File.separator +
                                                                  propertyManager.getPropertyValue(APP_CSS));
        assert cssResource != null;
        gui.getPrimaryScene().getStylesheets().add(cssResource.toExternalForm());
    }
}
