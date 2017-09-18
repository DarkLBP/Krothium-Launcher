package kml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class Language {
    private static ArrayList<String> langData =  new ArrayList<>();

    /**
     * Loads a language file
     * @param lang The language to be loaded
     * @throws IOException When data read fails
     */
    public static void loadLang(String lang) throws IOException {
        URL resource = Language.class.getResource("/kml/lang/" + lang + ".txt");
        if (resource == null) {
            resource = Language.class.getResource("/kml/lang/en-us.txt");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream(), Charset.forName("UTF-8")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                langData.add(line);
            }
        }
    }

    /**
     * Gets a specific localized line
     * @param line The target line
     * @return The requested localized line
     */
    public static String get(int line) {
        if (line <= langData.size()) {
            return langData.get(line - 1);
        }
        return "";
    }
}
