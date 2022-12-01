package org.javaboy.vhr.controller.system.basic;

import org.javaboy.vhr.model.Department;
import org.javaboy.vhr.model.RespBean;
import org.javaboy.vhr.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @作者 江南一点雨
 * @公众号 江南一点雨
 * @微信号 a_java_boy
 * @GitHub https://github.com/lenve
 * @博客 http://wangsong.blog.csdn.net
 * @网站 http://www.javaboy.org
 * @时间 2019-10-21 8:02
 */
@RestController
@RequestMapping("/system/basic/department")
public class DepartmentController {
    @Autowired
    DepartmentService departmentService;
    @GetMapping("/")
    public List<Department> getAuthingDepartments() {
        return departmentService.getAuthingDepartments();
    }
    @PostMapping("/")
    public RespBean addAuthingDep(@RequestBody Department dep) {
        return departmentService.addAuthingDep(dep);
    }

    @DeleteMapping("/{authingDepId}")
    public RespBean deleteDepByAuthingDepId(@PathVariable String authingDepId) {
        return departmentService.deleteDepByAuthingDepId(authingDepId);
    }
}