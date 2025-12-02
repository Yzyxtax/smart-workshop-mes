package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bom树形数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BomTreeData {
    private Integer id;
    private Integer parentId;
    private String label;
}
