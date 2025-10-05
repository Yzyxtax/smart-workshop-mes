package com.xtax.controller;

import com.xtax.pojo.LoginInfo;
import com.xtax.pojo.Result;
import com.xtax.service.serviceImpl.userServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/login")
public class loginController {
    @Autowired
    private userServiceImpl userServiceImpl;
    @GetMapping
    public Result login(String userName, String password) {
        log.info("用户登录：{}，{}", userName, password);
        LoginInfo loginInfo = userServiceImpl.login(userName, password);
        if(loginInfo != null) return Result.success(loginInfo);
        return Result.error("登录失败");
    }
}
