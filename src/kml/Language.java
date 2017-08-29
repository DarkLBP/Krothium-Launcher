package kml;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class Language {
    private static String[] langData;

    /**
     * Loads a language file
     * @param lang The language to be loaded
     */
    public static void loadLang(String lang) {
        String data = Utils.readURL(Language.class.getResource("/kml/lang/" + lang + ".txt"));
        if (data != null) {
            langData = data.split(System.lineSeparator());
        } else {
            data = Utils.readURL(Language.class.getResource("/kml/lang/en-us.txt"));
            if (data != null) {
                langData = data.split(System.lineSeparator());
            }
        }
    }

    /**
     * Gets a specific localized line
     * @param line The target line
     * @return The requested localized line
     */
    public static String get(int line) {
        if (langData == null || langData.length == 0) {
            return "";
        }
        if (line <= langData.length) {
            return langData[line - 1];
        }
        return "";
    }
}
