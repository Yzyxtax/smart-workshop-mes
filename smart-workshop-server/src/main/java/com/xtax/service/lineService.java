package com.xtax.service;

import com.xtax.entity.LineCompose;
import com.xtax.vo.LineComposeVO;
import com.xtax.entity.ProductionLine;

import java.util.List;

public interface lineService {
    //获取所有产线信息
    List<ProductionLine> getAllLine();

    //添加产线信息
    void addLine(ProductionLine productionLine);

    //修改产线信息
    void updateLine(ProductionLine productionLine, String lineNo);

    //删除产线信息
    void deleteLine(String lineNo);

    //获取产线班组信息
    LineCompose getLineCompose(String lineNo);

    //保存产线班组信息
    void updateLineCompose(LineComposeVO lineCompose);

    //获取产线信息
    ProductionLine getLine(String lineNo);
}
