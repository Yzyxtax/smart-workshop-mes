package com.xtax.service.serviceImpl;

import com.xtax.mapper.bomMapper;
import com.xtax.pojo.Bom;
import com.xtax.pojo.BomTreeData;
import com.xtax.service.bomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class bomServiceImpl implements bomService {
    @Autowired
    private bomMapper bomMapper;

    //查询所有物料的名称和层次
    @Override
    public List<BomTreeData> getAllMaterialName() {
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
        int result = bomMapper.addBom(bom);
        Integer id = bom.getId();
//        if(id == null){
//            log.info("插入失败");
//            return 0;
//        }
        return id;
    }

    //修改BOM的层次
    @Override
    public int updateBomLevel(Integer id, Integer parentId) {
        return bomMapper.updateBomLevel(id, parentId);
    }

    //删除
    @Override
    public int deleteBom(List<Integer> ids) {
        return bomMapper.deleteBom(ids);
    }
}
