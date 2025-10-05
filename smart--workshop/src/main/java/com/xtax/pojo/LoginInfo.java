package com.xtax.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装登录信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginInfo {
    private String username;
    private String password;
    private String name;
    private String token;
}
