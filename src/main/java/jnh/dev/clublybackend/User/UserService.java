package jnh.dev.clublybackend.User;

import ch.qos.logback.core.CoreConstants;
import jnh.dev.clublybackend.Email.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;


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

    @Value("${recaptcha.secret}")  // Inject the secret key from application.properties
    private String recaptchaSecret;

    public boolean validateRecaptcha(String recaptchaToken) {
        String url = "https://www.google.com/recaptcha/api/siteverify";
        RestTemplate restTemplate = new RestTemplate();
    
        // Prepare the parameters for the POST request
        Map<String, String> params = new HashMap<>();
        params.put("secret", recaptchaSecret);
        params.put("response", recaptchaToken);
    
        // Make the API call to Google's reCAPTCHA service
        ResponseEntity<Map> response = restTemplate.postForEntity(url, params, Map.class);
        Map<String, Object> body = response.getBody();
    
        if (body == null || !body.containsKey("success")) {
            throw new RuntimeException("Failed to validate reCAPTCHA.");
        }
    
        // Log the entire response for debugging
        System.out.println("reCAPTCHA Validation Response: " + body);
    
        return Boolean.TRUE.equals(body.get("success"));
    }
    


    public void registerUser(User user, String recaptchaToken) throws Exception {
        // Validate reCAPTCHA first
        if (!validateRecaptcha(recaptchaToken)) {
            throw new Exception("Invalid reCAPTCHA token.");
        }
    
        // Check if the user already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new Exception("Email already exists.");
        }
    
        // Hash and save the user's password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setVerified(false);
        userRepository.save(user);
    
        // Send a verification email
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
