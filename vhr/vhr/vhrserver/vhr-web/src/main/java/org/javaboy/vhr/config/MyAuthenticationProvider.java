package org.javaboy.vhr.config;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.*;
import org.javaboy.vhr.Exception.AuthingException;
import org.javaboy.vhr.model.CurrentUserAccessToken;
import org.javaboy.vhr.service.HrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public class MyAuthenticationProvider extends DaoAuthenticationProvider {
    @Autowired
    AuthenticationClient authenticationClient;
    @Autowired
    HrService hrService;
    @Value("${authing.config.appId}")
    String namespace;
    @Autowired
    ManagementClient managementClient;

    public MyAuthenticationProvider() {
    }

    // 自定义密码校验方法
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        String username = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();
        LoginTokenRespDto respDto = authenticationClient.signInByUsernamePassword(username, password, new SignInOptionsDto());
        if(respDto.getStatusCode() != 200){
            throw new AuthingException(respDto.getMessage());
        }else{
            // 把 accessToken 存到 threadLocal 中
            CurrentUserAccessToken.setAccessToken(respDto.getData().getAccessToken());
        }
        GetUserRolesDto reqDto = new GetUserRolesDto();
        reqDto.setUserIdType("username");
        reqDto.setUserId(username);
        reqDto.setNamespace(namespace);
        RolePaginatedRespDto respDto1 = managementClient.getUserRoles(reqDto);
        List<RoleDto> roleList = respDto1.getData().getList();
        // 普通员工没有赋予角色
        if(roleList.size() == 0){
            throw new AuthingException("普通员工不可登录后台系统！");
        }
//        super.additionalAuthenticationChecks(userDetails, authentication);
    }
}
