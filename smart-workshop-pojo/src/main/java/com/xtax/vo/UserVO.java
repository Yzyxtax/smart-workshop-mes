package com.xtax.vo;

import com.xtax.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户视图对象
 * 继承User类，扩展了工序名称列表字段
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVO extends User {
    private List<String> processName;
}
