package com.xtax.mapper;

import com.xtax.pojo.Equipment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface equipmentMapper {
    //分页查询所有设备信息
    public List<Equipment> getAllEquipment(String name, String type, String model);

    //添加设备信息
    @Options(useGeneratedKeys = true,keyColumn = "id",keyProperty = "id")
    @Insert("insert into equipment_info(name,type,model,production_date,manufacturer) values(#{name},#{type},#{model},#{productionDate},#{manufacturer})")
    public int addEquipment(Equipment equipment);

    //根据id查询设备信息和功能信息
    public Equipment getEquipmentById(Integer id);
}
