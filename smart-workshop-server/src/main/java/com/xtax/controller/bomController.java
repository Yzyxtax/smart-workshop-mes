package com.xtax.controller;

import com.xtax.entity.Bom;
import com.xtax.vo.BomTreeData;
import com.xtax.vo.Result;
import com.xtax.service.serviceImpl.bomServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * @author yzyxt
 */
@Slf4j
@RequestMapping("/bom")
@RestController
public class bomController {
    @Autowired
    private bomServiceImpl bomServiceImpl;

    @GetMapping
    public Result getAllBom(){
        log.info("查询所有BOM");
        List<BomTreeData> bom = bomServiceImpl.getAllMaterialName();
        if(bom != null) {
            return Result.success(bom);
        }
        return Result.error("查询失败");
    }

    @GetMapping("/{id}")
    public Result getBomById(@PathVariable Integer id){
        log.info("查询id为{}的BOM",id);
        Bom bom = bomServiceImpl.getBomById(id);
        if(bom != null) {
            return Result.success(bom);
        }
        return Result.error("查询失败");
    }

    @PutMapping
    public Result updateBom(@RequestBody Bom bom){
        log.info("更新BOM:{}", bom.getId());
        int update = bomServiceImpl.updateBom(bom);
        if(update > 0) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @PutMapping("/level")
    public Result updateBomLevel(Integer id, Integer parentId){
        log.info("更新id为{}的BOM的层级:{}", id, parentId);
        int update = bomServiceImpl.updateBomLevel(id, parentId);
        if(update > 0) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @PostMapping
    public Result addBom(@RequestBody Bom bom){
        log.info("添加BOM:{}", bom);
        int add = bomServiceImpl.addBom(bom);
        if(add > 0) {
            return Result.success(add);
        }
        return Result.error("添加失败");
    }

    @DeleteMapping("/{ids}")
    public Result deleteBom(@PathVariable List<Integer> ids){
        log.info("删除id为{}的BOM",ids);
        int delete = bomServiceImpl.deleteBom(ids);
        if(delete > 0) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
