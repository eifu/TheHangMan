package data;

import com.fasterxml.jackson.core.*;
import components.AppDataComponent;
import components.AppFileComponent;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * @author Ritwik Banerjee
 */
public class GameDataFile implements AppFileComponent {

    public static final String TARGET_WORD  = "TARGET_WORD";
    public static final String GOOD_GUESSES = "GOOD_GUESSES";
    public static final String BAD_GUESSES  = "BAD_GUESSES";

    @Override
    public void saveData(AppDataComponent data, Path to) {
        GameData       gamedata    = (GameData) data;
        Set<Character> goodguesses = gamedata.getGoodGuesses();
        Set<Character> badguesses  = gamedata.getBadGuesses();

        JsonFactory jsonFactory = new JsonFactory();

        try (OutputStream out = Files.newOutputStream(to)) {

            JsonGenerator generator = jsonFactory.createGenerator(out, JsonEncoding.UTF8);

            generator.writeStartObject();

            generator.writeStringField(TARGET_WORD, gamedata.getTargetWord());

            generator.writeFieldName(GOOD_GUESSES);
            generator.writeStartArray(goodguesses.size());
            for (Character c : goodguesses)
                generator.writeString(c.toString());
            generator.writeEndArray();

            generator.writeFieldName(BAD_GUESSES);
            generator.writeStartArray(badguesses.size());
            for (Character c : badguesses)
                generator.writeString(c.toString());
            generator.writeEndArray();

            generator.writeEndObject();

            generator.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void loadData(AppDataComponent data, Path from) throws IOException {
        GameData gamedata = (GameData) data;
        gamedata.reset();

        JsonFactory jsonFactory = new JsonFactory();
        JsonParser  jsonParser  = jsonFactory.createParser(Files.newInputStream(from));

        while (!jsonParser.isClosed()) {
            JsonToken token = jsonParser.nextToken();
            if (JsonToken.FIELD_NAME.equals(token)) {
                String fieldname = jsonParser.getCurrentName();
                switch (fieldname) {
                    case TARGET_WORD:
                        jsonParser.nextToken();
                        gamedata.setTargetWord(jsonParser.getValueAsString());
                        break;
                    case GOOD_GUESSES:
                        jsonParser.nextToken();
                        while (jsonParser.nextToken() != JsonToken.END_ARRAY)
                            gamedata.addGoodGuess(jsonParser.getText().charAt(0));
                        break;
                    case BAD_GUESSES:
                        jsonParser.nextToken();
                        while (jsonParser.nextToken() != JsonToken.END_ARRAY)
                            gamedata.addBadGuess(jsonParser.getText().charAt(0));
                        break;
                    default:
                        throw new JsonParseException(jsonParser, "Unable to load JSON data");
                }
            }
        }
    }

    /** This method will be used if we need to export data into other formats. */
    @Override
    public void exportData(AppDataComponent data, Path filePath) throws IOException { }
}
