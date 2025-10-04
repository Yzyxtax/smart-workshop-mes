package com.xtax.mapper;

import com.xtax.pojo.Bom;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface bomMapper {
    //查询所有物料的名称和层次
    @Select("Select level_num,name_specification from bom")
    public List<Bom> getAllMaterialName();

    //根据id查询BOM
    @Select("Select * from bom where id=#{id}")
    public Bom getBomById(Integer id);

    //修改BOM
    public int updateBom(Bom bom);

    //添加BOM
    public int addBom(Bom bom);

    //删除BOM
    public int deleteBom(List<Integer> bomIdList);

    //根据层次查询BOM的id
    @Select("SELECT id FROM bom WHERE level_num LIKE CONCAT(#{levelNum}, '%')")
    List<Integer> getBomIdByLevelNum(String levelNum);
}
