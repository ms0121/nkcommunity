package com.liu.nkcommunity;

import com.liu.nkcommunity.domain.User;
import com.liu.nkcommunity.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

public class UserTest extends NkcommunityApplicationTests {

    @Autowired
    private UserMapper userMapper;


    @Test
    public void selectTest() {
        User user = userMapper.selectById(149);
        System.out.println("user = " + user);

        User lihonghe = userMapper.selectByName("lihonghe");
        System.out.println("lihonghe = " + lihonghe);

        User user1 = userMapper.selectByEmail("nowcoder145@sina.com");
        System.out.println("user1 = " + user1);
    }


    @Test
    public void insertTest(){
        User user = new User();
        user.setUsername("zhangsan");
        user.setPassword("123456");
        user.setEmail("1232@qq.com");
        user.setSalt("jksdjks");
        user.setType(0);
        user.setStatus(1);
        user.setHeaderUrl("http://images.nowcoder.com/head/150t.png");
        user.setCreateTime(new Date());
        int flag = userMapper.insertUser(user);
        System.out.println("flag = " + flag);
    }

    @Test
    public void updateTest(){
        int updateStatus = userMapper.updateStatus(150, 0);
        System.out.println("updateStatus = " + updateStatus);

        int updateHeader = userMapper.updateHeader(150, "http://images.nowcoder.com/head/1500t.png");
        System.out.println("updateHeader = " + updateHeader);

        int updatePassword = userMapper.updatePassword(150, "888888");
        System.out.println("updatePassword = " + updatePassword);
    }
}
