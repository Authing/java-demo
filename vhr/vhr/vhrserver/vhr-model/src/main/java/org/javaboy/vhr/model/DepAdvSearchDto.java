package org.javaboy.vhr.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DepAdvSearchDto {
    private String organizationCode;
    private String departmentId;
    private String departmentIdType;
    private boolean includeChildrenDepartments;
}
