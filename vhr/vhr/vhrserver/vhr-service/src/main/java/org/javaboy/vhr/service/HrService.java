package org.javaboy.vhr.service;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.*;
import org.javaboy.vhr.mapper.HrMapper;
import org.javaboy.vhr.mapper.HrRoleMapper;
import org.javaboy.vhr.model.Hr;
import org.javaboy.vhr.model.Role;
import org.javaboy.vhr.utils.HrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @作者 江南一点雨
 * @公众号 江南一点雨
 * @微信号 a_java_boy
 * @GitHub https://github.com/lenve
 * @博客 http://wangsong.blog.csdn.net
 * @网站 http://www.javaboy.org
 * @时间 2019-09-20 8:21
 */
@Service
public class HrService implements UserDetailsService {
    @Autowired
    HrMapper hrMapper;
    @Autowired
    HrRoleMapper hrRoleMapper;
    @Autowired
    ManagementClient managementClient;
    @Value("${authing.config.appId}")
    String namespace;

    // 更改为 authing 登录逻辑
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        Hr hr = hrMapper.loadUserByUsername(username);
//        if (hr == null) {
//            throw new UsernameNotFoundException("用户名不存在!");
//        }
//        hr.setRoles(hrMapper.getHrRolesById(hr.getId()));
//        return hr;
        GetUserDto getUserDto = new GetUserDto();
        getUserDto.setUserIdType("username");
        getUserDto.setUserId(username);
        UserSingleRespDto respDto = managementClient.getUser(getUserDto);
        UserDto authingUser = respDto.getData();
        return convertAuthingUserToHr(authingUser);
    }

    // authing 用户实体类和 hr 实体类的对应
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

        // authing 没有 password、userface、remark 属性

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

    public List<Hr> getAllHrs(String keywords) {
        return hrMapper.getAllHrs(HrUtils.getCurrentHr().getId(),keywords);
    }

    public Integer updateHr(Hr hr) {
        return hrMapper.updateByPrimaryKeySelective(hr);
    }

    @Transactional
    public boolean updateHrRole(Integer hrid, Integer[] rids) {
        hrRoleMapper.deleteByHrid(hrid);
        return hrRoleMapper.addRole(hrid, rids) == rids.length;
    }

    public Integer deleteHrById(Integer id) {
        return hrMapper.deleteByPrimaryKey(id);
    }

    public List<Hr> getAllHrsExceptCurrentHr() {
        return hrMapper.getAllHrsExceptCurrentHr(HrUtils.getCurrentHr().getId());
    }

    public Integer updateHyById(Hr hr) {
        return hrMapper.updateByPrimaryKeySelective(hr);
    }

    public boolean updateHrPasswd(String oldpass, String pass, Integer hrid) {
        Hr hr = hrMapper.selectByPrimaryKey(hrid);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (encoder.matches(oldpass, hr.getPassword())) {
            String encodePass = encoder.encode(pass);
            Integer result = hrMapper.updatePasswd(hrid, encodePass);
            if (result == 1) {
                return true;
            }
        }
        return false;
    }

    public Integer updateUserface(String url, Integer id) {
        return hrMapper.updateUserface(url, id);
    }
}
