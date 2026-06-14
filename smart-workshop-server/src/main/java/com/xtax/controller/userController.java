package com.xtax.controller;

import com.xtax.annotation.RequirePermission;
import com.xtax.dto.UserQueryParam;
import com.xtax.entity.FreeUserName;
import com.xtax.entity.User;
import com.xtax.service.serviceImpl.userServiceImpl;
import com.xtax.vo.Result;
import com.xtax.vo.ResultPage;
import com.xtax.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
public class userController {
    @Autowired
    private userServiceImpl userServiceImpl;

    @GetMapping
    public Result getAllUser(UserQueryParam userQueryParam){
        Integer page = userQueryParam.getPage();
        Integer pageSize = userQueryParam.getPageSize();
        String name = userQueryParam.getName();
        String position = userQueryParam.getPosition();
        LocalDate begin = userQueryParam.getBegin();
        LocalDate end = userQueryParam.getEnd();

        log.info("条件分页查询：{}，{}，{}，{}，{}，{}", page, pageSize, name, position, begin, end);
        ResultPage<User> user = userServiceImpl.getAllUser(userQueryParam);
        if(user != null) return Result.success(user);
        return Result.error("查询失败");
    }

    @RequirePermission("SYS_USER_CREATE")
    @PostMapping
    public Result addUser(@RequestBody UserVO user){
        log.info("添加用户：{}", user);
        int add = userServiceImpl.addUser(user);
        if(add > 0) return Result.success("添加成功");
        return Result.error("添加失败");
    }

    @RequirePermission("SYS_USER_DELETE")
    @DeleteMapping
    public Result deleteUsers(@RequestParam List<Integer> ids){
        log.info("删除id为{}的用户",ids);
        int delete = userServiceImpl.deleteUsers(ids);
        if(delete > 0) return Result.success("删除成功");
        return Result.error("删除失败");
    }

    @GetMapping("/{id}")
    public Result getUserById(@PathVariable Integer id){
        log.info("查询id为{}的用户",id);
        UserVO user = userServiceImpl.getUserById(id);
        if(user != null) return Result.success(user);
        return Result.error("查询失败");
    }

    @RequirePermission("SYS_USER_UPDATE")
    @PutMapping
    public Result updateUser(@RequestBody UserVO user){
        log.info("更新用户信息：{}", user);
        int update = userServiceImpl.updateUser(user);
        if(update > 0) return Result.success("更新成功");
        return Result.error("更新失败");
    }

    @GetMapping("/join")
    public Result findUserByTeamNo(){
        log.info("查询还没加入班组的用户名");
        FreeUserName user = userServiceImpl.findUserByTeamNo();
        return Result.success(user);
    }
}
