package org.javaboy.vhr.controller.system.basic;

import cn.hutool.core.util.StrUtil;
import org.javaboy.vhr.model.Menu;
import org.javaboy.vhr.model.RespBean;
import org.javaboy.vhr.model.Role;
import org.javaboy.vhr.model.UpdateAuthResDto;
import org.javaboy.vhr.service.MenuService;
import org.javaboy.vhr.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@RestController
@RequestMapping("/system/basic/permission")
public class PermissionController {
    @Autowired
    RoleService roleService;
    @Autowired
    MenuService menuService;
    @GetMapping("/")
    public List<Role> getAllRoles() {
        return roleService.getAllRoles();
    }
    @GetMapping("/menus")
    public List<Menu> getAllMenus() {
        return menuService.getAllMenus();
    }

    @GetMapping("/mids/{roleName}")
    public List<Integer> getMidsByRoleName(@PathVariable String roleName) {
        return menuService.getMidsByRoleName(roleName);
    }

    @PostMapping("/")
    public RespBean updateMenuRole(@RequestBody UpdateAuthResDto updateAuthResDto) {
//        if (menuService.updateMenuRole(updateAuthResDto)) {
//            return RespBean.ok("更新成功!");
//        }
//        return RespBean.error("更新失败!");
        // TODO
        return RespBean.error("Authing 暂未开放相关 API!");
    }

    @PostMapping("/role")
    public RespBean addRole(@RequestBody Role role) {
        return roleService.addAuthingRole(role);
    }

    @DeleteMapping("/role/{roleName}")
    public RespBean deleteRoleByName(@PathVariable String roleName) {
        if(StrUtil.equals("ROLE_superAdmin",roleName)){
            return RespBean.error("不允许删除超级管理员角色!");
        }
        return roleService.deleteRoleByName(roleName);
    }
}
