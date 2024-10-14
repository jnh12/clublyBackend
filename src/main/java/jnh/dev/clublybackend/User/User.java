package jnh.dev.clublybackend.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;

    private String email;
    private String password;
    private boolean verified; // Track verification status

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.verified = false; // Default to unverified
    }
}
