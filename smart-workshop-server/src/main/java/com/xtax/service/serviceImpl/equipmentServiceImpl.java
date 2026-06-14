package com.xtax.service.serviceImpl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xtax.mapper.equipmentFunctionMapper;
import com.xtax.mapper.equipmentMapper;
import com.xtax.entity.Equipment;
import com.xtax.dto.EquipmentQueryParam;
import com.xtax.entity.FunctionDescription;
import com.xtax.vo.ResultPage;
import com.xtax.service.equipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class equipmentServiceImpl implements equipmentService {
    @Autowired
    private equipmentMapper equipmentMapper;
    @Autowired
    private equipmentFunctionMapper equipmentFunctionMapper;

    //分页查询所有设备信息
    @Override
    public ResultPage<Equipment> getAllEquipment(EquipmentQueryParam equipmentQueryParam) {
        //1.设置分页参数
        Integer page = equipmentQueryParam.getPage();
        Integer pageSize = equipmentQueryParam.getPageSize();
        String name = equipmentQueryParam.getName();
        String type = equipmentQueryParam.getType();
        String model = equipmentQueryParam.getModel();

        PageHelper.startPage(page, pageSize);

        //2.执行查询
        List<Equipment> list = equipmentMapper.getAllEquipment(name, type, model);

        //3.解析查询结果并封装
        Page<Equipment> p = (Page<Equipment>) list;
        return new ResultPage<>(p.getTotal(), p.getResult());
    }

    //添加设备信息
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addEquipment(Equipment equipment) {
        int a = equipmentMapper.addEquipment(equipment);

        Integer id = equipment.getId();
        List<FunctionDescription> fd = equipment.getDescription();
        int b = equipmentFunctionMapper.addEquipmentFunction(fd,id);

        if(a > 0 && b > 0) return (a+b);
        return 0;
    }

    //根据id查询设备信息
    @Override
    public Equipment getEquipmentById(Integer id) {
        return equipmentMapper.getEquipmentById(id);
    }

    //修改设备信息
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEquipment(Equipment equipment) {
        int a = equipmentMapper.updateEquipment(equipment);
        List<Integer> id = Collections.singletonList(equipment.getId());
        int b = equipmentFunctionMapper.deleteEquipmentFunction(id);
        int c = equipmentFunctionMapper.addEquipmentFunction(equipment.getDescription(),equipment.getId());
        if(a > 0 && b > 0 && c > 0) return (a+b+c);
        return 0;
    }

    //删除设备信息
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteEquipment(List<Integer> ids) {
        int a = equipmentMapper.deleteEquipment(ids);
        int b = equipmentFunctionMapper.deleteEquipmentFunction(ids);
        return a;
    }

    //查询所有设备信息
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Equipment> listAllEquipment() {
        List<Equipment> list = equipmentMapper.listAllEquipment();
        List<Equipment> fd = equipmentFunctionMapper.listAllEquipmentFunction();
        for (Equipment equipment : list){
            for (Equipment equipment1 : fd){
                if(equipment.getId().equals(equipment1.getId())){
                    equipment.setDescription(equipment1.getDescription());
                }
            }
        }
        return list;
    }
}
