package kml.auth.user;

import com.google.gson.annotations.Expose;

public class UserData {
    @Expose
    private String id;
    @Expose
    private String email;
    @Expose
    private String username;
    @Expose
    private String registerIp;
    @Expose
    private String migratedFrom;
    @Expose
    private long migratedAt;
    @Expose
    private long registeredAt;
    @Expose
    private long passwordChangedAt;
    @Expose
    private long dateOfBirth;
    @Expose
    private Boolean suspended;
    @Expose
    private Boolean blocked;
    @Expose
    private Boolean secured;
    @Expose
    private Boolean migrated;
    @Expose
    private Boolean emailVerified;
    @Expose
    private Boolean legacyUser;
    @Expose
    private String emailSubscriptionStatus;
    @Expose
    private String emailSubscriptionKey;
    @Expose
    private Boolean verifiedByParent;
    @Expose
    private Boolean hashed;
    @Expose
    private Boolean fromMigratedUser;

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getRegisterIp() {
        return registerIp;
    }

    public String getMigratedFrom() {
        return migratedFrom;
    }

    public long getMigratedAt() {
        return migratedAt;
    }

    public long getRegisteredAt() {
        return registeredAt;
    }

    public long getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public long getDateOfBirth() {
        return dateOfBirth;
    }

    public Boolean getSuspended() {
        return suspended;
    }

    public Boolean getBlocked() {
        return blocked;
    }

    public Boolean getSecured() {
        return secured;
    }

    public Boolean getMigrated() {
        return migrated;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public Boolean getLegacyUser() {
        return legacyUser;
    }

    public String getEmailSubscriptionStatus() {
        return emailSubscriptionStatus;
    }

    public String getEmailSubscriptionKey() {
        return emailSubscriptionKey;
    }

    public Boolean getVerifiedByParent() {
        return verifiedByParent;
    }

    public Boolean getHashed() {
        return hashed;
    }

    public Boolean getFromMigratedUser() {
        return fromMigratedUser;
    }
}
