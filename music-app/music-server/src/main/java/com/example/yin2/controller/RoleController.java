package com.example.yin2.controller;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.*;
import com.example.yin2.Enum.RoleCodeEnum;
import com.example.yin2.common.ErrorMessage;
import com.example.yin2.common.SuccessMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
public class RoleController {

    @Autowired
    AuthenticationClient authenticationClient;

    @Autowired
    ManagementClient managementClient;

    @Value("${authing.config.appId}")
    String AUTHING_APP_ID;

    /**
     * 获取全部角色
     */
    @GetMapping("/role/getAll")
    public Object getAll(){
        ListRolesDto listRolesDto = new ListRolesDto();
        // 与 namespace 对应应用绑定
        listRolesDto.setNamespace(AUTHING_APP_ID);
        // 调用 authing 接口
        RolePaginatedRespDto rolePaginatedRespDto = managementClient.listRoles(listRolesDto);
        // 获取返回结果中的数据
        List<RoleDto> RoleList = rolePaginatedRespDto.getData().getList();
        // 200 状态码表示请求成功，其他表示失败，getMessage() 可以获取更详细的提示信息
        if(rolePaginatedRespDto.getStatusCode() == 200){
            return new SuccessMessage<List<RoleDto>>(null, RoleList).getMessage();
        }else {
            return new ErrorMessage(rolePaginatedRespDto.getMessage()).getMessage();
        }
    }

    /**
     * 获取除了 superAdmin 之外的所有角色
     */
    @GetMapping("/role/getAllRoleWithOutSuperAdmin")
    public Object getAllRoleWithOutSuperAdmin(){
        ListRolesDto listRolesDto = new ListRolesDto();
        // 与 namespace 对应应用绑定
        listRolesDto.setNamespace(AUTHING_APP_ID);
        // 调用 authing 接口
        RolePaginatedRespDto rolePaginatedRespDto = managementClient.listRoles(listRolesDto);
        // 获取返回结果中的数据
        List<RoleDto> roleDtoList = rolePaginatedRespDto.getData().getList();
        // 过滤 superAdmin
        List<RoleDto> resList = new ArrayList<>();
        for(RoleDto roleDto:roleDtoList){
            if(!roleDto.getCode().equals(RoleCodeEnum.SUPER_ADMIN.getValue())){
                resList.add(roleDto);
            }
        }
        // 200 状态码表示请求成功，其他表示失败，getMessage() 可以获取更详细的提示信息
        if(rolePaginatedRespDto.getStatusCode() == 200){
            return new SuccessMessage<List<RoleDto>>(null, resList).getMessage();
        }else {
            return new ErrorMessage(rolePaginatedRespDto.getMessage()).getMessage();
        }
    }

    /**
     * 添加角色
     */
    @PostMapping("role/add")
    public Object addRole(HttpServletRequest req){
        String code = req.getParameter("code").trim();
        String description = req.getParameter("description").trim();

        CreateRoleDto createRoleDto = new CreateRoleDto();
        createRoleDto.setCode(code);
        createRoleDto.setDescription(description);
        createRoleDto.setNamespace(AUTHING_APP_ID);
        // 调用 authing 接口
        RoleSingleRespDto roleSingleRespDto = managementClient.createRole(createRoleDto);
        if(roleSingleRespDto.getStatusCode() == 200){
            return new SuccessMessage<>("添加成功").getMessage();
        }else {
            return new ErrorMessage(roleSingleRespDto.getMessage()).getMessage();
        }
    }

    /**
     * 删除角色
     */
    @GetMapping("role/delete")
    public Object deleteRole(HttpServletRequest req){
        String code = req.getParameter("code").trim();

        DeleteRoleDto deleteRoleDto = new DeleteRoleDto();
        // 支持批量删除
        List<String> codeList = new ArrayList<>();
        codeList.add(code);
        deleteRoleDto.setCodeList(codeList);
        deleteRoleDto.setNamespace(AUTHING_APP_ID);
        // 调用 authing 接口
        IsSuccessRespDto isSuccessRespDto = managementClient.deleteRolesBatch(deleteRoleDto);
        if(isSuccessRespDto.getStatusCode() == 200){
            return new SuccessMessage<>("删除成功").getMessage();
        }else {
            return new ErrorMessage(isSuccessRespDto.getMessage()).getMessage();
        }
    }

    @PostMapping("/role/edit")
    public Object editRole(HttpServletRequest req){
        String code = req.getParameter("code").trim();
        String newCode = req.getParameter("newCode").trim();
        String description = req.getParameter("description").trim();

        UpdateRoleDto updateRoleDto = new UpdateRoleDto();
        updateRoleDto.setCode(code);
        updateRoleDto.setNewCode(newCode);
        updateRoleDto.setDescription(description);
        updateRoleDto.setNamespace(AUTHING_APP_ID);
        IsSuccessRespDto isSuccessRespDto = managementClient.updateRole(updateRoleDto);
        if(isSuccessRespDto.getStatusCode() == 200){
            return new SuccessMessage<>("修改成功").getMessage();
        }else{
            return new ErrorMessage("修改失败").getMessage();
        }
    }

}
