package com.example.yin.controller;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.*;
import cn.authing.sdk.java.dto.authentication.UserInfo;
import com.example.yin.Enum.RoleCodeEnum;
import com.example.yin.common.ErrorMessage;
import com.example.yin.common.SuccessMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


@RestController
public class AdminController {
    @Autowired
    private AuthenticationClient authenticationClient;

    @Autowired
    private ManagementClient managementClient;

    // 判断是否登录成功
    @ResponseBody
    @RequestMapping(value = "/admin/login/status", method = RequestMethod.POST)
    public Object loginStatus(HttpServletRequest req) {
        // 从 url 中获取参数
        String username = req.getParameter("username").trim();
        String password = req.getParameter("password").trim();
        // 调用 authing "用户名+密码"方式的 SDK 登录
        LoginTokenRespDto loginTokenRespDto =
                authenticationClient.signInByUsernamePassword(username, password, new SignInOptionsDto());
        //返回结果中层层获取 userId
        String accessToken = loginTokenRespDto.getData().getAccessToken();
        UserInfo userInfo = authenticationClient.getUserInfoByAccessToken(accessToken);
        String userId = userInfo.getSub();
        //通过 userId 获取 用户角色
        GetUserRolesDto getUserRolesDto = new GetUserRolesDto();
        getUserRolesDto.setUserId(userId);
        getUserRolesDto.setNamespace(authenticationClient.getOptions().getAppId());
        RolePaginatedRespDto rolePaginatedRespDto = managementClient.getUserRoles(getUserRolesDto);
        List<RoleDto> roleDtoList = rolePaginatedRespDto.getData().getList();
        // 将结果类中的角色属性封装成一个 list
        List<String> roleList = new ArrayList<>();
        for(RoleDto roleDto:roleDtoList){
            roleList.add(roleDto.getCode());
        }
        //判断用户是不是管理员
        boolean isAdmin = false;
        for(String role:roleList){
            if(RoleCodeEnum.ADMIN.getValue().equals(role) ||
                    RoleCodeEnum.SUPER_ADMIN.getValue().equals(role)){
                isAdmin = true;
                break;
            }
        }
        if (loginTokenRespDto.getStatusCode() == 200) {
            if(isAdmin){
                // 只有请求成功且是管理员才能登录后台系统，同时将 accessToken 返回到前端
                return new SuccessMessage<String>("登录成功",loginTokenRespDto.getData().getAccessToken()).getMessage();
            }else{
                return new ErrorMessage("只有管理员才能登录后台管理系统").getMessage();
            }
        } else {
            // 该项目未进行错误码区分，故此处仅传递错误信息；Authing 定义的错误码可以通过 loginTokenRespDto.getApiCode() 获取
            return new ErrorMessage(loginTokenRespDto.getMessage()).getMessage();
        }
    }

    /**
     * 登出
     */
    @PostMapping("admin/logout")
    public Object logout(@CookieValue("accessToken") String accessToken){
        Boolean flag = authenticationClient.revokeToken(accessToken);
        if(flag){
            return new SuccessMessage<>("退出登录").getMessage();
        }else{
            return new ErrorMessage("退出登录出错").getMessage();
        }
    }
}
