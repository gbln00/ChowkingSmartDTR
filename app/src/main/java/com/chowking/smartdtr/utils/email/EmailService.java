package com.chowking.smartdtr.utils.email;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {

    // IMPORTANT: Use an "App Password" from Google Account settings, NOT your regular password.
    private static final String SENDER_EMAIL = "2001102491@student.buksu.edu.ph";
    private static final String APP_PASSWORD = "tkprptrawvpahohs";

    public static void sendEmail(String recipientEmail, String subject, String messageContent) {
        new SendEmailTask(recipientEmail, subject, messageContent).execute();
    }

    private static class SendEmailTask extends AsyncTask<Void, Void, Boolean> {
        private final String recipientEmail;
        private final String subject;
        private final String messageContent;

        public SendEmailTask(String recipientEmail, String subject, String messageContent) {
            this.recipientEmail = recipientEmail;
            this.subject = subject;
            this.messageContent = messageContent;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SENDER_EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject(subject);
                message.setText(messageContent);

                Transport.send(message);
                return true;
            } catch (MessagingException e) {
                Log.e("EmailService", "Error sending email", e);
                return false;
            }
        }
    }
}