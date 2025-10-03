package com.xtax.service;

import com.xtax.pojo.Bom;

import java.util.List;

public interface bomService {
    //查询所有物料的名称和层次
    public List<Bom> getAllMaterialName();
}
