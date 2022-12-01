package org.javaboy.vhr.converter;

import cn.authing.sdk.java.dto.DepartmentDto;
import org.javaboy.vhr.model.Department;
import org.springframework.stereotype.Component;

@Component
public class AuthingDepToDepConverter {
    public Department convert(DepartmentDto departmentDto){
        Department res = new Department();
        res.setName(departmentDto.getName());
        res.setAuthingDepartmentId(departmentDto.getDepartmentId());
        res.setParent(departmentDto.getHasChildren());
        res.setParentAuthingId(departmentDto.getParentDepartmentId());
        return res;
    }
}
