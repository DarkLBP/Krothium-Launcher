package kml.auth;

import com.google.gson.annotations.Expose;

public class AuthenticateRequest {
    @Expose
    private AuthAgent agent = new AuthAgent();
    @Expose
    private String clientToken;
    @Expose
    private String password;
    @Expose
    private boolean requestUser = true;
    @Expose
    private String username;

    public AuthAgent getAgent() {
        return agent;
    }

    public void setAgent(AuthAgent agent) {
        this.agent = agent;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRequestUser() {
        return requestUser;
    }

    public void setRequestUser(boolean requestUser) {
        this.requestUser = requestUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
