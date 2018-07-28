package kml.auth.user;

import com.google.gson.annotations.Expose;

public class SelectedUser {
    @Expose
    private String account;
    @Expose
    private String profile;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
