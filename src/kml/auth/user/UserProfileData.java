package kml.auth.user;

import com.google.gson.annotations.Expose;

public class UserProfileData {
    @Expose
    private String agent;
    @Expose
    private long createdAt;
    @Expose
    private String id;
    @Expose
    private boolean legacyProfile;
    @Expose
    private boolean migrated;
    @Expose
    private String name;
    @Expose
    private boolean paid;
    @Expose
    private Boolean suspended;
    @Expose
    private String tokenId;
    @Expose
    private String userId;

    public String getAgent() {
        return agent;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getId() {
        return id;
    }

    public boolean isLegacyProfile() {
        return legacyProfile;
    }

    public boolean isMigrated() {
        return migrated;
    }

    public String getName() {
        return name;
    }

    public boolean isPaid() {
        return paid;
    }

    public Boolean getSuspended() {
        return suspended;
    }

    public String getTokenId() {
        return tokenId;
    }

    public String getUserId() {
        return userId;
    }
}
