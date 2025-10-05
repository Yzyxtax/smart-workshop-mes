package com.xtax.service;

import com.xtax.pojo.ResultPage;
import com.xtax.pojo.User;
import com.xtax.pojo.UserQueryParam;

import java.util.List;

public interface userService {
    //条件分页查询所有用户信息
    public ResultPage<User> getAllUser(UserQueryParam userQueryParam);

    //添加用户信息
    public int addUser(User user);

    //删除用户信息
    public int deleteUsers(List<Integer> ids);

    //根据id查询用户信息
    public User getUserById(Integer id);

    //修改用户信息
    public int updateUser(User user);
}
