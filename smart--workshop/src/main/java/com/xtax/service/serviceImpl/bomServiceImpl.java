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

    //根据id查询
    @Override
    public Bom getBomById(Integer id) {
        return bomMapper.getBomById(id);
    }

    //修改
    @Override
    public int updateBom(Bom bom) {
        return bomMapper.updateBom(bom);
    }

    //添加
    @Override
    public int addBom(Bom bom) {
        return bomMapper.addBom(bom);
    }

    //删除
    @Override
    public int deleteBom(Integer id) {
        //获取要删除的BOM的层次
        Bom bom = bomMapper.getBomById(id);
        String levelNum = bom.getLevelNum();
        //获取该层次下的所有BOM的id
        List<Integer> bomIdList = bomMapper.getBomIdByLevelNum(levelNum);
        //删除该层次下的所有BOM
        return bomMapper.deleteBom(bomIdList);
    }
}
