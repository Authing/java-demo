package org.javaboy.vhr;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.*;
import cn.authing.sdk.java.model.AuthenticationClientOptions;
import cn.authing.sdk.java.model.ManagementClientOptions;
import cn.authing.sdk.java.util.JsonUtils;
import org.javaboy.vhr.converter.AuthingUserEmployeeConverter;
import org.javaboy.vhr.mapper.MenuMapper;
import org.javaboy.vhr.model.Menu;
import org.javaboy.vhr.service.MenuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class selfTest {

    @Autowired
    MenuService menuService;

    @Autowired
    MenuMapper menuMapper;

    @Autowired
    ManagementClient managementClient;

    @Autowired
    AuthenticationClient authenticationClient;

    @Value("${authing.config.appId}")
    String namespace;

    @Autowired
    AuthingUserEmployeeConverter authingUserEmployeeConverter;

    @Test
    public void testAuth() throws IOException, ParseException {

    }

    @Test
    public void testMag(){
//        GetRoleAuthorizedResourcesDto resourcesDto = new GetRoleAuthorizedResourcesDto();
//        resourcesDto.setNamespace(namespace);
//        resourcesDto.setCode("ROLE_train");
//        resourcesDto.setResourceType(ResourceItemDto.ResourceType.API.getValue());
//        // 获取角色资源
//        RoleAuthorizedResourcePaginatedRespDto roleAuthorizedResources = managementClient.getRoleAuthorizedResources(resourcesDto);
//        List<RoleAuthorizedResourcesRespDto> list = roleAuthorizedResources.getData().getList();

//        IsActionAllowedDto isActionAllowedDto = new IsActionAllowedDto();
//        isActionAllowedDto.setNamespace(namespace);
//        isActionAllowedDto.setUserId("637eccbb4fd5272f44b6dc7f");
//        isActionAllowedDto.setResource("menuId:7");
//        isActionAllowedDto.setAction("read12312");
//        IsActionAllowedRespDtp actionAllowed = managementClient.isActionAllowed(isActionAllowedDto);

        List<Integer> allMenuIds = menuService.getAllMenuIds();
        System.out.println(allMenuIds.toString());
    }


    // 执行批量创建用户
    @Test
    public void testNormal(){
        authingUserEmployeeConverter.batchCreateAuthingUser();
    }
}
