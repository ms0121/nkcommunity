package com.liu.nkcommunity;


import com.liu.nkcommunity.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

public class MailClientTest extends NkcommunityApplicationTests{

    @Autowired
    private MailClient mailClient;

    // thymeleaf的模板引擎
    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void mailTest(){
        mailClient.sendMail("1132601565@qq.com", "仿牛客社区","这是一个假的邮件！");
    }


    @Test
    public void htmlTest(){
        // 构建文本
        Context context = new Context();
        context.setVariable("username", "Zhangsan");
        // 将html页面转为string类型的发送内容
        String content = templateEngine.process("mail/demo", context);
        System.out.println("content = " + content);

        // 发送邮件
        mailClient.sendMail("1132601565@qq.com", "新的邮件", content);
    }
}
