package com.xtax.service;

import com.xtax.pojo.Bom;

import java.util.List;

public interface bomService {
    //查询所有物料的名称和层次
    public List<Bom> getAllMaterialName();

    //根据id查询物料的详细信息
    public Bom getBomById(Integer id);

    //修改物料信息
    public int updateBom(Bom bom);

    //添加物料信息
    public int addBom(Bom bom);

    //删除物料信息
    public int deleteBom(Integer id);
}
