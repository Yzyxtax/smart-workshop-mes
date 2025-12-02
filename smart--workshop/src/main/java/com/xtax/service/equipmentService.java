package com.xtax.service;

import com.xtax.pojo.Equipment;
import com.xtax.pojo.EquipmentQueryParam;
import com.xtax.pojo.ResultPage;

import java.util.List;

public interface equipmentService {
    //分页查询所有设备信息
    public ResultPage<Equipment> getAllEquipment(EquipmentQueryParam equipmentQueryParam);

    //添加设备信息
    public int addEquipment(Equipment equipment);

    //根据id查询设备信息
    public Equipment getEquipmentById(Integer id);

    //修改设备信息
    public int updateEquipment(Equipment equipment);

    //删除设备信息
    int deleteEquipment(List<Integer> ids);

    //查询所有设备信息
    List<Equipment> listAllEquipment();
}
