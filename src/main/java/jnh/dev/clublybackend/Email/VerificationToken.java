package jnh.dev.clublybackend.Email;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "email_verification_tokens")
public class VerificationToken {
    @Id
    private String id;
    private String email;
    private String token;
    private LocalDateTime expirationDate;

    public VerificationToken(String email, String token, LocalDateTime expirationDate) {
        this.email = email;
        this.token = token;
        this.expirationDate = expirationDate;
    }
}
