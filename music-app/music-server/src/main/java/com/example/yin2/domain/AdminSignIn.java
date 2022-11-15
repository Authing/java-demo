package com.example.yin2.domain;

public class AdminSignIn {
    private String accessToken;

    private boolean isSuperAdmin;

    public AdminSignIn() {
    }

    public AdminSignIn(String accessToken, boolean isSuperAdmin) {
        this.accessToken = accessToken;
        this.isSuperAdmin = isSuperAdmin;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean isSuperAdmin() {
        return isSuperAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        isSuperAdmin = superAdmin;
    }
}
