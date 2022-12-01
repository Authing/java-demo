package org.javaboy.vhr.service;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.*;
import cn.authing.sdk.java.dto.authentication.UserInfo;
import cn.hutool.core.util.StrUtil;
import org.javaboy.vhr.converter.AuthingUserToHrConverter;
import org.javaboy.vhr.mapper.RoleMapper;
import org.javaboy.vhr.model.RespBean;
import org.javaboy.vhr.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @作者 江南一点雨
 * @公众号 江南一点雨
 * @微信号 a_java_boy
 * @GitHub https://github.com/lenve
 * @博客 http://wangsong.blog.csdn.net
 * @网站 http://www.javaboy.org
 * @时间 2019-10-01 19:41
 */
@Service
public class RoleService {
    @Autowired
    RoleMapper roleMapper;
    @Autowired
    AuthenticationClient authenticationClient;
    @Value("${authing.config.appId}")
    String namespace;
    @Autowired
    AuthingUserToHrConverter authingUserToHrConverter;
    @Autowired
    ManagementClient managementClient;
    public List<Role> getAllRoles() {
        ListRolesDto reqDto = new ListRolesDto();
        reqDto.setNamespace(namespace);
        RolePaginatedRespDto respDto = managementClient.listRoles(reqDto);
        List<RoleDto> authingRoleList = respDto.getData().getList();
        List<Role> roleList = new ArrayList<>();
        authingRoleList.forEach(authingRole -> {
            Role role = authingUserToHrConverter.convertAuthingRoleToRole(authingRole);
            roleList.add(role);
        });
        return roleList;
    }

    public Integer addRole(Role role) {
        if (!role.getName().startsWith("ROLE_")) {
            role.setName("ROLE_" + role.getName());
        }
        return roleMapper.insert(role);
    }

    public RespBean addAuthingRole(Role role) {
        CreateRoleDto createRoleDto = new CreateRoleDto();
        if (!role.getName().startsWith("ROLE_")) {
            createRoleDto.setCode("ROLE_" + role.getName());
        }else {
            createRoleDto.setCode(role.getName());
        }
        createRoleDto.setDescription(role.getNameZh());
        createRoleDto.setNamespace(namespace);
        RoleSingleRespDto respDto = managementClient.createRole(createRoleDto);
        return respDto.getStatusCode() == 200 ?
                RespBean.ok("添加成功！") : RespBean.error(respDto.getMessage());
    }

    public Integer deleteRoleById(Integer rid) {
        return roleMapper.deleteByPrimaryKey(rid);
    }

    public RespBean deleteRoleByName(String roleName) {
        DeleteRoleDto reqDto = new DeleteRoleDto();
        reqDto.setNamespace(namespace);
        List<String> codeList = new ArrayList<>();
        codeList.add(roleName);
        reqDto.setCodeList(codeList);
        IsSuccessRespDto respDto = managementClient.deleteRolesBatch(reqDto);
        return respDto.getStatusCode() == 200 ?
                RespBean.ok("删除成功！") : RespBean.error(respDto.getMessage());
    }
}
