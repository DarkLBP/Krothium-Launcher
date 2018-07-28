package kml.game.profile;

import com.google.gson.annotations.SerializedName;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public enum ProfileType {
    @SerializedName("latest-release") RELEASE,
    @SerializedName("latest-snapshot") SNAPSHOT,
    @SerializedName("custom") CUSTOM
}
