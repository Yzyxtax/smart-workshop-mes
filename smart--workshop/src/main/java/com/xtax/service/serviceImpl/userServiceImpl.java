package com.xtax.service.serviceImpl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xtax.mapper.userMapper;
import com.xtax.pojo.*;
import com.xtax.service.userService;
import com.xtax.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(rollbackFor = Exception.class)
    public int addUser(UserVO user) {
        int i = userMapper.addUser(user);
        for (String skill : user.getProcessName()) {
            userMapper.addSkill(user.getUserName(), user.getName(), skill);
        }
        return i;
    }

    //删除用户信息
    @Override
    public int deleteUsers(List<Integer> ids) {
        return userMapper.deleteUsers(ids);
    }

    //根据id查询用户信息
    @Override
    public UserVO getUserById(Integer id) {
        return userMapper.getUserById(id);
    }

    //修改用户信息
    @Override
    public int updateUser(UserVO user) {
        userMapper.updateUser(user);
        userMapper.deleteSkill(user.getUserName());
        for (String skill : user.getProcessName()){
            userMapper.addSkill(user.getUserName(), user.getName(), skill);
        }
        return 1;
    }

    @Override
    public LoginInfo login(String userName, String password) {
        User user = userMapper.getUserByNameAndPassword(userName, password);
        if (user == null) return null;
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("userName", user.getUserName());
        String token = JwtUtils.generateToken(claims);
        return new LoginInfo(user.getId(), user.getUserName(), user.getPassword(), user.getName(), token);
    }

    //查询还没有加入班组的成员
    @Transactional(rollbackFor = Exception.class)
    @Override
    public FreeUserName findUserByTeamNo() {
        FreeUserName freeUserName = new FreeUserName();
        freeUserName.setEmpList(userMapper.findUserByTeamNo());
        freeUserName.setLeaderList(userMapper.findLeaderByTeamNo());
        return freeUserName;
    }
}
