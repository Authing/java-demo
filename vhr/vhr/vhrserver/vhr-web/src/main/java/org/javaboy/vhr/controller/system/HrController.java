package org.javaboy.vhr.controller.system;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.*;
import cn.authing.sdk.java.dto.authentication.UserInfo;
import org.javaboy.vhr.converter.AuthingUserToHrConverter;
import org.javaboy.vhr.model.Hr;
import org.javaboy.vhr.model.RespBean;
import org.javaboy.vhr.model.Role;
import org.javaboy.vhr.model.UpdateHrRoleDto;
import org.javaboy.vhr.service.HrService;
import org.javaboy.vhr.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @作者 江南一点雨
 * @公众号 江南一点雨
 * @微信号 a_java_boy
 * @GitHub https://github.com/lenve
 * @博客 http://wangsong.blog.csdn.net
 * @网站 http://www.javaboy.org
 * @时间 2019-10-24 8:09
 */
@RestController
@RequestMapping("/system/hr")
public class HrController {
    @Autowired
    HrService hrService;
    @Autowired
    RoleService roleService;

    // 模糊搜索 hr
    @GetMapping("/search")
    public List<Hr> getAllHrs(@RequestParam(defaultValue = "") String keywords) {
        return hrService.getAllHrs(keywords);
    }

    // 改变账号状态
    @PutMapping("/status")
    public RespBean updateHrStatus(@RequestBody Hr hr) {
        return hrService.updateHrStatus(hr);
    }
    @GetMapping("/roles")
    public List<Role> getAllRoles() {
        return roleService.getAllRoles();
    }

    @PostMapping("/role")
    public RespBean updateHrRole(@RequestBody UpdateHrRoleDto updateHrRoleDto) {
        return hrService.updateHrRole(updateHrRoleDto.getAuthingUserId(), updateHrRoleDto.getRoleCodes());
    }

    @DeleteMapping("/{authingUserId}")
    public RespBean deleteHrByAuthingUserId(@PathVariable String authingUserId) {
        return hrService.deleteHrByAuthingUserId(authingUserId);
    }
}
