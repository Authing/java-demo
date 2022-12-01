package org.javaboy.vhr.converter;

import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.*;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.poi.ss.formula.functions.Na;
import org.javaboy.vhr.Exception.AuthingException;
import org.javaboy.vhr.model.*;
import org.javaboy.vhr.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
@Component
public class AuthingUserEmployeeConverter {
    @Autowired
    EmployeeService employeeService;
    @Autowired
    ManagementClient managementClient;
    @Autowired
    NationService nationService;
    @Autowired
    PoliticsstatusService politicsstatusService;
    @Autowired
    JobLevelService jobLevelService;
    @Autowired
    PositionService positionService;
    @Autowired
    DepartmentService departmentService;

    // authing 用户转换成 employee
    public Employee AuthToEmp(UserDto userDto){
        Employee employee = new Employee();
        employee.setOwnerId(userDto.getUserId());
        if(StrUtil.isNotBlank(userDto.getExternalId())) {
            employee.setId(Integer.valueOf(userDto.getExternalId()));
        }
        String username = userDto.getUsername();
        // 如果是从数据库中迁移，含有 externalId，则从拼接字符串中拆分出原来的 name
        if(StrUtil.isNotBlank(userDto.getExternalId())
                && StrUtil.contains(userDto.getUsername(),userDto.getExternalId())){
            StrUtil.removeSuffix(userDto.getUsername(), userDto.getExternalId());
        }
        employee.setName(username);

        if(StrUtil.equals(userDto.getGender().getValue(),"M")){
            employee.setGender("男");
        } else if (StrUtil.equals(userDto.getGender().getValue(),"F")) {
            employee.setGender("女");
        }

        employee.setBirthday(DateUtil.parseDate(userDto.getBirthdate()));

        LinkedHashMap<String,Object> customData = (LinkedHashMap) userDto.getCustomData();
        if(customData.containsKey("idCard")) {
            employee.setIdCard((String) customData.get("idCard"));
        }
        if(customData.containsKey("wedlock")) {
            employee.setWedlock((String) customData.get("wedlock"));
        }
        if(customData.containsKey("nationId")) {
            employee.setNationId((Integer) customData.get("nationId"));
            employee.setNation(nationService.getNationById((Integer) customData.get("nationId")));
        }
        if(customData.containsKey("nativePlace")) {
            employee.setNativePlace((String) customData.get("nativePlace"));
        }
        if(customData.containsKey("politicId")) {
            employee.setPoliticId((Integer) customData.get("politicId"));
            employee.setPoliticsstatus(politicsstatusService.getPoliticsstatusById((Integer) customData.get("politicId")));
        }

        employee.setAddress(userDto.getAddress());

        // 获取用户部门
        Department department = departmentService.getAuthingDepByUser(userDto.getUserId());
        employee.setAuthingDepartmentId(department.getAuthingDepartmentId());
        employee.setDepartment(department);

        if(customData.containsKey("jobLevelId")) {
            employee.setJobLevelId((Integer) customData.get("jobLevelId"));
            employee.setJobLevel(jobLevelService.getJobLevelById((Integer) customData.get("jobLevelId")));
        }
        if(customData.containsKey("posId")) {
            employee.setPosId((Integer) customData.get("posId"));
            employee.setPosition(positionService.getPositionById((Integer) customData.get("posId")));
        }
        if(customData.containsKey("engageForm")) {
            employee.setEngageForm((String) customData.get("engageForm"));
        }
        if(customData.containsKey("tiptopDegree")) {
            employee.setTiptopDegree((String) customData.get("tiptopDegree"));
        }
        if(customData.containsKey("specialty")) {
            employee.setSpecialty((String) customData.get("specialty"));
        }
        if(customData.containsKey("school")) {
            employee.setSchool((String) customData.get("school"));
        }
        if(customData.containsKey("beginDate")) {
            employee.setBeginDate(DateUtil.parseDate((String) customData.get("beginDate")));
        }
        if(customData.containsKey("workState")) {
            employee.setWorkState((String) customData.get("workState"));
        }
        if(customData.containsKey("workID")) {
            employee.setWorkID((String) customData.get("workID"));
        }
        if(customData.containsKey("contractTerm")) {
            employee.setContractTerm(Double.valueOf(((String) customData.get("contractTerm"))));
        }
        if(customData.containsKey("conversionTime")) {
            employee.setConversionTime(DateUtil.parseDate((String) customData.get("conversionTime")));
        }
        if(customData.containsKey("notWorkDate")) {
            employee.setNotWorkDate(DateUtil.parseDate((String) customData.get("notWorkDate")));
        }
        if(customData.containsKey("beginContract")) {
            employee.setBeginContract(DateUtil.parseDate((String) customData.get("beginContract")));
        }
        if(customData.containsKey("endContract")) {
            employee.setEndContract(DateUtil.parseDate((String) customData.get("endContract")));
        }
        if(customData.containsKey("workAge")) {
            employee.setWorkAge((Integer) customData.get("workAge"));
        }

        return employee;
    }

    // employee 转换成 authing 用户
    public CreateUserInfoDto EmpToAuth(Employee employee){
        CreateUserInfoDto createUserInfoDto = new CreateUserInfoDto();
        if(employee.getId() != null) {
            createUserInfoDto.setExternalId(Integer.toString(employee.getId()));
        }
        // 由于原本数据库中很多重复的重名数据，而 username 将作为 authing 的唯一标识，此处拼接 username 为 username + id
        String[] arr = new String[2];
        arr[0] = employee.getName();
        arr[1] = "";
        if(employee.getId() != null && !StrUtil.contains(employee.getName(),Integer.toString(employee.getId()))) {
            arr[1] = Integer.toString(employee.getId());
        }
        createUserInfoDto.setUsername(StrUtil.concat(true,arr));

        if(StrUtil.equals(employee.getGender(),"男")) {
            createUserInfoDto.setGender(CreateUserInfoDto.Gender.M);
        }else if(StrUtil.equals(employee.getGender(),"女")){
            createUserInfoDto.setGender(CreateUserInfoDto.Gender.F);
        }else{
            createUserInfoDto.setGender(CreateUserInfoDto.Gender.U);
        }
        createUserInfoDto.setBirthdate(DateUtil.formatDate(employee.getBirthday()));
        // customData 存放 authing 不默认提供但是 employee 中包含的数据
        CustomData customData = new CustomData();
        customData.setIdCard(employee.getIdCard());
        customData.setWedlock(employee.getWedlock());
        customData.setNationId(employee.getNationId());
        customData.setNativePlace(employee.getNativePlace());
        customData.setPoliticId(employee.getPoliticId());

//        createUserInfoDto.setEmail(employee.getEmail());
//        createUserInfoDto.setPhone(employee.getPhone());

        createUserInfoDto.setAddress(employee.getAddress());

        List<String> departmentIds = new ArrayList<>();
        departmentIds.add(String.valueOf(employee.getAuthingDepartmentId()));
        createUserInfoDto.setDepartmentIds(departmentIds);

        customData.setJobLevelId(employee.getJobLevelId());
        customData.setPosId(employee.getPosId());
        customData.setEngageForm(employee.getEngageForm());
        customData.setTiptopDegree(employee.getTiptopDegree());
        customData.setSpecialty(employee.getSpecialty());
        customData.setSchool(employee.getSchool());
        customData.setBeginDate(employee.getBeginDate());
        customData.setWorkState(employee.getWorkState());
        customData.setWorkID(employee.getWorkID());
        // 类型转换
        customData.setContractTerm(Double.toString(employee.getContractTerm()));
        customData.setConversionTime(employee.getConversionTime());
        customData.setNotWorkDate(employee.getNotWorkDate());
        customData.setBeginContract(employee.getBeginContract());
        customData.setEndContract(employee.getEndContract());
        customData.setWorkAge(employee.getWorkAge());
        createUserInfoDto.setCustomData(customData);
        return createUserInfoDto;
    }

    // 把原本数据库中的 employee 全部转换成 authing 用户,需手动调用
    public void batchCreateAuthingUser(){
        List<Employee> list = employeeService.getAllEmployee();
        List<CreateUserInfoDto> authingUserList = new ArrayList<>();
        // 批量创建一次最多 50 个
        for(int i = 1;i <= list.size();i++){
            authingUserList.add(this.EmpToAuth(list.get(i-1)));
            if(i % 50 == 0){
                CreateUserBatchReqDto reqDto = new CreateUserBatchReqDto();
                reqDto.setList(authingUserList);
                UserListRespDto respDto = managementClient.createUsersBatch(reqDto);
                if(respDto.getStatusCode() == 200) {
                    authingUserList.clear();
                }else{
                    throw new AuthingException(respDto.getMessage());
                }
            }
        }
    }
}
