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
    private final String REALMS_COMPATIBLE_URL = "https://mc.krothium.com/realms/compatible";
    private final String REALMS_AVAILABLE_URL = "https://mc.krothium.com/realms/available";
    private final String REALMS_TRIAL_URL = "https://mc.krothium.com/realms/trial";
    private final String REALMS_WORLDS_URL = "https://mc.krothium.com/realms/worlds";
    private final String REALMS_INVITES_PENDING_URL = "https://mc.krothium.com/realms/pending";
    private final String REALMS_NEWS_URL = "https://mc.krothium.com/realms/news";
    private final String REALMS_PLAYER_LIST_URL = "https://mc.krothium.com/realms/liveplayerlist";
    private final String REALMS_JOIN_WORLD_URL = "https://mc.krothium.com/realms/join";
    private final String REALMS_UNINVITE_URL = "https://mc.krothium.com/realms/leave";
    private final String REALMS_INVITESLIST_URL = "https://mc.krothium.com/realms/invites";
    private final String REALMS_EDITWORLD_URL = "https://mc.krothium.com/realms/editworld";
    private final String REALMS_REGIONS_URL = "https://mc.krothium.com/realms/regions";

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
            remoteURL = this.REALMS_COMPATIBLE_URL;
        } else if (url.equalsIgnoreCase(REALMS_AVAILABLE)) {
            remoteURL = this.REALMS_AVAILABLE_URL;
        } else if (url.equalsIgnoreCase(REALMS_TRIAL)) {
            remoteURL = this.REALMS_TRIAL_URL;
        } else if (url.equalsIgnoreCase(REALMS_WORLDS)) {
            remoteURL = this.REALMS_WORLDS_URL;
        } else if (url.equalsIgnoreCase(REALMS_INVITES)) {
            remoteURL = this.REALMS_INVITES_PENDING_URL;
        } else if (url.equalsIgnoreCase(REALMS_NEWS)) {
            remoteURL = this.REALMS_NEWS_URL;
        } else if (url.equalsIgnoreCase(REALMS_LIVEPLAYERLIST)) {
            remoteURL = this.REALMS_PLAYER_LIST_URL;
        } else if (REALMS_WORLD_JOIN.matcher(url).matches()) {
            Matcher m = REALMS_WORLD_JOIN.matcher(url);
            if (m.matches()) {
                String worldID = m.group(1);
                remoteURL = this.REALMS_JOIN_WORLD_URL + '/' + worldID;
            }
        } else if (REALMS_UNINVITE.matcher(url).matches()) {
            remoteURL = this.REALMS_UNINVITE_URL;
        } else if (url.equalsIgnoreCase(REALMS_PENDING)) {
            remoteURL = this.REALMS_INVITESLIST_URL;
        } else if (REALMS_EDIT_JOIN.matcher(url).matches()) {
            remoteURL = this.REALMS_EDITWORLD_URL;
        } else if (url.equalsIgnoreCase(REALMS_REGIONS)) {
            remoteURL = this.REALMS_REGIONS_URL;
        }
        return remoteURL;
    }
}