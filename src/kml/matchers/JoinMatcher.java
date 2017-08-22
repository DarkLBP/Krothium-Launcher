package kml.matchers;

import kml.Constants;
import kml.GameStarter;
import kml.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class JoinMatcher implements URLMatcher {
    private final String joinURL = "https://sessionserver.mojang.com/session/minecraft/join";

    @Override
    public boolean match(URL url) {
        return url.toString().equalsIgnoreCase(joinURL);
    }

    @Override
    public URL handle(URL url) {
        if (url.toString().equalsIgnoreCase(joinURL)) {
            Map<String, String> arguments = new HashMap<>();
            arguments.put("Access-Token", GameStarter.ACCESS_TOKEN);
            arguments.put("Profile-ID", GameStarter.PROFILE_ID);
            JSONArray array = new JSONArray();
            String[] files = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
            for (String file : files) {
                File f = new File(file);
                if (f.exists() && f.isFile()) {
                    JSONObject obj = new JSONObject();
                    String checksum = Utils.calculateChecksum(f, "SHA-1");
                    if (Objects.nonNull(checksum)) {
                        obj.put("name", f.getName());
                        obj.put("hash", checksum);
                        array.put(obj);
                    }
                }
            }
            File gameDir = new File(GameStarter.GAME_DIR, "mods");
            if (gameDir.exists() && gameDir.isDirectory()) {
                File[] mods = gameDir.listFiles();
                if (Objects.nonNull(mods)) {
                    for (File mod : mods) {
                        if (mod.isFile()) {
                            JSONObject obj = new JSONObject();
                            String checksum = Utils.calculateChecksum(mod, "SHA-1");
                            if (Objects.nonNull(checksum)) {
                                obj.put("name", mod.getName());
                                obj.put("hash", checksum);
                                array.put(obj);
                            }
                        }
                    }
                }
            }
            System.out.println(array.toString());
            try {
                String response = Utils.sendPost(Constants.PROTECTION_URL, array.toString().getBytes(), arguments);
                if (response.equalsIgnoreCase("OK")) {
                    System.out.println("Client successfully validated.");
                } else if (response.equalsIgnoreCase("BANNED")) {
                    System.out.println("Cheats detected. Account banned.");
                    JOptionPane.showMessageDialog(null, "Your account has been banned for using online cheats.", "Account banned", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                } else {
                    System.out.println("Failed to validate the client. " + response);
                }
            } catch (IOException e) {
                System.out.println("Failed to validate the client.");
            }
            return Constants.JOINSERVER;
        }
        return null;
    }
}
