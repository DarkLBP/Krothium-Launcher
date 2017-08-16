package kml;

import java.util.Objects;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class Language {
    private static String[] langData = new String[0];

    public static void loadLang(String lang) {
        String data = Utils.readURL(Language.class.getResource("/kml/lang/" + lang + ".txt"));
        if (Objects.nonNull(data)) {
            langData = data.split("\r\n");
        } else {
            data = Utils.readURL(Language.class.getResource("/kml/lang/en-us.txt"));
            if (Objects.nonNull(langData)) {
                langData = data.split("\r\n");
            }
        }
    }

    public static String get(int line) {
        if (langData.length == 0) {
            return "";
        } else if (line <= langData.length) {
            return langData[line - 1];
        }
        return "";
    }
}
