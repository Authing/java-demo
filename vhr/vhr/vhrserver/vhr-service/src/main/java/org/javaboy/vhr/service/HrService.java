package org.javaboy.vhr.service;

import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.*;
import cn.hutool.core.util.StrUtil;
import org.javaboy.vhr.Exception.AuthingException;
import org.javaboy.vhr.converter.AuthingUserToHrConverter;
import org.javaboy.vhr.mapper.HrMapper;
import org.javaboy.vhr.mapper.HrRoleMapper;
import org.javaboy.vhr.model.Hr;
import org.javaboy.vhr.model.RespBean;
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
    @Autowired
    AuthingUserToHrConverter authingUserToHrConverter;
    @Value("${authing.config.appId}")
    String namespace;

    // 更改为 authing 获取用户信息逻辑
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        GetUserDto getUserDto = new GetUserDto();
        getUserDto.setUserIdType("username");
        getUserDto.setUserId(username);
        UserSingleRespDto respDto = managementClient.getUser(getUserDto);
        UserDto authingUser = respDto.getData();
        return authingUserToHrConverter.convertAuthingUserToHr(authingUser);
    }

    public List<Hr> getAllHrs(String keywords){
        List<UserDto> userList = new ArrayList<>();
        ListUsersRequestDto reqDto = new ListUsersRequestDto();
        if(StrUtil.isNotBlank(keywords)) {
            reqDto.setKeywords(keywords);
        }
        ListUsersOptionsDto optionsDto = new ListUsersOptionsDto();
        List<String> searchList = new ArrayList<>();
        // 设置模糊搜索的字段
        searchList.add("username");
        optionsDto.setFuzzySearchOn(searchList);
        // 获取用户自定义字段 remark
        optionsDto.setWithCustomData(true);
        reqDto.setOptions(optionsDto);

        // 只选出管理员用户（externalId 为空）
        List<ListUsersAdvancedFilterItemDto> advancedFilterList = new ArrayList<>();
        ListUsersAdvancedFilterItemDto itemDto = new ListUsersAdvancedFilterItemDto();
        itemDto.setField("externalId");
        itemDto.setOperator(ListUsersAdvancedFilterItemDto.Operator.IS_NULL);
        advancedFilterList.add(itemDto);
        reqDto.setAdvancedFilter(advancedFilterList);

        UserPaginatedRespDto respDto = managementClient.listUsers(reqDto);
        userList.addAll(respDto.getData().getList());
        List<Hr> hrList = new ArrayList<>();
        userList.forEach(userDto -> {
            Hr hr = authingUserToHrConverter.convertAuthingUserToHr(userDto);
            hrList.add(hr);
        });
        return hrList;
    }



    public Integer updateHr(Hr hr) {
        return hrMapper.updateByPrimaryKeySelective(hr);
    }

    public boolean updateHrStatus(Hr hr){
        UpdateUserReqDto reqDto = new UpdateUserReqDto();
        reqDto.setUserId(hr.getOwnerId());
        if(hr.isEnabled()){
            reqDto.setStatus(UpdateUserReqDto.Status.ACTIVATED);
        }else{
            reqDto.setStatus(UpdateUserReqDto.Status.DEACTIVATED);
        }
        UserSingleRespDto respDto = managementClient.updateUser(reqDto);
        if (respDto.getStatusCode() == 200) {
            return true;
        }
        return false;
    }

    @Transactional
    public boolean updateHrRole(String authingUserId, String[] roleCodes) {
        ListRolesDto listRolesDto = new ListRolesDto();
        listRolesDto.setNamespace(namespace);
        // 获取全部用户角色
        RolePaginatedRespDto rolePaginatedRespDto = managementClient.listRoles(listRolesDto);
        List<RoleDto> roleDtoList = rolePaginatedRespDto.getData().getList();
        List<TargetDto> target = new ArrayList<>();
        TargetDto targetDto = new TargetDto();
        targetDto.setTargetType(TargetDto.TargetType.USER);
        targetDto.setTargetIdentifier(authingUserId);
        target.add(targetDto);
        // 清空该用户角色
        for(RoleDto roleDto:roleDtoList){
            RevokeRoleDto revokeRoleDto = new RevokeRoleDto();
            revokeRoleDto.setTargets(target);
            revokeRoleDto.setNamespace(namespace);
            // code 不同
            revokeRoleDto.setCode(roleDto.getCode());
            IsSuccessRespDto isSuccessRespDto = managementClient.revokeRole(revokeRoleDto);
            if(isSuccessRespDto.getStatusCode() != 200){
                return false;
            }
        }
        // 添加角色
        for(String code:roleCodes){
            AssignRoleDto assignRoleDto = new AssignRoleDto();
            assignRoleDto.setTargets(target);
            assignRoleDto.setNamespace(namespace);
            assignRoleDto.setCode(code);
            IsSuccessRespDto isSuccessRespDto = managementClient.assignRole(assignRoleDto);
            if(isSuccessRespDto.getStatusCode() != 200){
                return false;
            }
        }
        return true;
    }

    public Integer deleteHrById(Integer id) {
        return hrMapper.deleteByPrimaryKey(id);
    }

    // 删除用户（可批量）
    public boolean deleteHrByAuthingUserId(String authingUserId){
        DeleteUsersBatchDto reqDto = new DeleteUsersBatchDto();
        List<String> authingUserIdList = new ArrayList<>();
        authingUserIdList.add(authingUserId);
        reqDto.setUserIds(authingUserIdList);
        IsSuccessRespDto respDto = managementClient.deleteUsersBatch(reqDto);
        return respDto.getData().getSuccess();
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
