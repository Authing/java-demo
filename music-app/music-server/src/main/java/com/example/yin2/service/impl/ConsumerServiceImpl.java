package com.example.yin2.service.impl;

import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.*;
import com.example.yin2.Enum.RoleCodeEnum;
import com.example.yin2.common.ErrorMessage;
import com.example.yin2.common.SuccessMessage;
import com.example.yin2.dao.ConsumerMapper;
import com.example.yin2.domain.Consumer;
import com.example.yin2.domain.UserRoleParam;
import com.example.yin2.service.ConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConsumerServiceImpl implements ConsumerService {

    @Autowired
    private ConsumerMapper consumerMapper;
    @Value("${authing.config.appId}")
    String AUTHING_APP_ID;
    @Autowired
    private ManagementClient managementClient;

    /**
     * 新增用户
     */
    @Override
    public boolean addUser(Consumer consumer) {
        return consumerMapper.insertSelective(consumer) > 0;
    }

    @Override
    public boolean updateUserMsg(Consumer consumer) {
        return consumerMapper.updateUserMsg(consumer) > 0;
    }

    @Override
    public boolean updatePassword(Consumer consumer) {
        return consumerMapper.updatePassword(consumer) > 0;
    }

    @Override
    public boolean updateUserAvator(Consumer consumer) {
        return consumerMapper.updateUserAvator(consumer) > 0;
    }

    @Override
    public boolean existUser(String username) {
        return consumerMapper.existUsername(username) > 0;
    }

    @Override
    public boolean veritypasswd(String username, String password) {
        return consumerMapper.verifyPassword(username, password) > 0;
    }

    // 删除用户
    @Override
    public boolean deleteUser(Integer id) {
        return consumerMapper.deleteUser(id) > 0;
    }

    @Override
    public List<Consumer> allUser() {
        return consumerMapper.allUser();
    }

    @Override
    public List<Consumer> userOfId(Integer id) {
        return consumerMapper.userOfId(id);
    }

    @Override
    public List<Consumer> loginStatus(String username) {
        return consumerMapper.loginStatus(username);
    }

    @Transactional
    @Override
    public boolean changeRole(UserRoleParam param) {
        String userId = param.getUserId();
        List<String> codeList = param.getCodeList();
        // 获取全部用户角色
        ListRolesDto listRolesDto = new ListRolesDto();
        listRolesDto.setNamespace(AUTHING_APP_ID);
        // 调用 authing 接口
        RolePaginatedRespDto rolePaginatedRespDto = managementClient.listRoles(listRolesDto);
        List<RoleDto> roleDtoList = rolePaginatedRespDto.getData().getList();
        // 设置清空和添加的共同目标 —— 用户
        List<TargetDto> target = new ArrayList<>();
        TargetDto targetDto = new TargetDto();
        targetDto.setTargetType(TargetDto.TargetType.USER);
        targetDto.setTargetIdentifier(userId);
        target.add(targetDto);
        // 清空该用户角色
        for(RoleDto roleDto:roleDtoList){
            RevokeRoleDto revokeRoleDto = new RevokeRoleDto();
            revokeRoleDto.setTargets(target);
            revokeRoleDto.setNamespace(AUTHING_APP_ID);
            // code 不同
            revokeRoleDto.setCode(roleDto.getCode());
            // 调用 authing 接口
            IsSuccessRespDto isSuccessRespDto = managementClient.revokeRole(revokeRoleDto);
            if(isSuccessRespDto.getStatusCode() != 200){
                return false;
            }
        }
        // 添加角色
        for(String code:codeList){
            AssignRoleDto assignRoleDto = new AssignRoleDto();
            assignRoleDto.setTargets(target);
            assignRoleDto.setNamespace(AUTHING_APP_ID);
            assignRoleDto.setCode(code);
            // 调用 authing 接口
            IsSuccessRespDto isSuccessRespDto = managementClient.assignRole(assignRoleDto);
            if(isSuccessRespDto.getStatusCode() != 200){
                return false;
            }
        }
        return true;
    }
}
