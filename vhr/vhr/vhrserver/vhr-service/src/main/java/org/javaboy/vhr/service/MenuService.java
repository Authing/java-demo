package org.javaboy.vhr.service;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.*;
import cn.authing.sdk.java.dto.authentication.UserInfo;
import cn.authing.sdk.java.util.JsonUtils;
import org.javaboy.vhr.mapper.MenuMapper;
import org.javaboy.vhr.mapper.MenuRoleMapper;
import org.javaboy.vhr.model.Hr;
import org.javaboy.vhr.model.Menu;
import org.javaboy.vhr.model.MenuRole;
import org.javaboy.vhr.model.UpdateAuthResDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @作者 江南一点雨
 * @公众号 江南一点雨
 * @微信号 a_java_boy
 * @GitHub https://github.com/lenve
 * @博客 http://wangsong.blog.csdn.net
 * @网站 http://www.javaboy.org
 * @时间 2019-09-27 7:13
 */
@Service
@CacheConfig(cacheNames = "menus_cache")
public class MenuService {
    @Autowired
    MenuMapper menuMapper;
    @Autowired
    MenuRoleMapper menuRoleMapper;
    @Autowired
    AuthenticationClient authenticationClient;
    @Autowired
    ManagementClient managementClient;
    @Value("${authing.config.appId}")
    String AUTHING_APP_ID;

    public List<Menu> getMenusByHrId() {
        return menuMapper.getMenusByHrId(((Hr) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId());
    }

    @Cacheable
    public List<Menu> getMenusByAccessToken(String accessToken) {
        UserInfo userInfo = authenticationClient.getUserInfoByAccessToken(accessToken);
        String authingUserId = userInfo.getSub();
        List<String> menuIds = getAuthingUserResources(authingUserId);
        List<Menu> menuList = new ArrayList<>();
        menuIds.forEach((menuId) -> {
            Menu menu = menuMapper.getMenuById(menuId);
            menuList.add(menu);
        });
        return mergeChildren(menuList);
    }

    // 把父级相同的子菜单合并到一个父级下
    public List<Menu> mergeChildren(List<Menu> menuList){
        List<Menu> res = new ArrayList<>();
        Map<Integer,Menu> menuMap = new HashMap<>();
        menuList.forEach(menu -> {
            if(menuMap.containsKey(menu.getId())){
                List<Menu> childrenList = menuMap.get(menu.getId()).getChildren();
                for(Menu childrenMenu : menu.getChildren()){
                    childrenList.add(childrenMenu);
                }
                menuMap.get(menu.getId()).setChildren(childrenList);
            }else{
                menuMap.put(menu.getId(),menu);
            }
        });
        for(Map.Entry<Integer,Menu> entry : menuMap.entrySet()){
            res.add(entry.getValue());
        }
        return res;
    }

    // 获取 authing 用户的授权资源列表
    public List<String> getAuthingUserResources(String authingUserId){
        GetUserRolesDto getUserRolesDto = new GetUserRolesDto();
        getUserRolesDto.setNamespace(AUTHING_APP_ID);
        getUserRolesDto.setUserId(authingUserId);
        // 获取用户角色
        RolePaginatedRespDto userRoles = managementClient.getUserRoles(getUserRolesDto);
        List<RoleDto> roles = userRoles.getData().getList();
        // 用户允许访问的全部 menuId
        List<String> resources = new ArrayList<>();
        roles.forEach((dto) -> {
            GetRoleAuthorizedResourcesDto resourcesDto = new GetRoleAuthorizedResourcesDto();
            resourcesDto.setNamespace(AUTHING_APP_ID);
            resourcesDto.setCode(dto.getCode());
            resourcesDto.setResourceType(ResourceItemDto.ResourceType.API.getValue());
            // 获取角色资源
            RoleAuthorizedResourcePaginatedRespDto roleAuthorizedResources = managementClient.getRoleAuthorizedResources(resourcesDto);
            List<RoleAuthorizedResourcesRespDto> list = roleAuthorizedResources.getData().getList();
            List<String> resource = new ArrayList<>();
            list.stream().forEach(item -> {
                // 分割字符获取全部 id
                String[] ids = item.getResourceCode().split(":")[1].split("_");
                resource.addAll(Arrays.asList(ids));
            });
            resources.addAll(resource);
        });
        return resources;
    }

    public List<Integer> getAuthingRoleResources(String roleName){
        GetRoleAuthorizedResourcesDto resourcesDto = new GetRoleAuthorizedResourcesDto();
        resourcesDto.setNamespace(AUTHING_APP_ID);
        resourcesDto.setCode(roleName);
        resourcesDto.setResourceType(ResourceItemDto.ResourceType.API.getValue());
        // 获取角色资源
        RoleAuthorizedResourcePaginatedRespDto roleAuthorizedResources = managementClient.getRoleAuthorizedResources(resourcesDto);
        List<RoleAuthorizedResourcesRespDto> list = roleAuthorizedResources.getData().getList();
        List<String> resource = new ArrayList<>();
        list.stream().forEach(item -> {
            // 分割字符获取全部 id
            String[] ids = item.getResourceCode().split(":")[1].split("_");
            resource.addAll(Arrays.asList(ids));
        });
        List<Integer> res = new ArrayList<>();
        resource.forEach(item -> {
            res.add(Integer.valueOf(item));
        });
        return res;
    }

    @Cacheable
    public List<Menu> getAllMenusWithRole() {
        return menuMapper.getAllMenusWithRole();
    }

    // 以父子形式返回
    public List<Menu> getAllMenus() {
        return menuMapper.getAllMenus();
    }

    // 同级别形式返回
    public List<Menu> getMenus(){
        return menuMapper.getMenus();
    }

    public List<Integer> getMidsByRid(Integer rid) {
        return menuMapper.getMidsByRid(rid);
    }

    public List<Integer> getMidsByRoleName(String roleName) {
        return getAuthingRoleResources(roleName);
    }

    @Transactional
    public boolean updateMenuRole(Integer rid, Integer[] mids) {
        menuRoleMapper.deleteByRid(rid);
        if (mids == null || mids.length == 0) {
            return true;
        }
        Integer result = menuRoleMapper.insertRecord(rid, mids);
        return result==mids.length;
    }

    public List<String> selectAuthorizedRoles(Integer menuId){
        List<String> authorizedRoles = new ArrayList<>();
        // 获取全部角色
        ListRolesDto listRolesDto = new ListRolesDto();
        listRolesDto.setNamespace(AUTHING_APP_ID);
        List<RoleDto> roleDtoList = managementClient.listRoles(listRolesDto).getData().getList();
        roleDtoList.forEach(roleDto -> {
            List<Integer> resources = getAuthingRoleResources(roleDto.getCode());
            if(resources.contains(menuId)){
                authorizedRoles.add(roleDto.getCode());
            }
        });
        return authorizedRoles;
    }

    @Transactional
    public boolean updateMenuRole(UpdateAuthResDto updateAuthResDto) {
        // TODO 未找到 API
        return true;
    }
}
