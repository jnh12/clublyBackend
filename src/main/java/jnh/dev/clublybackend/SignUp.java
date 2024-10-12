package jnh.dev.clublybackend;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignUp {
    private String email;
    private String password;
    private String recaptchaToken;
}
