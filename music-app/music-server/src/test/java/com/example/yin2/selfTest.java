package com.example.yin2;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.*;
import cn.authing.sdk.java.dto.authentication.*;
import cn.authing.sdk.java.enums.ProtocolEnum;
import cn.authing.sdk.java.model.AuthenticationClientOptions;
import cn.authing.sdk.java.model.ClientCredentialInput;
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
    public void testAuthenticationClient() throws IOException, ParseException {
        AuthenticationClientOptions options = new AuthenticationClientOptions();
        options.setAppId("634521175dc75ced7033ccdb");
        options.setAppHost("https://kuluqi3.authing.cn");
        options.setAppSecret("1231231231");
        AuthenticationClient authenticationClient = new AuthenticationClient(options);
    }
    @Test
    public void testManagementClient(){
        ManagementClientOptions options = new ManagementClientOptions();
        options.setAccessKeyId("6361debca15e75f9205677b2");
        options.setAccessKeySecret("4ef5e0b05e48c6e078ea2f49865abd0b");
        options.setHost("https://kuluqi3.authing.cn");
        ManagementClient client = new ManagementClient(options);

    }

    @Test
    public void organStrTree() throws InterruptedException {
        ManagementClientOptions options = new ManagementClientOptions();
        options.setAccessKeyId("63450a7313f9b2bb4c5761a3");
        options.setAccessKeySecret("645141dd660fd26bd1d3ecf7f09cd34a");
//        options.setHost("https://kuluqi3.authing.cn");
        ManagementClient client = new ManagementClient(options);
        // 1\获取架构列表
        //获取组织架构信息
        OrganizationPaginatedRespDto response = client.listOrganizations(new ListOrganizationsDto());
        List<OrganizationDto> list = response.getData().getList();
        System.out.println(list);
        for (OrganizationDto organizationDto : list) {
            String departmentId = organizationDto.getDepartmentId();
            String organizationName = organizationDto.getOrganizationName();
            String organizationCode = organizationDto.getOrganizationCode();
            Boolean hasChildren = organizationDto.getHasChildren(); //是否有子节点
            //获取当前部门成员信息
            UserPaginatedRespDto listDepartmentMembersDto = getListDepartmentMembersDto(client,departmentId, organizationCode, 1, organizationName);
        }
    }

    @Test
    public UserPaginatedRespDto getListDepartmentMembersDto(ManagementClient client,String departmentId, String organizationCode, int pageIndex, String deptName) throws InterruptedException {

        ListDepartmentMembersDto reqDto = new ListDepartmentMembersDto();

        reqDto.setDepartmentId(departmentId);
        reqDto.setOrganizationCode(organizationCode);
        reqDto.setWithDepartmentIds(true);
        reqDto.setWithIdentities(true);
        reqDto.setWithCustomData(true);
        reqDto.setLimit(50);//每页返回50条数据
        reqDto.setPage(pageIndex);
        UserPaginatedRespDto response = client.listDepartmentMembers(reqDto);
        System.out.println(JsonUtils.serialize(response));
        //获取成员总数
        Integer deptUserCount = response.getData().getTotalCount();

        return response;
    }

}
