package com.xtax.mapper;

import com.xtax.entity.Equipment;
import com.xtax.entity.FunctionDescription;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface equipmentFunctionMapper {
    //添加设备功能
    public int addEquipmentFunction(List<FunctionDescription> equipmentDescription, Integer id);

    //批量删除设备功能
    public int deleteEquipmentFunction(List<Integer> ids);

    //查询所有设备功能
    List<Equipment> listAllEquipmentFunction();
}
