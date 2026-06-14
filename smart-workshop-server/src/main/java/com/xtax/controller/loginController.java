package com.xtax.controller;

import com.xtax.entity.LoginInfo;
import com.xtax.vo.Result;
import com.xtax.service.serviceImpl.userServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/login")
public class loginController {
    @Autowired
    private userServiceImpl userServiceImpl;
    @PostMapping
    public Result login(@RequestBody LoginInfo loginForm) {
        log.info("用户登录：{}", loginForm);
        String userName = loginForm.getUserName();
        String password = loginForm.getPassword();
        LoginInfo loginInfo = userServiceImpl.login(userName, password);
        if(loginInfo != null) return Result.success(loginInfo);
        return Result.error("登录失败");
    }
}
