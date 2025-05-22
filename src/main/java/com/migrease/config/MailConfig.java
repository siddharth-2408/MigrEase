package com.migrease.config;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

public class MailConfig {
	public Session getSession(String host, String from) {
		// Get system properties
        Properties properties = System.getProperties();
        System.out.println("Hello World!");

        // Setup mail server
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        // Get the Session object. Provide username and password
        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, "bkvr skim cith qhmy");
            }
        });
        return session;
	}
}
