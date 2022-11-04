package com.example.yin2.domain;

import java.util.List;

/**
 * 修改用户角色用到的参数类
 */
public class UserRoleParam {
    private String userId;

    private List<String> codeList;

    public UserRoleParam() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getCodeList() {
        return codeList;
    }

    public void setCodeList(List<String> codeList) {
        this.codeList = codeList;
    }
}
