package jnh.dev.clublybackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUp signUpRequest) {
        // Log the incoming request to check the received data
        System.out.println("Received signup request: " + signUpRequest);

        try {
            // Attempt to register the user
            User user = new User(signUpRequest.getEmail(), signUpRequest.getPassword());
            User registeredUser = userService.registerUser(user, signUpRequest.getRecaptchaToken());
            return ResponseEntity.ok(registeredUser);
        } catch (Exception e) {
            // Log the error and return a 400 Bad Request with the error message
            System.out.println("Error during signup: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        try {
            User user = userService.loginUser(email, password);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
