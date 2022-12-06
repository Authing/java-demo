package com.example.yin2.controller;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.*;
import cn.authing.sdk.java.dto.authentication.BuildLogoutUrlParams;
import cn.authing.sdk.java.dto.authentication.UserInfo;
import cn.authing.sdk.java.model.AuthingRequestConfig;
import cn.authing.sdk.java.util.JsonUtils;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import com.alibaba.fastjson.JSONObject;
import com.example.yin2.Enum.RoleCodeEnum;
import com.example.yin2.common.FatalMessage;
import com.example.yin2.common.ErrorMessage;
import com.example.yin2.common.SuccessMessage;
import com.example.yin2.common.WarningMessage;
import com.example.yin2.constant.Constants;
import com.example.yin2.domain.*;
import com.example.yin2.service.impl.ConsumerServiceImpl;
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
import java.util.*;
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

    @Value("${authing.config.appSecret}")
    String AUTHING_APP_SECRET;

    @Value("${authing.config.redirectUri}")
    String AUTHING_REDIRECTURI;

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
    public Object loginStatus(HttpServletRequest req) {
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
            String accessToken = loginTokenRespDto.getData().getAccessToken();
            List<Consumer> consumers = convertConsumers(managementClient.listUsers(listUsersRequestDto).getData().getList());
            //  调用 authing 接口 (登入成功后需要返回所有用户列表)
            return new SuccessMessage<ConsumerSignIn>("登录成功",
                    new ConsumerSignIn(accessToken,consumers)).getMessage();
        } else {
            return new ErrorMessage(loginTokenRespDto.getMessage()).getMessage();
        }
    }

    /**
     * 返回所有用户
     */
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public Object allUser() {
        int page = 1, limit = 50;
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
        consumer.setOwnerId(item.getUserId());
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
        UserSingleRespDto userSingleRespDto = managementClient.getUser(getUserDto);
        if(userSingleRespDto.getStatusCode() == 200) {
            return new SuccessMessage<List<Consumer>>(null,
                    convertConsumers(Collections.singletonList(userSingleRespDto.getData()))).getMessage();
        }else {
            return new ErrorMessage("获取用户信息失败").getMessage();
        }
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
    public Object deleteUserBySelf(HttpServletRequest req,@CookieValue(value = "userAccessToken",required = false) String accessToken) {
//        String id = req.getParameter("id");
        if(StrUtil.isBlank(accessToken)) {
            return new ErrorMessage("accessToken失效，请重新登录").getMessage();
        }

        List<RoleDto> authingRoleList = this.getUserRolesByAccessToken(accessToken);
        for(RoleDto roleDto : authingRoleList){
            if(StrUtil.equals(roleDto.getCode(),RoleCodeEnum.ADMIN.getValue()) ||
                    StrUtil.equals(roleDto.getCode(),RoleCodeEnum.SUPER_ADMIN.getValue())){
                return new ErrorMessage("管理员不允许自我注销！").getMessage();
            }
        }

        authenticationClient.setAccessToken(accessToken);
        String password = req.getParameter("password");

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
    public Object updatePassword(HttpServletRequest req,@CookieValue(value = "userAccessToken",required = false) String accessToken) {
        if(StrUtil.isBlank(accessToken)) {
            return new ErrorMessage("accessToken失效，请重新登录").getMessage();
        }
        List<RoleDto> authingRoleList = this.getUserRolesByAccessToken(accessToken);
        for(RoleDto roleDto : authingRoleList){
            if(StrUtil.equals(roleDto.getCode(),RoleCodeEnum.ADMIN.getValue()) ||
                    StrUtil.equals(roleDto.getCode(),RoleCodeEnum.SUPER_ADMIN.getValue())){
                return new ErrorMessage("不允许修改管理员的密码！").getMessage();
            }
        }
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
    public Object changeRole(@RequestBody UserRoleParam param) {
        List<String> codeList = param.getCodeList();
        // 检查是否删除了 user 角色
        if (!codeList.contains(RoleCodeEnum.USER.getValue())) {
            return new ErrorMessage("不允许删除基本的 user 角色").getMessage();
        }
        if (consumerService.changeRole(param)){
            return new SuccessMessage<>("修改成功").getMessage();
        }else{
            return new ErrorMessage("修改角色失败").getMessage();
        }
    }

    /**
     * 登出
     */
    @PostMapping("user/logout")
    public Object logout(@CookieValue(value = "userAccessToken",required = false) String accessToken) throws Exception {
        if(StrUtil.isBlank(accessToken)) {
            return new ErrorMessage("accessToken失效，请重新登录").getMessage();
        }
        Boolean flag = authenticationClient.revokeToken(accessToken);
        if(flag){
            return new SuccessMessage<>("退出登录").getMessage();
        }else{
            return new ErrorMessage("撤销accessToken出错").getMessage();
        }
    }

    /**
     * 通过 accessToken 获取用户角色
     */
    public List<RoleDto> getUserRolesByAccessToken(String accessToken){
        GetMyRoleListDto reqDto = new GetMyRoleListDto();
        reqDto.setNamespace(AUTHING_APP_ID);
        authenticationClient.setAccessToken(accessToken);
        RoleListRespDto respDto = authenticationClient.getRoleList(reqDto);
        return respDto.getData();
    }

    /**
     * 用 Guard 返回的 token 换取 AccessToken
     */
    @PostMapping("user/getAccessTokenByToken")
    public Object getAccessTokenByToken(HttpServletRequest req){
        String token = req.getParameter("token").trim();
        AuthingRequestConfig config = new AuthingRequestConfig();
        config.setUrl("/oidc/token");
        config.setMethod("UrlencodedPOST");
        Map<String, String> headers = new HashMap();
        headers.put(Header.CONTENT_TYPE.getValue(), "application/x-www-form-urlencoded");
        config.setHeaders(headers);
        AccessTokenReq tokenReq = new AccessTokenReq();
        tokenReq.setToken(token);
        tokenReq.setClientId(AUTHING_APP_ID);
        tokenReq.setClientSecret(AUTHING_APP_SECRET);
        tokenReq.setGrantType("http://authing.cn/oidc/grant_type/authing_token");
        tokenReq.setScope("openid");
        tokenReq.setRedirectUri(AUTHING_REDIRECTURI);
        config.setBody(tokenReq);
        String response = authenticationClient.request(config);
        AccessTokenRes accessTokenRes = JsonUtils.deserialize(response, AccessTokenRes.class);
        return new SuccessMessage<String>("获取 accessToken 成功",accessTokenRes.getAccessToken()).getMessage();
    }

}
