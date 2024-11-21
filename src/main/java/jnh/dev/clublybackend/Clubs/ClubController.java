package jnh.dev.clublybackend.Clubs;

import jnh.dev.clublybackend.Events.Announcments;
import jnh.dev.clublybackend.Events.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/clubs")
public class ClubController {
    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private ClubService clubService;

    @PostMapping("/create-club")
    public ResponseEntity<Club> createClub(@RequestBody ClubDto clubDto) throws IOException {
        Club club = new Club();
        club.setName(clubDto.getName());
        club.setDescription(clubDto.getDescription());
        club.setCategory(clubDto.getCategory());
        club.setAdminIds(List.of(clubDto.getUserId()));
        club.setApproved(false);

        byte[] decodedImage = Base64.getDecoder().decode(clubDto.getImage());
        club.setImage(decodedImage);

        club.setMembers(new ArrayList<>());
        club.setAnnouncements(new ArrayList<>());
        club.setEvents(new ArrayList<>());

        Club createdClub = clubService.createClub(club);
        return ResponseEntity.ok(createdClub);
    }

    @PostMapping("/join-club")
    public ResponseEntity<Club> joinClub(@RequestParam String clubId, @RequestParam String userId) {
        Club updatedClub = clubService.addMemberToClub(clubId, userId);
        return updatedClub != null ? ResponseEntity.ok(updatedClub) : ResponseEntity.notFound().build();
    }

    @PostMapping("/leave-club")
    public ResponseEntity<Club> leaveClub(@RequestParam String clubId, @RequestParam String userId) {
        Club updatedClub = clubService.removeMemberFromClub(clubId, userId);
        return updatedClub != null ? ResponseEntity.ok(updatedClub) : ResponseEntity.notFound().build();
    }

    @GetMapping("/get-club-info")
    public ResponseEntity<Club> getClubInfo(@RequestParam String clubId) {
        return clubRepository.findById(clubId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Club>> getAllClubs() {
        List<Club> clubs = clubRepository.findByIsApprovedTrue();
        return clubs.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(clubs);
    }

    @GetMapping("/get-all-pending-clubs")
    public ResponseEntity<List<Club>> getAllPendingClubs() {
        List<Club> pendingClubs = clubRepository.findByIsApprovedFalse();
        return pendingClubs.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(pendingClubs);
    }

    @PostMapping("/approvePending/{id}")
    public ResponseEntity<String> approvePendingClub(@PathVariable String id) {
        Club club = clubRepository.findById(id).orElse(null);

        if (club == null) {
            return ResponseEntity.notFound().build();
        }

        if (club.isApproved()) {
            return ResponseEntity.badRequest().body("Club is already approved.");
        }

        clubService.notifyApprovedClub(id);
        club.setApproved(true);
        clubRepository.save(club);

        return ResponseEntity.ok("Pending club approved successfully.");
    }

    @DeleteMapping("/deletePending/{id}")
    public ResponseEntity<String> deletePendingClub(@PathVariable String id) {
        Club club = clubRepository.findById(id).orElse(null);

        if (club == null) {
            return ResponseEntity.notFound().build();
        }

        if (club.isApproved()) {
            return ResponseEntity.badRequest().body("Cannot delete an already approved club.");
        }

        clubService.notifyRejectedClub(id);
        clubRepository.deleteById(id);
        return ResponseEntity.ok("Pending club deleted successfully.");
    }

    @GetMapping("/my-clubs")
    public ResponseEntity<List<Club>> getUserClubs(@RequestParam String userId) {
        List<Club> userClubs = clubRepository.findAll().stream()
                .filter(club -> club.getMembers().contains(userId))
                .toList();
        return userClubs.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(userClubs);
    }

    @GetMapping("/is-admin")
    public ResponseEntity<Boolean> isAdmin(@RequestParam String clubId, @RequestParam String userId) {
        boolean isAdmin = clubService.isAdmin(clubId, userId);
        return ResponseEntity.ok(isAdmin);
    }

    @PostMapping("/add-event")
    public ResponseEntity<Club> addEvent(@RequestParam String clubId, @RequestBody Event event) {
        Club updatedClub = clubService.addEventToClub(clubId, event);
        return updatedClub != null ? ResponseEntity.ok(updatedClub) : ResponseEntity.notFound().build();
    }

    @PostMapping("/add-announcement")
    public ResponseEntity<Club> addAnnouncement(@RequestParam String clubId, @RequestBody Announcments announcement) {
        Club updatedClub = clubService.addAnnouncementToClub(clubId, announcement);
        return updatedClub != null ? ResponseEntity.ok(updatedClub) : ResponseEntity.notFound().build();
    }

    @PostMapping("/join-event")
    public ResponseEntity<Club> joinEvent(@RequestParam String clubId, @RequestParam String eventId, @RequestParam String userId) {
        Club updatedClub = clubService.joinEvent(clubId, eventId, userId);
        return updatedClub != null ? ResponseEntity.ok(updatedClub) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{clubId}/events/{eventId}")
    public ResponseEntity<Event> getEvent(@PathVariable String clubId, @PathVariable String eventId) {
        return clubRepository.findById(clubId)
                .map(club -> club.getEvents().stream()
                        .filter(event -> event.getId().equals(eventId))
                        .findFirst()
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{clubId}/announcements")
    public ResponseEntity<List<Announcments>> getAnnouncements(@PathVariable String clubId) {
        return clubRepository.findById(clubId)
                .map(club -> {
                    List<Announcments> announcements = club.getAnnouncements();
                    Collections.reverse(announcements); // Reverse order
                    return ResponseEntity.ok(announcements);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/leave-event")
    public ResponseEntity<Club> leaveEvent(@RequestParam String clubId, @RequestParam String eventId, @RequestParam String userId) {
        Club updatedClub = clubService.leaveEvent(clubId, eventId, userId);
        return updatedClub != null ? ResponseEntity.ok(updatedClub) : ResponseEntity.notFound().build();
    }

    @PostMapping("/{clubId}/events/{eventId}/notify-members")
    public ResponseEntity<Void> notifyMembersOfApproachingEvent(@PathVariable String clubId, @PathVariable String eventId) {
        clubService.notifyMembersOfApproachingEvent(clubId, eventId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/is-member-of-club")
    public ResponseEntity<Boolean> isMemberOfClub(@RequestParam String clubId, @RequestParam String userId) {
        boolean isMember = clubService.isMemberOfClub(clubId, userId);
        return ResponseEntity.ok(isMember);
    }

    @GetMapping("/{clubId}/events/{eventId}/is-member")
    public ResponseEntity<Boolean> isMemberOfEvent(@PathVariable String clubId, @PathVariable String eventId, @RequestParam String userId) {
        boolean isMember = clubService.isMemberOfEvent(clubId, eventId, userId);
        return ResponseEntity.ok(isMember);
    }

    @GetMapping("/{clubId}/events/{eventId}/members")
    public ResponseEntity<List<String>> getEventMembers(@PathVariable String clubId, @PathVariable String eventId) {
        List<String> members = clubService.getEventMembers(clubId, eventId);
        return members.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(members);
    }

    @DeleteMapping("/{clubId}/events/{eventId}")
    public ResponseEntity<Club> deleteEvent(@PathVariable String clubId, @PathVariable String eventId) {
        Club updatedClub = clubService.deleteEvent(clubId, eventId);
        return updatedClub != null ? ResponseEntity.ok(updatedClub) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{clubId}")
    public ResponseEntity<Void> deleteClub(@PathVariable String clubId) {
        boolean isDeleted = clubService.deleteClub(clubId);
        return isDeleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{clubId}/feedback")
    public ResponseEntity<Club> addFeedback(@PathVariable String clubId, @RequestParam String userEmail, @RequestParam String feedbackText) {
        Club updatedClub = clubService.addFeedback(clubId, userEmail, feedbackText);
        return updatedClub != null ? ResponseEntity.ok(updatedClub) : ResponseEntity.notFound().build();
    }


}
