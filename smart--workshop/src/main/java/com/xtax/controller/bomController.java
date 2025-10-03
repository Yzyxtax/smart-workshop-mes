package com.xtax.controller;

import com.xtax.pojo.Bom;
import com.xtax.pojo.Result;
import com.xtax.service.serviceImpl.bomServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("/bom")
@RestController
public class bomController {
    @Autowired
    private bomServiceImpl bomServiceImpl;

    @GetMapping
    public Result getAllBom(){
        List<Bom> bom = bomServiceImpl.getAllMaterialName();
        if(bom != null) return Result.success(bom);
        return Result.error("查询失败");
    }
}
