
package jnh.dev.clublybackend.Email;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface VerificationTokenRepository extends MongoRepository<VerificationToken, String> {
    Optional<VerificationToken> findByToken(String token);
}
