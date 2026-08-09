package com.shivani.notepad.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}