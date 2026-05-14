package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusHistory {
    private Integer id;
    private String targetType;
    private String targetNo;
    private String oldStatus;
    private String newStatus;
    private Integer operatorId;
    private String createdTime;
}
