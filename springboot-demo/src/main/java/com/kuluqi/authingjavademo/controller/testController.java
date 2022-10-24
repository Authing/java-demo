package com.kuluqi.authingjavademo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Controller
public class testController {

    @RequestMapping("/test1")
    public String index() {
        return "test1.html";
    }

}
