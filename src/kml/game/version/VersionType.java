package kml.game.version;

import com.google.gson.annotations.SerializedName;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public enum VersionType {
    @SerializedName("snapshot") SNAPSHOT,
    @SerializedName("release") RELEASE,
    @SerializedName("old_beta") OLD_BETA,
    @SerializedName("old_alpha") OLD_ALPHA
}
