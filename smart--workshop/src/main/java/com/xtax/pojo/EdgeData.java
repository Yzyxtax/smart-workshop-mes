package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 边数据实体类
 * 描述流程图中节点之间的连接关系
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EdgeData {
    private String sourceNodeName;
    private String targetNodeName;
}
