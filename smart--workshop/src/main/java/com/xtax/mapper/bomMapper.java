package com.xtax.mapper;

import com.xtax.pojo.Bom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface bomMapper {
    //查询所有物料的名称和层次
    @Select("Select level_num,name_specification from bom")
    public List<Bom> getAllMaterialName();
}
