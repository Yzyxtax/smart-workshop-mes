package com.xtax.service;

import com.xtax.pojo.*;

import java.util.List;

public interface userService {
    //条件分页查询所有用户信息
    public ResultPage<User> getAllUser(UserQueryParam userQueryParam);

    //添加用户信息
    public int addUser(UserVO user);

    //删除用户信息
    public int deleteUsers(List<Integer> ids);

    //根据id查询用户信息
    public UserVO getUserById(Integer id);

    //修改用户信息
    public int updateUser(UserVO user);

    //用户登录
    public LoginInfo login(String userName, String password);

    //查询还没有加入班组的用户
    FreeUserName findUserByTeamNo();
}
