package com.example.yin.controller;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.*;
import cn.hutool.core.util.StrUtil;
import com.example.yin.Enum.RoleCodeEnum;
import com.example.yin.common.FatalMessage;
import com.example.yin.common.ErrorMessage;
import com.example.yin.common.SuccessMessage;
import com.example.yin.common.WarningMessage;
import com.example.yin.constant.Constants;
import com.example.yin.domain.Consumer;
import com.example.yin.domain.UserRoleParam;
import com.example.yin.service.impl.ConsumerServiceImpl;
import org.apache.commons.lang3.ObjectUtils.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ConsumerController {

    @Autowired
    private ConsumerServiceImpl consumerService;

    @Autowired
    private ManagementClient managementClient;

    @Autowired
    private AuthenticationClient authenticationClient;

    @Value("${authing.config.appId}")
    String AUTHING_APP_ID;

    @Configuration
    public static class MyPicConfig implements WebMvcConfigurer {
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/img/avatorImages/**")
                    .addResourceLocations(Constants.AVATOR_IMAGES_PATH);
        }
    }

    /**
     * 用户注册
     */
    @ResponseBody
    @RequestMapping(value = "/user/add", method = RequestMethod.POST)
    public Object addUser(HttpServletRequest req) {
        String username = req.getParameter("username").trim();
        String password = req.getParameter("password").trim();
        String sex = req.getParameter("sex").trim();
        String birth = req.getParameter("birth").trim();
        String introduction = req.getParameter("introduction").trim();
        String location = req.getParameter("location").trim();
        String avator = "/img/avatorImages/user.jpg";

        IsUserExistsReqDto isUserExistsReqDto = new IsUserExistsReqDto();
        isUserExistsReqDto.setUsername(username);
        // 调用 authing 接口 (判断用户名是否被注册)
        if (managementClient.isUserExists(isUserExistsReqDto).getData().getExists()) {
            return new WarningMessage("用户名已注册").getMessage();
        }

        SignUpDto signUpDto = new SignUpDto();
        signUpDto.setConnection(SignUpDto.Connection.PASSWORD);
        SignUpByPasswordDto passwordDto = new SignUpByPasswordDto();
        passwordDto.setUsername(username);
        passwordDto.setPassword(password);
        signUpDto.setPasswordPayload(passwordDto);

        SignUpProfileDto profileDto = new SignUpProfileDto();
        if ("0".equals(sex)) {
            profileDto.setGender(SignUpProfileDto.Gender.F);
        } else if ("1".equals(sex)) {
            profileDto.setGender(SignUpProfileDto.Gender.M);
        } else {
            profileDto.setGender(SignUpProfileDto.Gender.U);
        }
        profileDto.setBirthdate(birth);
        profileDto.setAddress(location);
        profileDto.setPhoto(avator);
        profileDto.setProfile(introduction);
        signUpDto.setProfile(profileDto);

        // 调用 authing 接口 (注册用户)
        UserSingleRespDto userSingleRespDto = authenticationClient.signUp(signUpDto);
        if (userSingleRespDto.getStatusCode() == 200) {
            AssignRoleDto assignRoleDto = new AssignRoleDto();
            assignRoleDto.setNamespace(AUTHING_APP_ID);
            assignRoleDto.setCode(RoleCodeEnum.USER.getValue());
            List<TargetDto> list = new ArrayList<>();
            TargetDto targetDto = new TargetDto();
            targetDto.setTargetType(TargetDto.TargetType.USER);
            targetDto.setTargetIdentifier(userSingleRespDto.getData().getUserId());
            list.add(targetDto);
            assignRoleDto.setTargets(list);
            //  调用 authing 接口 (分配角色，默认为 user)
            IsSuccessRespDto isSuccessRespDto = managementClient.assignRole(assignRoleDto);
            if (!isSuccessRespDto.getData().getSuccess()) {
                return new SuccessMessage<Null>("分配角色失败").getMessage();
            }
            return new SuccessMessage<Null>("注册成功").getMessage();
        }
        return new ErrorMessage(userSingleRespDto.getMessage()).getMessage();
    }

    /**
     * 登录判断
     */
    @ResponseBody
    @RequestMapping(value = "/user/login/status", method = RequestMethod.POST)
    public Object loginStatus(HttpServletRequest req, HttpSession session) {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        //  调用 authing 接口 (通过用户名和密码登入)
        LoginTokenRespDto loginTokenRespDto = authenticationClient.signInByUsernamePassword(username, password, new SignInOptionsDto());
        ListUsersRequestDto listUsersRequestDto = new ListUsersRequestDto();
        List<ListUsersAdvancedFilterItemDto> list = new ArrayList<>();
        ListUsersAdvancedFilterItemDto userDto = new ListUsersAdvancedFilterItemDto();
        userDto.setField("username");
        userDto.setOperator(ListUsersAdvancedFilterItemDto.Operator.EQUAL);
        userDto.setValue(username);
        list.add(userDto);
        listUsersRequestDto.setAdvancedFilter(list);
        if (loginTokenRespDto.getStatusCode() == 200) {
            session.setAttribute("username", username);
            //  调用 authing 接口 (登入成功后需要返回所有用户列表)
            return new SuccessMessage<SignInRes>("登录成功",
                    new signInRes(loginTokenRespDto.getData().getAccessToken(),
                            convertConsumers(managementClient.listUsers(listUsersRequestDto).getData().getList()
                            ))).getMessage();
        } else {
            return new ErrorMessage(loginTokenRespDto.getMessage()).getMessage();
        }
    }

    /**
     * 返回所有用户
     */
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public Object allUser() {
        int page = 1, limit = 10;
        List<UserDto> userList = new ArrayList<>();
        ListUsersRequestDto listUsersRequestDto = new ListUsersRequestDto();
        ListUsersOptionsDto optionsDto = new ListUsersOptionsDto();
        PaginationDto paginationDto = new PaginationDto();
        paginationDto.setPage(page);
        paginationDto.setLimit(limit);
        optionsDto.setPagination(paginationDto);
        listUsersRequestDto.setOptions(optionsDto);
        //  调用 authing 接口 (获取所有用户列表)
        UserPaginatedRespDto users = managementClient.listUsers(listUsersRequestDto);
        userList.addAll(users.getData().getList());
        // 循环分页处理, 返回全量数据
        while (users.getData().getTotalCount() - userList.size() > 0) {
            paginationDto.setPage(++page);
            paginationDto.setLimit(limit);
            optionsDto.setPagination(paginationDto);
            listUsersRequestDto.setOptions(optionsDto);
            //  调用 authing 接口 (再次分页获取数据)
            users = managementClient.listUsers(listUsersRequestDto);
            userList.addAll(users.getData().getList());
        }
        List<Consumer> consumers = convertConsumers(userList);
        return new SuccessMessage<List<Consumer>>(null, consumers).getMessage();
    }

    /**
     * 将 Authing 返回的 dto 转为原接口的 dto
     * @param list
     * @return
     */
    private List<Consumer> convertConsumers(List<UserDto> list) {
        return list.stream().map(this::convertConsumer).collect(Collectors.toList());
    }

    private Consumer convertConsumer(UserDto item) {
        Consumer consumer = new Consumer();
        consumer.setId(item.getUserId());
        consumer.setUsername(item.getUsername());
        if (SignUpProfileDto.Gender.F.getValue().equals(item.getGender().getValue())) {
            consumer.setSex(new Byte("0"));
        } else if (SignUpProfileDto.Gender.M.getValue().equals(item.getGender().getValue())) {
            consumer.setSex(new Byte("1"));
        } else {
            consumer.setSex(new Byte("2"));
        }
        consumer.setIntroduction(item.getProfile());
        consumer.setLocation(item.getAddress());
        consumer.setAvator(item.getPhoto());

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date myBirth = new Date();
        try {
            if(item.getBirthdate()!=null){
                myBirth = dateFormat.parse(item.getBirthdate());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        consumer.setBirth(myBirth);

        //获取用户角色信息
        GetUserRolesDto getUserRolesDto = new GetUserRolesDto();
        getUserRolesDto.setNamespace(AUTHING_APP_ID);
        getUserRolesDto.setUserId(item.getUserId());
        getUserRolesDto.setNamespace(AUTHING_APP_ID);
        //  调用 authing 接口 (获取用户的角色列表并返回)
        RolePaginatedRespDto userRoles = managementClient.getUserRoles(getUserRolesDto);
        List<RoleDto> roles = userRoles.getData().getList();
        List<String> codes = roles.stream().map(RoleDto::getCode).filter(code -> !RoleCodeEnum.SUPER_ADMIN.getValue().equals(code)).collect(Collectors.toList());
        consumer.setRoleCodes(codes);
        return consumer;
    }

    /**
     * 返回指定 ID 的用户
     */
    @RequestMapping(value = "/user/detail", method = RequestMethod.GET)
    public Object userOfId(HttpServletRequest req) {
        String id = req.getParameter("id");
        GetUserDto getUserDto = new GetUserDto();
        getUserDto.setUserId(id);
        //  调用 authing 接口 (根据 userId 获取用户信息)
        UserSingleRespDto user = managementClient.getUser(getUserDto);
        return new SuccessMessage<List<Consumer>>(null, convertConsumers(Collections.singletonList(user.getData()))).getMessage();
    }

    /**
     * 管理员删除用户
     */
    @GetMapping("/user/delete")
    public Object deleteUserByAdmin(HttpServletRequest req){
        String id = req.getParameter("id");
        DeleteUsersBatchDto deleteDto = new DeleteUsersBatchDto();
        deleteDto.setUserIds(Collections.singletonList(id));
        //  调用 authing 接口 (根据 userId 删除用户)
        boolean res = managementClient.deleteUsersBatch(deleteDto).getData().getSuccess();
        if (res) {
            return new SuccessMessage<Null>("删除成功").getMessage();
        } else {
            return new ErrorMessage("删除失败").getMessage();
        }
    }

    /**
     * 用户自我注销账号
     */
    @RequestMapping(value = "/user/deleteSelf", method = RequestMethod.POST)
    public Object deleteUserBySelf(HttpServletRequest req,@CookieValue("userAccessToken") String accessToken) {
//        String id = req.getParameter("id");
        String password = req.getParameter("password");
        authenticationClient.setAccessToken(accessToken);

        VerifyDeleteAccountRequestDto verifyDeleteAccountRequestDto = new VerifyDeleteAccountRequestDto();
        verifyDeleteAccountRequestDto.setVerifyMethod(VerifyDeleteAccountRequestDto.VerifyMethod.PASSWORD);
        DeleteAccountByPasswordDto deleteAccountByPasswordDto = new DeleteAccountByPasswordDto();
        deleteAccountByPasswordDto.setPassword(password);
        verifyDeleteAccountRequestDto.setPasswordPayload(deleteAccountByPasswordDto);
        // 调用 authing 接口
        VerifyDeleteAccountRequestRespDto verifyDeleteAccountRequestRespDto = authenticationClient.verifyDeleteAccountRequest(verifyDeleteAccountRequestDto);
        if(verifyDeleteAccountRequestRespDto.getStatusCode() != 200){
            return new ErrorMessage(verifyDeleteAccountRequestRespDto.getMessage()).getMessage();
        }
        String deleteAccountToken = verifyDeleteAccountRequestRespDto.getData().getDeleteAccountToken();

        DeleteAccounDto deleteAccounDto = new DeleteAccounDto();
        deleteAccounDto.setDeleteAccountToken(deleteAccountToken);
        // 调用 authing 接口
        authenticationClient.deleteAccount(deleteAccounDto);

        return new SuccessMessage<>("删除成功").getMessage();
    }

    /**
     * 更新用户信息
     */
    @ResponseBody
    @RequestMapping(value = "/user/update", method = RequestMethod.POST)
    public Object updateUserMsg(HttpServletRequest req) {
        String id = req.getParameter("id").trim();
        String username = req.getParameter("username").trim();
        String sex = req.getParameter("sex").trim();
        String birth = req.getParameter("birth").trim();
        String introduction = req.getParameter("introduction").trim();
        String location = req.getParameter("location").trim();

        // 更新字段填充
        UpdateUserReqDto updateDto = new UpdateUserReqDto();
        updateDto.setUserId(id);
        updateDto.setUsername(username);
        updateDto.setBirthdate(birth);
        updateDto.setProfile(introduction);
        updateDto.setAddress(location);
        if ("0".equals(sex)) {
            updateDto.setGender(UpdateUserReqDto.Gender.F);
        } else if ("1".equals(sex)) {
            updateDto.setGender(UpdateUserReqDto.Gender.M);
        } else {
            updateDto.setGender(UpdateUserReqDto.Gender.U);
        }
        //  调用 authing 接口
        UserSingleRespDto updateUser = managementClient.updateUser(updateDto);
        if (updateUser.getStatusCode() == 200) {
            return new SuccessMessage<Null>("修改成功").getMessage();
        } else {
            return new ErrorMessage("修改失败").getMessage();
        }
    }

    /**
     * 更新用户密码
     */
    @ResponseBody
    @RequestMapping(value = "/user/updatePassword", method = RequestMethod.POST)
    public Object updatePassword(HttpServletRequest req,@CookieValue("userAccessToken") String accessToken) {
        // 配置 accessToken
        authenticationClient.setAccessToken(accessToken);
//        String id = req.getParameter("id").trim();
//        String username = req.getParameter("username").trim();
        String old_password = req.getParameter("old_password").trim();
        String password = req.getParameter("password").trim();

        UpdatePasswordDto updatePasswordDto = new UpdatePasswordDto();
        updatePasswordDto.setOldPassword(old_password);
        updatePasswordDto.setNewPassword(password);
        updatePasswordDto.setPasswordEncryptType(UpdatePasswordDto.PasswordEncryptType.NONE);
        CommonResponseDto commonResponseDto = authenticationClient.updatePassword(updatePasswordDto);
        if (commonResponseDto.getStatusCode() == 200) {
            return new SuccessMessage<Null>("密码修改成功").getMessage();
        } else {
            return new ErrorMessage(commonResponseDto.getMessage()).getMessage();
        }
    }

    /**
     * 更新用户头像
     */
    @ResponseBody
    @RequestMapping(value = "/user/avatar/update", method = RequestMethod.POST)
    public Object updateUserPic(@RequestParam("file") MultipartFile avatorFile, @RequestParam("id") int id) {
        String fileName = System.currentTimeMillis() + avatorFile.getOriginalFilename();
        String filePath = Constants.PROJECT_PATH + System.getProperty("file.separator") + "img" + System.getProperty("file.separator") + "avatorImages";
        File file1 = new File(filePath);
        if (!file1.exists()) {
            file1.mkdir();
        }

        File dest = new File(filePath + System.getProperty("file.separator") + fileName);
        String imgPath = "/img/avatorImages/" + fileName;
        try {
            avatorFile.transferTo(dest);
            Consumer consumer = new Consumer();
            consumer.setId(String.valueOf(id));
            consumer.setAvator(imgPath);
            boolean res = consumerService.updateUserAvator(consumer);
            if (res) {
                return new SuccessMessage<String>("上传成功", imgPath).getMessage();
            } else {
                return new ErrorMessage("上传失败").getMessage();
            }
        } catch (IOException e) {
            return new FatalMessage("上传失败" + e.getMessage()).getMessage();
        }
    }

    /**
     * 修改用户角色
     */
    @PostMapping("user/changeRole")
    public Object changeRole(@RequestBody UserRoleParam param){
        String userId = param.getUserId();
        List<String> codeList = param.getCodeList();
        // 检查是否删除了 user 角色
        if(!codeList.contains(RoleCodeEnum.USER.getValue())){
            return new ErrorMessage("不允许删除基本的 user 角色").getMessage();
        }
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
                return new ErrorMessage("修改角色失败").getMessage();
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
                return new ErrorMessage("修改角色失败").getMessage();
            }
        }
        return new SuccessMessage<>("修改成功").getMessage();
    }

    /**
     * 通过 accessToken 获取用户角色
     */
    @PostMapping("user/selectRoles")
    public Object selectRolesByAccessToken(@CookieValue("manageAccessToken") String accessToken){
        if(StrUtil.isBlank(accessToken)){
            return new ErrorMessage("accessToken 已失效，请重新登录").getMessage();
        }
        authenticationClient.setAccessToken(accessToken);
        // 获取用户信息
        UserSingleRespDto userSingleRespDto = authenticationClient.getProfile(new GetProfileDto());
        if(userSingleRespDto.getStatusCode() != 200){
            return new ErrorMessage("获取用户信息失败").getMessage();
        }
        String userId = userSingleRespDto.getData().getUserId();
        // 获取用户角色信息
        GetUserRolesDto getUserRolesDto = new GetUserRolesDto();
        getUserRolesDto.setNamespace(AUTHING_APP_ID);
        getUserRolesDto.setUserId(userId);
        getUserRolesDto.setNamespace(AUTHING_APP_ID);
        // 调用 authing 接口 (获取用户的角色列表并返回)
        RolePaginatedRespDto userRoles = managementClient.getUserRoles(getUserRolesDto);
        if(userRoles.getStatusCode() != 200){
            return new ErrorMessage("获取用户角色列表失败").getMessage();
        }
        List<RoleDto> roles = userRoles.getData().getList();
        List<String> codes = roles.stream().map(RoleDto::getCode).collect(Collectors.toList());
        return new SuccessMessage<List<String>>("获取用户角色成功",codes).getMessage();
    }

    /**
     * 登出
     */
    @PostMapping("user/logout")
    public Object logout(@CookieValue("userAccessToken") String accessToken){
        Boolean flag = authenticationClient.revokeToken(accessToken);
        if(flag){
            return new SuccessMessage<>("退出登录").getMessage();
        }else{
            return new ErrorMessage("退出登录出错").getMessage();
        }
    }

}
