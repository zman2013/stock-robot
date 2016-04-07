package com.zman.stock.service;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void send(String title, String content) throws MessagingException {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("smtp.163.com");
        sender.setUsername("stockanalysis2014@163.com");
        sender.setPassword("qwe123");
        Properties props = System.getProperties();
        props.put("mail.smtp.auth", "true"); // 这样才能通过验证
        sender.setJavaMailProperties(props);

        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setTo("vsmingren@qq.com");
        helper.setFrom("stockanalysis2014@163.com");
        helper.setSubject(title);
        message.setContent(content, "text/html;charset=utf8");

        sender.send(message);
    }
}
