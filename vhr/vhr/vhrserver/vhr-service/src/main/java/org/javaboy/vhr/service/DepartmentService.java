package org.javaboy.vhr.service;

import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.*;
import org.javaboy.vhr.converter.AuthingDepToDepConverter;
import org.javaboy.vhr.mapper.DepartmentMapper;
import org.javaboy.vhr.model.Department;
import org.javaboy.vhr.model.RespBean;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @作者 江南一点雨
 * @公众号 江南一点雨
 * @微信号 a_java_boy
 * @GitHub https://github.com/lenve
 * @博客 http://wangsong.blog.csdn.net
 * @网站 http://www.javaboy.org
 * @时间 2019-10-21 8:04
 */
@Service
public class DepartmentService {
    @Autowired
    DepartmentMapper departmentMapper;
    @Autowired
    ManagementClient managementClient;
    @Autowired
    AuthingDepToDepConverter authingDepToDepConverter;
    public List<Department> getAllDepartments() {
        return departmentMapper.getAllDepartmentsByParentId(-1);
    }

    public List<Department> getAuthingDepartments(){
        // 获取根组织
        GetOrganizationDto getOrganizationDto = new GetOrganizationDto();
        getOrganizationDto.setOrganizationCode("vhr");
        OrganizationDto organizationDto = managementClient.getOrganization(getOrganizationDto).getData();
        DepartmentDto departmentDto = new DepartmentDto();

        BeanUtils.copyProperties(organizationDto,departmentDto);
        return listChildren(departmentDto);
    }

    // 获取子部门并作类型转换
    public List<Department> listChildren(DepartmentDto departmentDto){
        if(!departmentDto.getHasChildren()){
            return null;
        }
        ListChildrenDepartmentsDto reqDto = new ListChildrenDepartmentsDto();
        reqDto.setDepartmentId(departmentDto.getDepartmentId());
        reqDto.setOrganizationCode(departmentDto.getOrganizationCode());
        DepartmentPaginatedRespDto respDto = managementClient.listChildrenDepartments(reqDto);
        List<DepartmentDto> list = respDto.getData().getList();
        List<Department> res = new ArrayList<>();
        list.forEach(child -> {
            Department department = authingDepToDepConverter.convert(child);
            // 手动设置父部门 id
            department.setParentAuthingId(departmentDto.getDepartmentId());
            if(child.getHasChildren()) {
                department.setChildren(listChildren(child));
            }
            res.add(department);
        });
        return res;
    }



    public void addDep(Department dep) {
        dep.setEnabled(true);
        departmentMapper.addDep(dep);
    }

    public RespBean addAuthingDep(Department dep){
        CreateDepartmentReqDto reqDto = new CreateDepartmentReqDto();
        reqDto.setOrganizationCode("vhr");
        reqDto.setName(dep.getName());
        reqDto.setParentDepartmentId(dep.getParentAuthingId());
        DepartmentSingleRespDto respDto = managementClient.createDepartment(reqDto);
        dep.setAuthingDepartmentId(respDto.getData().getDepartmentId());
        return respDto.getStatusCode() == 200 ?
                RespBean.ok("添加成功！",dep) : RespBean.error(respDto.getMessage());
    }

    public void deleteDepById(Department dep) {
        departmentMapper.deleteDepById(dep);
    }

    public RespBean deleteDepByAuthingDepId(String authingDepId) {
        DeleteDepartmentReqDto reqDto = new DeleteDepartmentReqDto();
        reqDto.setOrganizationCode("vhr");
        reqDto.setDepartmentId(authingDepId);
        IsSuccessRespDto respDto = managementClient.deleteDepartment(reqDto);
        return respDto.getStatusCode() == 200 ?
                RespBean.ok("删除成功！") : RespBean.error(respDto.getMessage());
    }

    public List<Department> getAllDepartmentsWithOutChildren() {
        return departmentMapper.getAllDepartmentsWithOutChildren();
    }

    public Department getAuthingDepByUser(String authingUserId){
        GetUserDepartmentsDto reqDto = new GetUserDepartmentsDto();
        reqDto.setUserId(authingUserId);
        UserDepartmentPaginatedRespDto respDto = managementClient.getUserDepartments(reqDto);
        List<UserDepartmentRespDto> departments = respDto.getData().getList();
        Department department = new Department();
        if(departments.size() != 0){
            // vhr 职工只属于一个部门
            UserDepartmentRespDto departmentDto = departments.get(0);
            String departmentId = departmentDto.getDepartmentId();
            String departmentName = departmentDto.getName();
            department.setAuthingDepartmentId(departmentId);
            department.setName(departmentName);
        }
        return department;
    }

}
