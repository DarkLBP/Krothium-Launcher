package kml.game.version.rule;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FeaturesRule {
    @Expose
    @SerializedName("is_demo_user")
    private Boolean isDemoUser;
    @Expose
    @SerializedName("has_custom_resolution")
    private Boolean hasCustomResolution;

    public Boolean getDemoUser() {
        return isDemoUser;
    }

    public Boolean getHasCustomResolution() {
        return hasCustomResolution;
    }
}
