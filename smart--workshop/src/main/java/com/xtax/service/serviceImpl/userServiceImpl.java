package com.xtax.service.serviceImpl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xtax.mapper.userMapper;
import com.xtax.pojo.LoginInfo;
import com.xtax.pojo.ResultPage;
import com.xtax.pojo.User;
import com.xtax.pojo.UserQueryParam;
import com.xtax.service.userService;
import com.xtax.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class userServiceImpl implements userService {
    @Autowired
    private userMapper userMapper;

    //条件分页查询所有用户信息
    @Override
    public ResultPage<User> getAllUser(UserQueryParam userQueryParam) {
        Integer page = userQueryParam.getPage();
        Integer pageSize = userQueryParam.getPageSize();
        String name = userQueryParam.getName();
        String position = userQueryParam.getPosition();
        LocalDate begin = userQueryParam.getBegin();
        LocalDate end = userQueryParam.getEnd();

        PageHelper.startPage(page, pageSize);

        List<User> list = userMapper.getAllUser(name, position, begin, end);

        Page<User> p = (Page<User>) list;
        return new ResultPage<>(p.getTotal(), p.getResult());
    }

    //添加用户信息
    @Override
    public int addUser(User user) {
        return userMapper.addUser(user);
    }

    //删除用户信息
    @Override
    public int deleteUsers(List<Integer> ids) {
        return userMapper.deleteUsers(ids);
    }

    //根据id查询用户信息
    @Override
    public User getUserById(Integer id) {
        return userMapper.getUserById(id);
    }

    //修改用户信息
    @Override
    public int updateUser(User user) {
        return userMapper.updateUser(user);
    }

    @Override
    public LoginInfo login(String userName, String password) {
        User user = userMapper.getUserByNameAndPassword(userName, password);
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("userName", user.getUserName());
        String token = JwtUtils.generateToken(claims);
        return new LoginInfo(user.getUserName(), user.getPassword(), user.getName(), token);
    }
}
