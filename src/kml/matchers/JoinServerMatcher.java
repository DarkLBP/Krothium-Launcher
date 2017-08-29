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

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class JoinServerMatcher implements URLMatcher {
    private static final String JOIN_URL = "http://session.minecraft.net/game/joinserver.jsp";

    @Override
    public final boolean match(URL url) {
        return url.toString().contains(JOIN_URL) && url.getQuery() != null;
    }

    @Override
    public final URL handle(URL url) {
        if (url.toString().contains(JOIN_URL) && url.getQuery() != null) {
            Map<String, String> arguments = new HashMap<>();
            arguments.put("Access-Token", GameStarter.ACCESS_TOKEN);
            arguments.put("Profile-ID", GameStarter.PROFILE_ID);
            JSONArray array = new JSONArray();
            String[] files = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
            boolean forge = false;
            for (String file : files) {
                File f = new File(file);
                if (f.exists() && f.isFile()) {
                    if (f.getName().contains("forge")) {
                        forge = true;
                    }
                    JSONObject obj = new JSONObject();
                    String checksum = Utils.calculateChecksum(f, "SHA-1");
                    if (checksum != null) {
                        obj.put("name", f.getName());
                        obj.put("hash", checksum);
                        array.put(obj);
                    }
                }
            }
            if (forge) {
                File gameDir = new File(GameStarter.GAME_DIR, "mods");
                if (gameDir.exists() && gameDir.isDirectory()) {
                    File[] mods = gameDir.listFiles();
                    if (mods != null) {
                        for (File mod : mods) {
                            if (mod.isFile()) {
                                JSONObject obj = new JSONObject();
                                String checksum = Utils.calculateChecksum(mod, "SHA-1");
                                if (checksum != null) {
                                    obj.put("name", mod.getName());
                                    obj.put("hash", checksum);
                                    array.put(obj);
                                }
                            }
                        }
                    }
                }
            }
            try {
                String response = Utils.sendPost(Constants.PROTECTION_URL, array.toString().getBytes(), arguments);
                if ("OK".equalsIgnoreCase(response)) {
                    System.out.println("Client successfully validated.");
                } else if ("BANNED".equalsIgnoreCase(response)) {
                    System.out.println("Cheats detected. Account banned.");
                    JOptionPane.showMessageDialog(null, "Your account has been banned for using online cheats.", "Account banned", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                } else {
                    System.out.println("Failed to validate the client. " + response);
                }
            } catch (IOException e) {
                System.out.println("Failed to validate the client.");
            }
            return Utils.stringToURL("http://mc.krothium.com/server/joinserver?" + url.getQuery());
        }
        return null;
    }
}
