package jnh.dev.clublybackend.Clubs;



import jnh.dev.clublybackend.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
public class ClubController {
    @Autowired
    private ClubRepository clubRepository;

    @PostMapping("/create")
    public ResponseEntity<Club> createClub(@RequestBody Club club, @RequestHeader("userId")String userId){
        club.getAdminIds().add((userId));
        club.getMembers().add((userId));

        Club savedClub = clubRepository.save(club);
        return ResponseEntity.ok(savedClub);
    }

    @GetMapping
    public ResponseEntity<List<Club>> getAllCLubs(){
        List<Club> clubs = clubRepository.findAll();
        return ResponseEntity.ok(clubs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Club> getClubById(@PathVariable String id){
        Club club = clubRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(("Club not Found")));
        return ResponseEntity.ok(club);
    }

}
