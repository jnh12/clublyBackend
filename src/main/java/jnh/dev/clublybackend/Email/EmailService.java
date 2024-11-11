package jnh.dev.clublybackend.Email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    public void sendResetPasswordEmail(String toEmail, String token) {
        String subject = "Reset your password";
        String url = "http://localhost:3000/auth/reset-password?token=" + token;
        String message = "Click the following link to reset your password: \n" + url;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(toEmail);
        email.setSubject(subject);
        email.setText(message);
        mailSender.send(email);
    }

    public String createPasswordResetToken(String email) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(email, token, LocalDateTime.now().plusHours(1));
        tokenRepository.save(resetToken);
        return token;
    }

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "Verify your email";
        String url = "http://localhost:8080/auth/verify-email?token=" + token;
        String message = "Click the following link to verify your email: \n" + url;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(toEmail);
        email.setSubject(subject);
        email.setText(message);
        mailSender.send(email);
    }

    public void sendJoinedEmail(String toEmail, String clubName) {
        String subject = "Welcome to the " + clubName;
        String messageBody = "Dear member,\n\nWelcome to " + clubName + "!\nWe are thrilled to have you as part of our club. "
                + "We look forward to your participation in our upcoming activities.\n\nBest regards,\nClubly Team";

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(toEmail);
        email.setSubject(subject);
        email.setText(messageBody);

        mailSender.send(email);
    }

    public void sendApproachingEmail(String toEmail, String clubName, Date eventDate) {
        String subject = "An event you joined in the " + clubName;
        String messageBody = "The event you signed up for is on " + eventDate;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(toEmail);
        email.setSubject(subject);
        email.setText(messageBody);

        mailSender.send(email);
    }

    public String createVerificationToken(String email) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(email, token, LocalDateTime.now().plusHours(24));
        verificationTokenRepository.save(verificationToken);
        return token;
    }

}
