package jnh.dev.clublybackend.Clubs;



import jnh.dev.clublybackend.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
public class ClubController {
    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private ClubService clubService;

    @PostMapping("/create")
    public ResponseEntity<Club> createClub(@RequestParam("name") String name,
                                           @RequestParam("description") String description,
                                           @RequestParam("category") String category,
                                           @RequestParam("image") MultipartFile image,
                                           @RequestHeader("userId") String userId) {
        try {
            // Handle image upload and get the URL
            String imageUrl = clubService.uploadImage(image); // Implement this method in your ClubService

            // Create a new club instance
            Club club = new Club(name, description, category, imageUrl, userId);
            club.getAdminIds().add(userId);
            club.getMembers().add(userId);

            Club savedClub = clubRepository.save(club);
            return ResponseEntity.ok(savedClub);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null); // Return a proper error response
        }
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
