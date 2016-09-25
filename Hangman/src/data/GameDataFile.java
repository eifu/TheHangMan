package data;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import components.AppDataComponent;
import components.AppFileComponent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Ritwik Banerjee
 */
public class GameDataFile implements AppFileComponent {

    public static final String TARGET_WORD  = "TARGET_WORD";
    public static final String GOOD_GUESSES = "GOOD_GUESSES";
    public static final String BAD_GUESSES  = "BAD_GUESSES";

    @Override
    public void saveData(AppDataComponent data, Path to) throws IOException{
        // todo save data to the path. called from HangmanController.
        ObjectMapper om = new ObjectMapper();

        GameData data_holder = new GameData(((GameData) data).getTargetWord(), ((GameData) data).getGoodGuesses(), ((GameData) data).getBadGuesses(), ((GameData) data).getRemainingGuesses());

        try {
            //Convert object to JSON string and save into file directly
            om.writeValue(new File(to.toString()), data_holder);

            //Convert object to JSON string
            String jsonInString = om.writeValueAsString(data_holder);
            System.out.println(jsonInString);

            //Convert object to JSON string and pretty print
            jsonInString = om.writerWithDefaultPrettyPrinter().writeValueAsString(data_holder);
            System.out.println(jsonInString);


        }  catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


//        om.writeValue(new File(to.toString()), data);


    }

    @Override
    public void loadData(AppDataComponent data, Path from) throws IOException {

    }

    /** This method will be used if we need to export data into other formats. */
    @Override
    public void exportData(AppDataComponent data, Path filePath) throws IOException { }
}
