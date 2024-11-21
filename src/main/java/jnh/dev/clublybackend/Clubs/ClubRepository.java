package jnh.dev.clublybackend.Clubs;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ClubRepository extends MongoRepository<Club, String> {
    List<Club> findByIsApprovedTrue();
    List<Club> findByIsApprovedFalse();
}
