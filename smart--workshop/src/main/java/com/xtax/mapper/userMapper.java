package com.xtax.mapper;

import com.xtax.pojo.FreeUserName;
import com.xtax.pojo.User;
import com.xtax.pojo.UserVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface userMapper {
    //条件分页查询所有用户信息
    public List<User> getAllUser(String name, String position, LocalDate begin, LocalDate end);

    //添加用户信息
    public int addUser(UserVO user);

    //添加技能信息
    public int addSkill(String username, String name, String skill);

    //删除技能信息
    public int deleteSkill(String username);

    //删除用户信息
    public int deleteUsers(List<Integer> ids);

    //根据id查询用户信息
    public UserVO getUserById(Integer id);

    //修改用户信息
    public int updateUser(UserVO user);

    //根据用户名和密码查询用户信息
    @Select("select * from users where username=#{userName} and password=#{password}")
    User getUserByNameAndPassword(String userName, String password);

    //查询还没有加入班组的员工用户名
    @Select("select username from users where team_no is null and position = '工人'")
    List<String> findUserByTeamNo();

    //查询还没有加入班组的班组长用户名
    @Select("select username from users where team_no is null and position = '班组长'")
    List<String> findLeaderByTeamNo();
}
