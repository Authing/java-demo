package org.javaboy.vhr.service;

import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.*;
import cn.hutool.core.util.StrUtil;
import org.javaboy.vhr.Exception.AuthingException;
import org.javaboy.vhr.converter.AuthingUserEmployeeConverter;
import org.javaboy.vhr.mapper.EmployeeMapper;
import org.javaboy.vhr.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @作者 江南一点雨
 * @公众号 江南一点雨
 * @微信号 a_java_boy
 * @GitHub https://github.com/lenve
 * @博客 http://wangsong.blog.csdn.net
 * @网站 http://www.javaboy.org
 * @时间 2019-10-29 7:44
 */
@Service
public class EmployeeService {
    @Autowired
    EmployeeMapper employeeMapper;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    MailSendLogService mailSendLogService;
    public final static Logger logger = LoggerFactory.getLogger(EmployeeService.class);
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
    SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
    DecimalFormat decimalFormat = new DecimalFormat("##.00");
    @Autowired
    ManagementClient managementClient;
    @Autowired
    AuthingUserEmployeeConverter authingUserEmployeeConverter;

    public List<Employee> getAllEmployee(){
        return employeeMapper.getAllEmployee();
    }

    // 从 authing 搜索普通用户并转换类型
    public RespPageBean getAuthingEmployeeByPage(Integer page, Integer limit, Employee employee, Date[] beginDateScope){
        ListUsersRequestDto reqDto = new ListUsersRequestDto();
        if(StrUtil.isNotBlank(employee.getName())) {
            reqDto.setKeywords(employee.getName());
        }
        ListUsersOptionsDto optionsDto = new ListUsersOptionsDto();
        List<String> searchList = new ArrayList<>();
        // 设置模糊搜索的字段
        searchList.add("username");
        optionsDto.setFuzzySearchOn(searchList);
        // 获取用户自定义字段
        optionsDto.setWithCustomData(true);
        PaginationDto paginationDto = new PaginationDto();
        paginationDto.setPage(page);
        paginationDto.setLimit(limit);
        optionsDto.setPagination(paginationDto);
        reqDto.setOptions(optionsDto);

        // 只选出普通用户（externalId 不为空）
        List<ListUsersAdvancedFilterItemDto> advancedFilterList = new ArrayList<>();
        ListUsersAdvancedFilterItemDto itemDto = new ListUsersAdvancedFilterItemDto();
        itemDto.setField("externalId");
        itemDto.setOperator(ListUsersAdvancedFilterItemDto.Operator.NOT_NULL);
        advancedFilterList.add(itemDto);

        // 根据其他条件高级搜索
        if(employee.getPoliticId() != null){
            ListUsersAdvancedFilterItemDto politicIdDto = new ListUsersAdvancedFilterItemDto();
            politicIdDto.setField("politicId");
            politicIdDto.setOperator(ListUsersAdvancedFilterItemDto.Operator.EQUAL);
            politicIdDto.setValue(employee.getPoliticId());
            advancedFilterList.add(politicIdDto);
        }
        if(employee.getNationId() != null){
            ListUsersAdvancedFilterItemDto nationIdDto = new ListUsersAdvancedFilterItemDto();
            nationIdDto.setField("nationId");
            nationIdDto.setOperator(ListUsersAdvancedFilterItemDto.Operator.EQUAL);
            nationIdDto.setValue(employee.getNationId());
            advancedFilterList.add(nationIdDto);
        }
        if(employee.getJobLevelId() != null){
            ListUsersAdvancedFilterItemDto jobLevelIdDto = new ListUsersAdvancedFilterItemDto();
            jobLevelIdDto.setField("jobLevelId");
            jobLevelIdDto.setOperator(ListUsersAdvancedFilterItemDto.Operator.EQUAL);
            jobLevelIdDto.setValue(employee.getJobLevelId());
            advancedFilterList.add(jobLevelIdDto);
        }
        if(employee.getPosId() != null){
            ListUsersAdvancedFilterItemDto posIdDto = new ListUsersAdvancedFilterItemDto();
            posIdDto.setField("posId");
            posIdDto.setOperator(ListUsersAdvancedFilterItemDto.Operator.EQUAL);
            posIdDto.setValue(employee.getPosId());
            advancedFilterList.add(posIdDto);
        }
        if(StrUtil.isNotBlank(employee.getEngageForm())){
            ListUsersAdvancedFilterItemDto engageFormDto = new ListUsersAdvancedFilterItemDto();
            engageFormDto.setField("engageForm");
            engageFormDto.setOperator(ListUsersAdvancedFilterItemDto.Operator.EQUAL);
            engageFormDto.setValue(employee.getEngageForm());
            advancedFilterList.add(engageFormDto);
        }
        // TODO 根据部门 id 高级过滤搜索
//        if(employee.getAuthingDepartmentId() != null){
//            ListUsersAdvancedFilterItemDto departmentIdDto = new ListUsersAdvancedFilterItemDto();
//            departmentIdDto.setField("department");
//            departmentIdDto.setOperator(ListUsersAdvancedFilterItemDto.Operator.IN);
//            DepAdvSearchDto searchDto = new DepAdvSearchDto();
//            searchDto.setOrganizationCode("vhr");
//            searchDto.setDepartmentId(employee.getAuthingDepartmentId());
//            searchDto.setDepartmentIdType("department_id");
//            searchDto.setIncludeChildrenDepartments(true);
//            departmentIdDto.setValue(searchDto);
//            advancedFilterList.add(departmentIdDto);
//        }
        if(beginDateScope != null){
            ListUsersAdvancedFilterItemDto beginDateDto = new ListUsersAdvancedFilterItemDto();
            beginDateDto.setField("beginDate");
            beginDateDto.setOperator(ListUsersAdvancedFilterItemDto.Operator.BETWEEN);
            beginDateDto.setValue(beginDateScope);
            advancedFilterList.add(beginDateDto);
        }

        reqDto.setAdvancedFilter(advancedFilterList);
        UserPaginatedRespDto respDto = managementClient.listUsers(reqDto);

        List<UserDto> userDtoList = respDto.getData().getList();
        List<Employee> employeeList = new ArrayList<>();

        if(userDtoList.size() != 0){
            userDtoList.forEach(userDto -> {
                Employee emp = authingUserEmployeeConverter.AuthToEmp(userDto);
                employeeList.add(emp);
            });
        }

        RespPageBean bean = new RespPageBean();
        bean.setData(employeeList);
        bean.setTotal(new Long(respDto.getData().getTotalCount()));
        return bean;
    }

    public RespPageBean getEmployeeByPage(Integer page, Integer size, Employee employee, Date[] beginDateScope) {
        if (page != null && size != null) {
            page = (page - 1) * size;
        }
        List<Employee> data = employeeMapper.getEmployeeByPage(page, size, employee, beginDateScope);
        Long total = employeeMapper.getTotal(employee, beginDateScope);
        RespPageBean bean = new RespPageBean();
        bean.setData(data);
        bean.setTotal(total);
        return bean;
    }

    public Integer addEmp(Employee employee) {
        Date beginContract = employee.getBeginContract();
        Date endContract = employee.getEndContract();
        double month = (Double.parseDouble(yearFormat.format(endContract)) - Double.parseDouble(yearFormat.format(beginContract))) * 12 + (Double.parseDouble(monthFormat.format(endContract)) - Double.parseDouble(monthFormat.format(beginContract)));
        employee.setContractTerm(Double.parseDouble(decimalFormat.format(month / 12)));
        int result = employeeMapper.insertSelective(employee);
        if (result == 1) {
            Employee emp = employeeMapper.getEmployeeById(employee.getId());
            //生成消息的唯一id
            String msgId = UUID.randomUUID().toString();
            MailSendLog mailSendLog = new MailSendLog();
            mailSendLog.setMsgId(msgId);
            mailSendLog.setCreateTime(new Date());
            mailSendLog.setExchange(MailConstants.MAIL_EXCHANGE_NAME);
            mailSendLog.setRouteKey(MailConstants.MAIL_ROUTING_KEY_NAME);
            mailSendLog.setEmpId(emp.getId());
            mailSendLog.setTryTime(new Date(System.currentTimeMillis() + 1000 * 60 * MailConstants.MSG_TIMEOUT));
            mailSendLogService.insert(mailSendLog);
            rabbitTemplate.convertAndSend(MailConstants.MAIL_EXCHANGE_NAME, MailConstants.MAIL_ROUTING_KEY_NAME, emp, new CorrelationData(msgId));
        }
        return result;
    }

    public Integer maxWorkID() {
        return employeeMapper.maxWorkID();
    }

    public Integer deleteEmpByEid(Integer id) {
        return employeeMapper.deleteByPrimaryKey(id);
    }

    public Integer updateEmp(Employee employee) {
        return employeeMapper.updateByPrimaryKeySelective(employee);
    }

    public Integer addEmps(List<Employee> list) {
        return employeeMapper.addEmps(list);
    }

    public RespPageBean getEmployeeByPageWithSalary(Integer page, Integer size) {
        if (page != null && size != null) {
            page = (page - 1) * size;
        }
        List<Employee> list = employeeMapper.getEmployeeByPageWithSalary(page, size);
        RespPageBean respPageBean = new RespPageBean();
        respPageBean.setData(list);
        respPageBean.setTotal(employeeMapper.getTotal(null, null));
        return respPageBean;
    }

    public Integer updateEmployeeSalaryById(Integer eid, Integer sid) {
        return employeeMapper.updateEmployeeSalaryById(eid, sid);
    }

    public Employee getEmployeeById(Integer empId) {
        return employeeMapper.getEmployeeById(empId);
    }

    public RespBean addAuthEmp(Employee employee) {
        CreateUserInfoDto createUserInfoDto = authingUserEmployeeConverter.EmpToAuth(employee);
        CreateUserReqDto reqDto = new CreateUserReqDto();
        BeanUtils.copyProperties(createUserInfoDto,reqDto);
        if(StrUtil.equals(createUserInfoDto.getGender().getValue(),"M")){
            reqDto.setGender(CreateUserReqDto.Gender.M);
        } else if (StrUtil.equals(createUserInfoDto.getGender().getValue(),"F")) {
            reqDto.setGender(CreateUserReqDto.Gender.F);
        }else{
            reqDto.setGender(CreateUserReqDto.Gender.U);
        }
        UserSingleRespDto respDto = managementClient.createUser(reqDto);
        return respDto.getStatusCode() == 200 ? RespBean.ok("添加成功") : RespBean.error(respDto.getMessage());
    }

    public RespBean updateAuthEmp(Employee employee) {
        CreateUserInfoDto createUserInfoDto = authingUserEmployeeConverter.EmpToAuth(employee);
        UpdateUserReqDto reqDto = new UpdateUserReqDto();
        BeanUtils.copyProperties(createUserInfoDto,reqDto);

        reqDto.setUserId(employee.getOwnerId());

        if(StrUtil.equals(createUserInfoDto.getGender().getValue(),"M")){
            reqDto.setGender(UpdateUserReqDto.Gender.M);
        } else if (StrUtil.equals(createUserInfoDto.getGender().getValue(),"F")) {
            reqDto.setGender(UpdateUserReqDto.Gender.F);
        }else{
            reqDto.setGender(UpdateUserReqDto.Gender.U);
        }

        // 改变用户部门
        SetUserDepartmentsDto setUserDepartmentsDto = new SetUserDepartmentsDto();
        setUserDepartmentsDto.setUserId(employee.getOwnerId());
        List<SetUserDepartmentDto> list = new ArrayList<>();
        SetUserDepartmentDto setUserDepartmentDto = new SetUserDepartmentDto();
        setUserDepartmentDto.setDepartmentId(employee.getAuthingDepartmentId());
        list.add(setUserDepartmentDto);
        setUserDepartmentsDto.setDepartments(list);
        managementClient.setUserDepartments(setUserDepartmentsDto);

        UserSingleRespDto respDto = managementClient.updateUser(reqDto);
        return respDto.getStatusCode() == 200 ? RespBean.ok("修改成功") : RespBean.error(respDto.getMessage());
    }

    public RespBean deleteAuthEmpByEid(String id) {
        DeleteUsersBatchDto reqDto = new DeleteUsersBatchDto();
        List<String> authingUserIdList = new ArrayList<>();
        authingUserIdList.add(id);
        reqDto.setUserIds(authingUserIdList);
        IsSuccessRespDto respDto = managementClient.deleteUsersBatch(reqDto);
        return respDto.getStatusCode() == 200 ? RespBean.ok("删除成功") : RespBean.error(respDto.getMessage());
    }
}
