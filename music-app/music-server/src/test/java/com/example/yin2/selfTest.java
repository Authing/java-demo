package com.example.yin2;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.*;
import cn.authing.sdk.java.dto.authentication.IOidcParams;
import cn.authing.sdk.java.dto.authentication.OIDCTokenResponse;
import cn.authing.sdk.java.model.AuthenticationClientOptions;
import cn.authing.sdk.java.model.ManagementClientOptions;
import cn.authing.sdk.java.util.JsonUtils;
import com.example.yin2.domain.CustomData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class selfTest {

    @Autowired
    ManagementClient managementClient;

    @Autowired
    AuthenticationClient authenticationClient;

    @Test
    public void testCreateOrganization(){
        CreateOrganizationReqDto createOrganizationReqDto = new CreateOrganizationReqDto();
        createOrganizationReqDto.setOrganizationName("name1");
        createOrganizationReqDto.setOrganizationCode("org1");
        createOrganizationReqDto.setDescription("org1");
        createOrganizationReqDto.setOpenDepartmentId("dep1");
        createOrganizationReqDto.setI18n(new OrganizationNameI18nDto());

        OrganizationSingleRespDto organization = managementClient.createOrganization(createOrganizationReqDto);
        System.out.println(organization);
    }

    @Test
    public void testSignUpByUsernamePassword() throws IOException, ParseException {
        AuthenticationClientOptions options = new AuthenticationClientOptions();
        options.setAppId("634521175dc75ced7033ccdb");
        options.setAppHost("https://kuluqi3.authing.cn");
        options.setAppSecret("1231231231");
        AuthenticationClient authenticationClient = new AuthenticationClient(options);
        authenticationClient.signUpByUsernamePassword("test","test",new SignUpProfileDto(),new SignUpOptionsDto());
    }

    @Test
    public void testGetCustomData() throws IOException, ParseException {
        AuthenticationClientOptions options = new AuthenticationClientOptions();
        options.setAppId("634521175dc75ced7033ccdb");
        options.setAppHost("https://kuluqi3.authing.cn");
        options.setAppSecret("1231231231");
        AuthenticationClient authenticationClient = new AuthenticationClient(options);

//        UpdateUserProfileDto updateUserProfileDto = new UpdateUserProfileDto();
//        CustomData customData = new CustomData();
//        List<String> list = new ArrayList<>();
//        list.add("YOUR_SCHOOL");
//        customData.setSchool(list);
////        updateUserProfileDto.set
//        updateUserProfileDto.setCustomData(customData);
//        authenticationClient.updateProfile(updateUserProfileDto);

        GetProfileDto getProfileDto = new GetProfileDto();
//        getProfileDto.setWithCustomData(true);
        UserSingleRespDto userSingleRespDto = authenticationClient.getProfile(getProfileDto);
//        Object res = userSingleRespDto.getData().getCustomData();
        System.out.println(JsonUtils.serialize(userSingleRespDto));
    }


    @Test
    public void testUpdateUser(){
        ManagementClientOptions options = new ManagementClientOptions();
        options.setAccessKeyId("6361debca15e75f9205677b2");
        options.setAccessKeySecret("4ef5e0b05e48c6e078ea2f49865abd0b");
        options.setHost("https://kuluqi1.authing.localhost:3000");
        ManagementClient client = new ManagementClient(options);

        UpdateUserReqDto updateUserReqDto = new UpdateUserReqDto();
        updateUserReqDto.setUserId("6361decbda41328a428e4a3f");
        CustomData customData = new CustomData();
        List<String> list = new ArrayList<>();
        list.add("MY_SCHOOL");
        list.add("YOUR_SCHOOL");
        customData.setSchool(list);
        updateUserReqDto.setCustomData(customData);
        UserSingleRespDto userSingleRespDto = client.updateUser(updateUserReqDto);

        GetUserDto getUserDto = new GetUserDto();
        getUserDto.setUserId("635b8a1da683dd4c67896252");
        getUserDto.setWithCustomData(true);
        UserSingleRespDto user = client.getUser(getUserDto);
        System.out.println(JsonUtils.serialize(user));
    }

    @Test
    public void testBuildAuthorizeUrl() throws IOException, ParseException {
        AuthenticationClientOptions options = new AuthenticationClientOptions();
        options.setAppId("634521175dc75ced7033ccdb");
        options.setAppHost("https://kuluqi3.authing.cn");
        options.setRedirectUri("https://console.authing.cn/console/get-started/634521175dc75ced7033ccdb");
        AuthenticationClient authenticationClient = new AuthenticationClient(options);

        IOidcParams iOidcParams = new IOidcParams();
        String url = authenticationClient.buildAuthorizeUrl(iOidcParams);
        System.out.println(url);
    }

    @Test
    public void testInit(){
        AuthenticationClientOptions options = new AuthenticationClientOptions();
        options.setAppId("AUTHING_APP_ID");
        options.setAppHost("AUTHING_APP_HOST");
        AuthenticationClient authenticationClient = null;
        try {
            authenticationClient = new AuthenticationClient(options);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        LoginTokenRespDto loginTokenRespDto =
                authenticationClient.signInByEmailPassword("email", "password", new SignInOptionsDto());
        UpdateUserProfileDto updateUserProfileDto = new UpdateUserProfileDto();
        updateUserProfileDto.setNickname("example");
        UserSingleRespDto userSingleRespDto = authenticationClient.updateProfile(updateUserProfileDto);


        ManagementClientOptions managementClientOptions = new ManagementClientOptions();
        managementClientOptions.setAccessKeyId("AUTHING_USERPOOL_ID");
        managementClientOptions.setAccessKeySecret("AUTHING_USERPOOL_SECRET");
        ManagementClient managementClient = new ManagementClient(managementClientOptions);
    }



    @Test
    public void testGetUser(){
        ManagementClientOptions managementClientOptions = new ManagementClientOptions();
        managementClientOptions.setAccessKeyId("63316ef7d5988e741b215494");
        managementClientOptions.setAccessKeySecret("ff23e837609f78d217f8591c3449bbf2");
        ManagementClient managementClient = new ManagementClient(managementClientOptions);
        GetUserDto getUserDto = new GetUserDto();
        getUserDto.setUserId("6344e857ce5304d6b530f14d");
        UserSingleRespDto user = managementClient.getUser(getUserDto);
        System.out.println(JsonUtils.serialize(user));
    }

    @Test
    public void testCreateOrganization1(){
        ManagementClientOptions options = new ManagementClientOptions();
        options.setAccessKeyId("63316ef7d5988e741b215494");
        options.setAccessKeySecret("ff23e837609f78d217f8591c3449bbf2");
        options.setHost("https://kuluqi2.authing.cn");
        ManagementClient client = new ManagementClient(options);
        CreateOrganizationReqDto reqDto = new CreateOrganizationReqDto();
        reqDto.setOrganizationName("蒸汽记忆");
        reqDto.setOrganizationCode("steamory");
        reqDto.setOpenDepartmentId("123");
        OrganizationSingleRespDto response = client.createOrganization(reqDto);
        System.out.println(JsonUtils.serialize(response));
    }

    @Test
    public void testCreateDepartment(){
        ManagementClientOptions options = new ManagementClientOptions();
        options.setAccessKeyId("63316ef7d5988e741b215494");
        options.setAccessKeySecret("ff23e837609f78d217f8591c3449bbf2");
        options.setHost("https://kuluqi2.authing.cn");
        ManagementClient client = new ManagementClient(options);

        GetOrganizationDto getOrganizationDto = new GetOrganizationDto();
        getOrganizationDto.setOrganizationCode("steamory");
        OrganizationSingleRespDto organization = client.getOrganization(getOrganizationDto);
        System.out.println(JsonUtils.serialize(organization));

        CreateDepartmentReqDto createDepartmentReqDto = new CreateDepartmentReqDto();
        createDepartmentReqDto.setParentDepartmentId("63623ce1b4f88ef934f207ac");
        createDepartmentReqDto.setName("开发部");
        createDepartmentReqDto.setOrganizationCode("steamory");
        DepartmentSingleRespDto department = client.createDepartment(createDepartmentReqDto);
    }

    @Test
    public void testUpdateDep(){
        ManagementClientOptions options = new ManagementClientOptions();
        options.setAccessKeyId("63316ef7d5988e741b215494");
        options.setAccessKeySecret("ff23e837609f78d217f8591c3449bbf2");
        options.setHost("https://kuluqi2.authing.cn");
        ManagementClient client = new ManagementClient(options);

        UpdateDepartmentReqDto updateDepartmentReqDto = new UpdateDepartmentReqDto();
        updateDepartmentReqDto.setOrganizationCode("steamory");
        updateDepartmentReqDto.setDepartmentId("AUTHING_DEP_ID");
        updateDepartmentReqDto.setName("产品部");
        updateDepartmentReqDto.setParentDepartmentId("AUTHING_DEP_ID");
        DepartmentSingleRespDto departmentSingleRespDto = client.updateDepartment(updateDepartmentReqDto);
    }
    
    @Test
    public void testDelDep(){
        ManagementClientOptions options = new ManagementClientOptions();
        options.setAccessKeyId("63316ef7d5988e741b215494");
        options.setAccessKeySecret("ff23e837609f78d217f8591c3449bbf2");
        options.setHost("https://kuluqi2.authing.cn");
        ManagementClient client = new ManagementClient(options);

        DeleteDepartmentReqDto deleteDepartmentReqDto = new DeleteDepartmentReqDto();
        deleteDepartmentReqDto.setDepartmentId("AUTHING_DEP_ID");
        IsSuccessRespDto isSuccessRespDto = client.deleteDepartment(deleteDepartmentReqDto);
    }

    @Test
    public void testListChildDep(){
        ManagementClientOptions options = new ManagementClientOptions();
        options.setAccessKeyId("63316ef7d5988e741b215494");
        options.setAccessKeySecret("ff23e837609f78d217f8591c3449bbf2");
        options.setHost("https://kuluqi2.authing.cn");
        ManagementClient client = new ManagementClient(options);

        ListChildrenDepartmentsDto listChildrenDepartmentsDto = new ListChildrenDepartmentsDto();
        listChildrenDepartmentsDto.setDepartmentId("63623ce1b4f88ef934f207ac");
        listChildrenDepartmentsDto.setOrganizationCode("steamory");
        DepartmentPaginatedRespDto departmentPaginatedRespDto =
                client.listChildrenDepartments(listChildrenDepartmentsDto);
        System.out.println(JsonUtils.serialize(departmentPaginatedRespDto));
    }

    @Test
    public void testAddMebToDep(){
        ManagementClientOptions options = new ManagementClientOptions();
        options.setAccessKeyId("63316ef7d5988e741b215494");
        options.setAccessKeySecret("ff23e837609f78d217f8591c3449bbf2");
        options.setHost("https://kuluqi2.authing.cn");
        ManagementClient client = new ManagementClient(options);

        AddDepartmentMembersReqDto addDepartmentMembersReqDto = new AddDepartmentMembersReqDto();
        addDepartmentMembersReqDto.setOrganizationCode("steamory");
        addDepartmentMembersReqDto.setDepartmentId("63632977ee96fb966b787f0b");
        List<String> userIdList = new ArrayList<>();
        userIdList.add("6344e857ce5304d6b530f14d");
        addDepartmentMembersReqDto.setUserIds(userIdList);
        IsSuccessRespDto isSuccessRespDto = client.addDepartmentMembers(addDepartmentMembersReqDto);
    }

    @Test
    public void testDelMebToDep(){
        ManagementClientOptions options = new ManagementClientOptions();
        options.setAccessKeyId("63316ef7d5988e741b215494");
        options.setAccessKeySecret("ff23e837609f78d217f8591c3449bbf2");
        options.setHost("https://kuluqi2.authing.cn");
        ManagementClient client = new ManagementClient(options);

        RemoveDepartmentMembersReqDto removeDepartmentMembersReqDto = new RemoveDepartmentMembersReqDto();
        removeDepartmentMembersReqDto.setOrganizationCode("steamory");
        removeDepartmentMembersReqDto.setDepartmentId("63632977ee96fb966b787f0b");
        List<String> userIdList = new ArrayList<>();
        userIdList.add("6344e857ce5304d6b530f14d");
        removeDepartmentMembersReqDto.setUserIds(userIdList);
        IsSuccessRespDto isSuccessRespDto = client.removeDepartmentMembers(removeDepartmentMembersReqDto);
    }

    @Test
    public void testListDepMeb(){
        ManagementClientOptions options = new ManagementClientOptions();
        options.setAccessKeyId("63316ef7d5988e741b215494");
        options.setAccessKeySecret("ff23e837609f78d217f8591c3449bbf2");
        options.setHost("https://kuluqi2.authing.cn");
        ManagementClient client = new ManagementClient(options);

        ListDepartmentMembersDto listDepartmentMembersDto = new ListDepartmentMembersDto();
        listDepartmentMembersDto.setOrganizationCode("steamory");
        listDepartmentMembersDto.setDepartmentId("63632977ee96fb966b787f0b");
        listDepartmentMembersDto.setSortBy("JoinDepartmentAt");
        listDepartmentMembersDto.setOrderBy("Asc");
        UserPaginatedRespDto userPaginatedRespDto = client.listDepartmentMembers(listDepartmentMembersDto);
    }


}
