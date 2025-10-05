package com.xtax.mapper;

import com.xtax.pojo.User;
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
    @Insert("insert into users(username,password,name,position,permission_level) " +
            "values(#{userName},#{password},#{name},#{position},#{permissionLevel})")
    public int addUser(User user);

    //删除用户信息
    public int deleteUsers(List<Integer> ids);

    //根据id查询用户信息
    @Select("select * from users where id=#{id}")
    public User getUserById(Integer id);

    //修改用户信息
    public int updateUser(User user);
}
