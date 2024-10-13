package jnh.dev.clublybackend.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final String RECAPTCHA_SECRET = "6LdUb18qAAAAAKFJaDkAkYM4k6sVPGKfxewU3uhJ";  //secret key

    // Method to validate reCAPTCHA token
    public boolean validateRecaptcha(String recaptchaToken) {
        String url = "https://www.google.com/recaptcha/api/siteverify";
        RestTemplate restTemplate = new RestTemplate();

        // Prepare the parameters for the reCAPTCHA API request
        Map<String, String> params = new HashMap<>();
        params.put("secret", RECAPTCHA_SECRET);  // Your reCAPTCHA secret key
        params.put("response", recaptchaToken);  // The token from the frontend

        // Send request to Google reCAPTCHA verification API
        ResponseEntity<Map> response = restTemplate.postForEntity(url, params, Map.class);

        // Extract the response from Google
        Map<String, Object> body = response.getBody();

        System.out.println("reCAPTCHA validation response: " + body);
        Boolean success = (Boolean) body.get("success");

        // Log the entire response from Google for debugging
        System.out.println("reCAPTCHA validation response: " + body);

        // Return whether the reCAPTCHA was successful
        return success != null && success;
    }


    public User registerUser(User user, String recaptchaToken) throws Exception {
        // Check if the email is already in use
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new Exception("Email already exists.");
        }

//        // Validate reCAPTCHA token
//        if (!validateRecaptcha(recaptchaToken)) {
//            throw new Exception("Invalid reCAPTCHA.");
//        }

        // Hash and store password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User loginUser(String email, String password) throws Exception {
        // Check if the user exists by email
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (!userOptional.isPresent()) {
            throw new Exception("Invalid email.");
        }

        User user = userOptional.get();

        // Check if the password matches
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new Exception("Invalid password.");
        }

        return user;
    }
}
