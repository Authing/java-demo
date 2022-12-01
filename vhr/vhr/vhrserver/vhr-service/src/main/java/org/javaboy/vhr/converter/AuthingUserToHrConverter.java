package org.javaboy.vhr.converter;

import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.GetUserRolesDto;
import cn.authing.sdk.java.dto.RoleDto;
import cn.authing.sdk.java.dto.RolePaginatedRespDto;
import cn.authing.sdk.java.dto.UserDto;
import org.javaboy.vhr.model.Hr;
import org.javaboy.vhr.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class AuthingUserToHrConverter {
    @Autowired
    ManagementClient managementClient;
    @Value("${authing.config.appId}")
    String namespace;

    // authing 用户转换成 hr 实体类
    public Hr convertAuthingUserToHr(UserDto authingUser){
        Hr hr = new Hr();
        hr.setName(authingUser.getName());
        hr.setPhone(authingUser.getPhone());
        hr.setAddress(authingUser.getAddress());
        if(authingUser.getStatus().getValue()=="Activated") {
            hr.setEnabled(true);
        }else{
            hr.setEnabled(false);
        }
        hr.setUsername(authingUser.getUsername());
        hr.setUserface(authingUser.getPhoto());
        // authing 用户不自带 remark 属性，需要自定义用户字段
        if(authingUser.getCustomData() != null){
            LinkedHashMap<String,String> map = (LinkedHashMap) authingUser.getCustomData();
            hr.setRemark(map.get("remark"));
        }

        // authing 用户的 password 属性无法获取（解密）

        // 获取用户角色
        List<Role> roleList = new ArrayList<>();
        GetUserRolesDto getUserRolesDto = new GetUserRolesDto();
        getUserRolesDto.setUserIdType("username");
        getUserRolesDto.setUserId(authingUser.getUsername());
        getUserRolesDto.setNamespace(namespace);
        RolePaginatedRespDto respDto = managementClient.getUserRoles(getUserRolesDto);
        List<RoleDto> authingRoleList = respDto.getData().getList();
        for (RoleDto authingRole:authingRoleList){
            roleList.add(convertAuthingRoleToRole(authingRole));
        }
        hr.setRoles(roleList);

        hr.setOwnerId(authingUser.getUserId());
        return hr;
    }

    // authing 用户角色和 Role 实体类的对应
    public Role convertAuthingRoleToRole(RoleDto authingRole){
        Role role = new Role();
        role.setName(authingRole.getCode());
        role.setNameZh(authingRole.getDescription());
        return role;
    }
}