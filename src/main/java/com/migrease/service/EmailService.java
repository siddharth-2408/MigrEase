package com.migrease.service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.migrease.config.MailConfig;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class EmailService {
	private String host = "smtp.gmail.com"; // SMTP server
	private String from = "cocclash8859966@gmail.com"; // Sender email

	public void sendEmail(String subject, String messageToSend, String to) {
		System.out.println("Inside Send Email from emailService");
		Session session = new MailConfig().getSession(host, from);
		session.setDebug(true); // Print debug info

		try {
			// Create a default MimeMessage object
			MimeMessage message = new MimeMessage(session);

			// Set From: header field
			message.setFrom(new InternetAddress(from));

			// Set To: header field
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// Set Subject and actual message
			message.setSubject(subject);
			message.setText(messageToSend);

			// Send message
			Transport.send(message);
			System.out.println("Email sent successfully...");
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}
	public void sendBookingReceipt(String subject, String userName, String to, String attachmentPath, String bookingId) {
		Session session = new MailConfig().getSession(host, from);
		session.setDebug(true);

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject(subject);

			// Professional message
			String messageText = "Dear " + userName + ",\n\n" +
					"Thank you for choosing MigrEase for your recent furniture movement. We’re pleased to confirm that your booking (ID: "
					+ bookingId + ") has been successfully placed.\n\n" +
					"Please find your official receipt attached to this email. It includes all relevant details such as booking ID, pickup/drop addresses, item details, distance, weight, and charges.\n\n" +
					"If you have any questions or concerns, feel free to reply to this email or reach out to our support team.\n\n" +
					"We look forward to serving you again!\n\n" +
					"Warm regards,\n" +
					"The MigrEase Team";

			MimeMultipart mimeMultipart = new MimeMultipart();

			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setText(messageText);

			MimeBodyPart filePart = new MimeBodyPart();
			File file = new File(attachmentPath);
			filePart.attachFile(file);

			mimeMultipart.addBodyPart(textPart);
			mimeMultipart.addBodyPart(filePart);

			message.setContent(mimeMultipart);

			Transport.send(message);
			System.out.println("📧 Booking receipt sent to " + to);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
