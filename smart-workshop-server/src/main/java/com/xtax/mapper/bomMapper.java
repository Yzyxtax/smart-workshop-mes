package com.xtax.mapper;

import com.xtax.entity.Bom;
import com.xtax.vo.BomTreeData;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface bomMapper {
    //查询所有物料的名称和层次
    @Results(id = "bomTreeData", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "parent_id", property = "parentId"),
            @Result(column = "name_specification", property = "label")
    })
    @Select("Select id,parent_id,name_specification from bom")
    public List<BomTreeData> getAllMaterialName();

    //根据id查询BOM
    @Select("Select * from bom where id=#{id}")
    public Bom getBomById(Integer id);

    //修改BOM
    public int updateBom(Bom bom);

    //添加BOM
    @Options(useGeneratedKeys = true,keyColumn = "id",keyProperty = "id")
    public int addBom(Bom bom);

    //删除BOM
    public int deleteBom(List<Integer> ids);

    //修改BOM的层次
    @Update("Update bom set parent_id=#{parentId} where id=#{id}")
    int updateBomLevel(Integer id, Integer parentId);
}
