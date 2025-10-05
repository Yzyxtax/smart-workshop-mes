package com.xtax.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface equipmentFunctionMapper {
    //添加设备功能
    public int addEquipmentFunction(List<String> equipmentDescription, Integer id);

    //批量删除设备功能
    public int deleteEquipmentFunction(List<Integer> ids);
}
