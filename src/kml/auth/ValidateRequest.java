package kml.auth;

import com.google.gson.annotations.Expose;

public class ValidateRequest {
    @Expose
    private String accessToken;
    @Expose
    private String clientToken;
    @Expose
    private boolean requestUser = true;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public boolean isRequestUser() {
        return requestUser;
    }

    public void setRequestUser(boolean requestUser) {
        this.requestUser = requestUser;
    }
}
