package jnh.dev.clublybackend.User;

import jnh.dev.clublybackend.Requests.LoginRequest;
import jnh.dev.clublybackend.Requests.SignUpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest signUpRequest) {

        System.out.println("Received signup request: " + signUpRequest);

        try {
            User user = new User(signUpRequest.getEmail(), signUpRequest.getPassword());
            User registeredUser = userService.registerUser(user, signUpRequest.getRecaptchaToken());
            return ResponseEntity.ok(registeredUser);
        }

        catch (Exception e) {
            // Log the error and return a 400 Bad Request with the error message
            System.out.println("Error during signup: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            User user = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}