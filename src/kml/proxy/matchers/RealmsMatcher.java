package kml.proxy.matchers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class RealmsMatcher implements URLMatcher {
    private final String REALMS_COMPATIBLE = "https://pc.realms.minecraft.net/mco/client/compatible";
    private final String REALMS_INVITES = "https://pc.realms.minecraft.net/invites/count/pending";
    private final String REALMS_TRIAL = "https://pc.realms.minecraft.net/trial";
    private final String REALMS_NEWS = "https://pc.realms.minecraft.net/mco/v1/news";
    private final String REALMS_AVAILABLE = "https://pc.realms.minecraft.net/mco/available";
    private final String REALMS_LIVEPLAYERLIST = "https://pc.realms.minecraft.net/activities/liveplayerlist";
    private final String REALMS_WORLDS = "https://pc.realms.minecraft.net/worlds";
    private final Pattern REALMS_WORLD_JOIN = Pattern.compile("https://pc.realms.minecraft.net/worlds/v1/(\\d+)/join/pc");
    private final Pattern REALMS_EDIT_JOIN = Pattern.compile("https://pc.realms.minecraft.net/worlds/(\\d+)");
    private final Pattern REALMS_UNINVITE = Pattern.compile("https://pc.realms.minecraft.net/invites/(\\d+)");
    private final String REALMS_PENDING = "https://pc.realms.minecraft.net/invites/pending";
    private final String REALMS_REGIONS = "https://pc.realms.minecraft.net/regions/ping/stat";

    @Override
    public final boolean match(String url) {
        return url.equalsIgnoreCase(REALMS_COMPATIBLE) || url.equalsIgnoreCase(REALMS_INVITES) ||
                url.equalsIgnoreCase(REALMS_TRIAL) || url.equalsIgnoreCase(REALMS_NEWS) ||
                url.equalsIgnoreCase(REALMS_AVAILABLE) || url.equalsIgnoreCase(REALMS_LIVEPLAYERLIST) ||
                url.equalsIgnoreCase(REALMS_WORLDS) || REALMS_WORLD_JOIN.matcher(url).matches() ||
                REALMS_UNINVITE.matcher(url).matches() || url.equalsIgnoreCase(REALMS_PENDING) ||
                REALMS_EDIT_JOIN.matcher(url).matches() || url.equalsIgnoreCase(REALMS_REGIONS);
    }

    @Override
    public final String handle(String url) {
        String remoteURL = null;
        if (url.equalsIgnoreCase(REALMS_COMPATIBLE)) {
            remoteURL = "https://mc.krothium.com/realms/compatible";
        } else if (url.equalsIgnoreCase(REALMS_AVAILABLE)) {
            remoteURL = "https://mc.krothium.com/realms/available";
        } else if (url.equalsIgnoreCase(REALMS_TRIAL)) {
            remoteURL = "https://mc.krothium.com/realms/trial";
        } else if (url.equalsIgnoreCase(REALMS_WORLDS)) {
            remoteURL = "https://mc.krothium.com/realms/worlds";
        } else if (url.equalsIgnoreCase(REALMS_INVITES)) {
            remoteURL = "https://mc.krothium.com/realms/pending";
        } else if (url.equalsIgnoreCase(REALMS_NEWS)) {
            remoteURL = "https://mc.krothium.com/realms/news";
        } else if (url.equalsIgnoreCase(REALMS_LIVEPLAYERLIST)) {
            remoteURL = "https://mc.krothium.com/realms/liveplayerlist";
        } else if (REALMS_WORLD_JOIN.matcher(url).matches()) {
            Matcher m = REALMS_WORLD_JOIN.matcher(url);
            if (m.matches()) {
                String worldID = m.group(1);
                String REALMS_JOIN_WORLD_URL = "https://mc.krothium.com/realms/join";
                remoteURL = REALMS_JOIN_WORLD_URL + '/' + worldID;
            }
        } else if (REALMS_UNINVITE.matcher(url).matches()) {
            remoteURL = "https://mc.krothium.com/realms/leave";
        } else if (url.equalsIgnoreCase(REALMS_PENDING)) {
            remoteURL = "https://mc.krothium.com/realms/invites";
        } else if (REALMS_EDIT_JOIN.matcher(url).matches()) {
            remoteURL = "https://mc.krothium.com/realms/editworld";
        } else if (url.equalsIgnoreCase(REALMS_REGIONS)) {
            remoteURL = "https://mc.krothium.com/realms/regions";
        }
        return remoteURL;
    }
}