package data;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import components.AppDataComponent;
import components.AppFileComponent;
import propertymanager.PropertyManager;
import settings.InitializationParameters;
import ui.AppMessageDialogSingleton;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static settings.AppPropertyType.PROPERTIES_LOAD_ERROR_MESSAGE;
import static settings.AppPropertyType.PROPERTIES_LOAD_ERROR_TITLE;

/**
 * @author Ritwik Banerjee
 * @author Eifu Tomita
 */
public class GameDataFile implements AppFileComponent {

    public static final String TARGET_WORD  = "TARGET_WORD";
    public static final String GOOD_GUESSES = "GOOD_GUESSES";
    public static final String BAD_GUESSES  = "BAD_GUESSES";

    @Override
    public void saveData(AppDataComponent data, Path to) throws IOException{
        // save data to the path. called from HangmanController.
        ObjectMapper om = new ObjectMapper();

        GameData data_holder = new GameData(((GameData) data).getTargetWord(),
                                            ((GameData) data).getGoodGuesses(),
                                            ((GameData) data).getBadGuesses(),
                                            ((GameData) data).getRemainingGuesses());

        try {
            //Convert object to JSON string and save into file directly
            om.writeValue(new File(to.toString()), data_holder);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException();
        }
    }

    @Override
    public void loadData(AppDataComponent data, Path from) throws IOException {
        ObjectMapper om = new ObjectMapper();
        try {
            GameData data_loaded = om.readValue(new File(from.toString()), GameData.class);
            ((GameData) data).setTargetWord(data_loaded.getTargetWord())
                    .setGoodGuesses(data_loaded.getGoodGuesses())
                    .setBadGuesses(data_loaded.getBadGuesses())
                    .setRemainingGuesses(data_loaded.getRemainingGuesses());
        } catch(IOException e){
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
            PropertyManager           props  = PropertyManager.getManager();
            dialog.setCloseButtonText(InitializationParameters.WRONG_JSON_DIALOG_BUTTON_LABEL.getParameter());
            dialog.show(props.getPropertyValue(PROPERTIES_LOAD_ERROR_TITLE), props.getPropertyValue(PROPERTIES_LOAD_ERROR_MESSAGE));
            throw new IOException();
        }
    }

    /** This method will be used if we need to export data into other formats. */
    @Override
    public void exportData(AppDataComponent data, Path filePath) throws IOException { }
}
