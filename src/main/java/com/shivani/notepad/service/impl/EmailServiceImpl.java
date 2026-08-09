package com.shivani.notepad.service.impl;

import com.shivani.notepad.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender mailSender;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        logger.info("=== EMAIL SEND ATTEMPT === to={}, subject={}", to, subject);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            logger.info("=== EMAIL SEND SUCCESS === to={}", to);
        } catch (Exception ex) {
            // Full exception, not just getMessage() — this is the single
            // most important log line in this entire feature. Whatever
            // this prints IS the real, final answer to why email isn't
            // arriving.
            logger.error("=== EMAIL SEND FAILED === to={}", to, ex);
            throw new RuntimeException("Email send failed: " + ex.getMessage(), ex);
        }
    }
}