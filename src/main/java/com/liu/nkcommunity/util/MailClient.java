package com.liu.nkcommunity.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * 将发邮件的功能委托给新浪邮箱去做，当前的类就是将要发送的文件内容交给新浪邮箱即可
 */
@Component
public class MailClient {

    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    // 邮件发送者
    @Autowired
    private JavaMailSender mailSender;

    // 发件人
    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送人
     * 发送的邮件主题，内容
     * 接收人
     */
    public void sendMail(String to, String subject, String content) {
        try {
            // 构建MimeMessage对象
            MimeMessage message = mailSender.createMimeMessage();
            // 通过使用helper构建邮件信息
            MimeMessageHelper helper = new MimeMessageHelper(message);
            // 发件人，收件人，主题
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            // 内容，支持html的content
            helper.setText(content, true);
            // 发送邮件
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("邮件发送失败: " + e.getMessage());
        }
    }

}
