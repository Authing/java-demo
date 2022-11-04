package com.example.yin2.Enum;

/**
 * 用户角色枚举类
 */
public enum RoleCodeEnum {
    USER("user"),
    VIP("vip"),
    ADMIN("admin"),
    SUPER_ADMIN("superAdmin");

    private final String value;
    RoleCodeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
