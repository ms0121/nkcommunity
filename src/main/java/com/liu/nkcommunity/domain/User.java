package com.liu.nkcommunity.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private String salt;
    // 0-普通用户; 1-超级管理员; 2-版主;
    private int type;
    // 0-未激活; 1-已激活;
    private int status;
    // 激活码
    private String activationCode;
    // 头像访问路径
    private String headerUrl;
    @JsonFormat(shape = JsonFormat.Shape.STRING , pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createTime;
}
