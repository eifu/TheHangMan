package data;

import components.AppDataComponent;
import components.AppFileComponent;

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
    public void saveData(AppDataComponent data, Path to) {
        // todo save data to the path. called from HangmanController.
    }

    @Override
    public void loadData(AppDataComponent data, Path from) throws IOException {

    }

    /** This method will be used if we need to export data into other formats. */
    @Override
    public void exportData(AppDataComponent data, Path filePath) throws IOException { }
}
