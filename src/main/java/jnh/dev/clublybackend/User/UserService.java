package jnh.dev.clublybackend.User;

import ch.qos.logback.core.CoreConstants;
import jnh.dev.clublybackend.Email.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final String RECAPTCHA_SECRET = "6LdUb18qAAAAAKFJaDkAkYM4k6sVPGKfxewU3uhJ";  //secret key

    @Autowired
    private EmailService emailService;

    public boolean validateRecaptcha(String recaptchaToken) {
        String url = "https://www.google.com/recaptcha/api/siteverify";
        RestTemplate restTemplate = new RestTemplate();

        System.out.println(recaptchaToken);

        Map<String, String> params = new HashMap<>();
        params.put("secret", RECAPTCHA_SECRET);  // Your reCAPTCHA secret key
        params.put("response", recaptchaToken);  // The token from the frontend

        ResponseEntity<Map> response = restTemplate.postForEntity(url, params, Map.class);

        Map<String, Object> body = response.getBody();

        System.out.println("reCAPTCHA validation response: " + body);
        Boolean success = (Boolean) body.get("success");

        System.out.println("reCAPTCHA validation response: " + body);

        return success != null && success;
    }


    public void registerUser(User user) throws Exception {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new Exception("Email already exists.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setVerified(false); // Set the user as unverified initially
        userRepository.save(user); // Save the user with email and hashed password

        String token = emailService.createVerificationToken(user.getEmail());
        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    public User loginUser(String email, String password) throws Exception {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (!userOptional.isPresent()) {
            throw new Exception("Invalid email.");
        }

        User user = userOptional.get();

        if(!user.isVerified()){
            throw new Exception("User is not verified.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new Exception("Invalid password.");
        }

        return user;
    }

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;


    public void verifyEmail(String token) throws Exception {
        Optional<VerificationToken> optionalToken = verificationTokenRepository.findByToken(token);

        if (!optionalToken.isPresent()) {
            throw new Exception("Invalid token.");
        }

        VerificationToken verificationToken = optionalToken.get();

        if (verificationToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new Exception("Token expired.");
        }

        User user = userRepository.findByEmail(verificationToken.getEmail())
                .orElseThrow(() -> new Exception("User not found."));

        user.setVerified(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);
    }


    public void requestPasswordReset(String email) throws Exception {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            throw new Exception("No user with the provided email.");
        }

        String token = emailService.createPasswordResetToken(email);
        emailService.sendResetPasswordEmail(email, token);
    }

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    public void resetPassword(String token, String newPassword) throws Exception {
        Optional<PasswordResetToken> resetTokenOptional = tokenRepository.findByToken(token);

        if (!resetTokenOptional.isPresent() || !isTokenValid(resetTokenOptional.get())) {
            throw new Exception("Invalid or expired token.");
        }

        PasswordResetToken resetToken = resetTokenOptional.get();
        User user = userRepository.findByEmail(resetToken.getEmail()).orElseThrow(() -> new Exception("User not found."));

        // Hash the new password and save the user
        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        userRepository.save(user);
    }

    public boolean isTokenValid(PasswordResetToken resetToken) {
        return resetToken.getExpirationDate().isAfter(LocalDateTime.now());
    }


}
