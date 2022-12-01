package org.javaboy.vhr.model;

public class UpdateHrRoleDto {
    String authingUserId;
    String[] roleCodes;

    public UpdateHrRoleDto() {
    }

    public String getAuthingUserId() {
        return authingUserId;
    }

    public void setAuthingUserId(String authingUserId) {
        this.authingUserId = authingUserId;
    }

    public String[] getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(String[] roleCodes) {
        this.roleCodes = roleCodes;
    }
}
