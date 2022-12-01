package org.javaboy.vhr.model;

import java.util.List;

public class UpdateAuthResDto {
    private String roleName;
    private List<String> menuIds;

    public UpdateAuthResDto() {
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<String> getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(List<String> menuIds) {
        this.menuIds = menuIds;
    }
}
