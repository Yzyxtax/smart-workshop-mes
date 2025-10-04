package com.xtax.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Equipment {
    private Integer id;
    private String name;
    private String type;
    private String model;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate productionDate;
    private String manufacturer;

    //功能描述
    private List<String> functionDescription;
}
