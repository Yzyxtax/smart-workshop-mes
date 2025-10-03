package com.xtax.service.serviceImpl;

import com.xtax.mapper.bomMapper;
import com.xtax.pojo.Bom;
import com.xtax.service.bomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class bomServiceImpl implements bomService {
    @Autowired
    private bomMapper bomMapper;

    //查询所有物料的名称和层次
    @Override
    public List<Bom> getAllMaterialName() {
        return bomMapper.getAllMaterialName();
    }
}
