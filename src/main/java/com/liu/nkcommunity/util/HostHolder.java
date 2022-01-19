package com.liu.nkcommunity.util;

import com.liu.nkcommunity.domain.User;
import org.springframework.stereotype.Component;

/**
 * 使用ThreadLocal进行存取用户信息，起到了每个线程之间的隔离作用
 * 实际：ThreadLocal持有用户信息，用于代替session对象
 */
@Component
public class HostHolder {
    // 创建ThreadLocal对象
    private ThreadLocal<User> users = new ThreadLocal<User>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    // 使用完毕之后清除
    public void clear(){
        users.remove();
    }


}
