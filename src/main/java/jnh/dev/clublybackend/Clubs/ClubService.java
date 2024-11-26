package jnh.dev.clublybackend.Clubs;

import jnh.dev.clublybackend.Email.EmailService;
import jnh.dev.clublybackend.Events.Announcments;
import jnh.dev.clublybackend.Events.Event;
import jnh.dev.clublybackend.Events.Feedback;
import jnh.dev.clublybackend.User.User;
import jnh.dev.clublybackend.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.stream.Collectors;


@Service
public class ClubService {

    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserRepository userRepository;
    private static final List<String> SUPER_USER_IDS = List.of("671fc51edf0c575d9790f454");

    public Club createClub(Club club) {
        if (club.getAdminIds() == null) club.setAdminIds(new ArrayList<>());
        if (club.getMembers() == null) club.setMembers(new ArrayList<>());
        if (club.getAnnouncements() == null) club.setAnnouncements(new ArrayList<>());
        if (club.getEvents() == null) club.setEvents(new ArrayList<>());

        return clubRepository.save(club);
    }

    public Club addMemberToClub(String clubId, String userId) {
        return clubRepository.findById(clubId).map(club -> {
            if (!club.getMembers().contains(userId)) {
                club.getMembers().add(userId);
                Club updatedClub = clubRepository.save(club);

                Optional<User> userOptional = userRepository.findById(userId);
                userOptional.ifPresent(user -> {
                    String userEmail = user.getEmail();
                    String clubName = club.getName();

                    emailService.sendJoinedEmail(userEmail, clubName);
                });

                return updatedClub;
            }
            return club;
        }).orElse(null);
    }

    public void notifyMembersOfApproachingEvent(String clubId, String eventId) {
        clubRepository.findById(clubId).ifPresent(club -> {
            String clubName = club.getName();

            club.getEvents().stream()
                    .filter(event -> event.getId().equals(eventId))
                    .findFirst()
                    .ifPresent(event -> {
                        Date eventDate = event.getDate();

                        event.getMembers().forEach(memberId -> {
                            userRepository.findById(memberId).ifPresent(user -> {
                                String userEmail = user.getEmail();
                                emailService.sendApproachingEmail(userEmail, clubName, eventDate);
                            });
                        });
                    });
        });
    }

    public Club removeMemberFromClub(String clubId, String userId) {
        return clubRepository.findById(clubId).map(club -> {
            if (club.getMembers().contains(userId)) {
                club.getMembers().remove(userId);
                return clubRepository.save(club);
            }
            return club;
        }).orElse(null);
    }

    public boolean isAdmin(String clubId, String userId) {
        return clubRepository.findById(clubId)
                .map(club -> club.getAdminIds().contains(userId))
                .orElse(false);
    }

    public Club addEventToClub(String clubId, Event event) {
        Optional<Club> clubOptional = clubRepository.findById(clubId);
        if (clubOptional.isPresent()) {
            Club club = clubOptional.get();
            if (club.getEvents() == null) {
                club.setEvents(new ArrayList<>());
            }
            event.setId(String.valueOf(club.getEvents().size() + 1));

            club.getEvents().add(event);
            return clubRepository.save(club);
        }
        return null;
    }

    public Club addAnnouncementToClub(String clubId, Announcments announcement) {
        Optional<Club> clubOptional = clubRepository.findById(clubId);
        if (clubOptional.isPresent()) {
            Club club = clubOptional.get();
            if (club.getAnnouncements() == null) {
                club.setAnnouncements(new ArrayList<>());
            }
            club.getAnnouncements().add(announcement);
            notifyMembersOfNewAnnouncement(clubId, announcement.getAnnouncement());
            return clubRepository.save(club);
        }
        return null;
    }

    public Club joinEvent(String clubId, String eventId, String userId) {
        Optional<Club> clubOptional = clubRepository.findById(clubId);
        if (clubOptional.isPresent()) {
            Club club = clubOptional.get();

            boolean isUserMemberOrAdmin = club.getMembers().contains(userId) || club.getAdminIds().contains(userId);

            for (Event event : club.getEvents()) {
                if (eventId.equals(event.getId()) && event.getCapacity() > 0) {
                    if (event.isMemberOnly() && !isUserMemberOrAdmin) {
                        return null;
                    }

                    if (event.getMembers() == null) {
                        event.setMembers(new ArrayList<>());
                    }
                    if (!event.getMembers().contains(userId)) {
                        event.getMembers().add(userId);
                        event.setCapacity(event.getCapacity() - 1);
                        return clubRepository.save(club);
                    }
                }
            }
        }
        return null;
    }

    public Club leaveEvent(String clubId, String eventId, String userId) {
        Optional<Club> clubOptional = clubRepository.findById(clubId);
        if (clubOptional.isPresent()) {
            Club club = clubOptional.get();

            for (Event event : club.getEvents()) {
                if (eventId.equals(event.getId()) && event.getMembers() != null) {
                    if (event.getMembers().contains(userId)) {
                        event.getMembers().remove(userId);
                        event.setCapacity(event.getCapacity() + 1);
                        return clubRepository.save(club);
                    }
                }
            }
        }
        return null;
    }

    public boolean isMemberOfClub(String clubId, String userId) {
        return clubRepository.findById(clubId)
                .map(club -> club.getMembers().contains(userId))
                .orElse(false);
    }

    public boolean isMemberOfEvent(String clubId, String eventId, String userId) {
        return clubRepository.findById(clubId)
                .map(club -> club.getEvents().stream()
                        .filter(event -> event.getId().equals(eventId))
                        .anyMatch(event -> event.getMembers() != null && event.getMembers().contains(userId)))
                .orElse(false);
    }

    public List<String> getEventMembers(String clubId, String eventId) {
        Optional<Club> clubOptional = clubRepository.findById(clubId);
        if (clubOptional.isPresent()) {
            Club club = clubOptional.get();
            List<String> memberIds = club.getEvents().stream()
                    .filter(event -> event.getId().equals(eventId))
                    .findFirst()
                    .map(Event::getMembers)
                    .orElse(Collections.emptyList());

            return memberIds.stream()
                    .map(userId -> userRepository.findById(userId))
                    .filter(Optional::isPresent)
                    .map(userOpt -> userOpt.get().getEmail()) // Assuming getEmail() is a method in your User class
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public Club deleteEvent(String clubId, String eventId) {
        Optional<Club> clubOptional = clubRepository.findById(clubId);
        if (clubOptional.isPresent()) {
            Club club = clubOptional.get();
            club.getEvents().removeIf(event -> event.getId().equals(eventId));
            return clubRepository.save(club);
        }
        return null;
    }

    public boolean deleteClub(String clubId) {
        Optional<Club> clubOptional = clubRepository.findById(clubId);
        if (clubOptional.isPresent()) {
            clubRepository.deleteById(clubId);
            return true; // Successfully deleted
        }
        return false; // Club not found
    }

    public void notifyApprovedClub(String clubId) {
        clubRepository.findById(clubId).ifPresent(club -> {
            club.getAdminIds().forEach(adminId -> {
                userRepository.findById(adminId).ifPresent(admin -> {
                    String adminEmail = admin.getEmail();
                    emailService.sendApprovedEmail(adminEmail, club.getName());
                });
            });
        });
    }

    public void notifyRejectedClub(String clubId) {
        clubRepository.findById(clubId).ifPresent(club -> {
            club.getAdminIds().forEach(adminId -> {
                userRepository.findById(adminId).ifPresent(admin -> {
                    String adminEmail = admin.getEmail();
                    emailService.sendRejectedEmail(adminEmail, club.getName());
                });
            });
        });
    }

    public void notifyMembersOfNewAnnouncement(String clubId, String announcementText) {
        clubRepository.findById(clubId).ifPresent(club -> {
            club.getMembers().forEach(memberId -> {
                userRepository.findById(memberId).ifPresent(user -> {
                    String userEmail = user.getEmail();
                    emailService.sendAnnouncement(userEmail, club.getName(), announcementText);
                });
            });
        });
    }

    public Club addFeedback(String clubId, String userEmail, String feedbackText) {
        Optional<Club> clubOptional = clubRepository.findById(clubId);
        if (clubOptional.isPresent()) {
            Club club = clubOptional.get();

            if (club.getFeedback() == null) {
                club.setFeedback(new ArrayList<>());
            }

            Feedback feedback = new Feedback();
            feedback.setId(club.getFeedback().size() + 1);
            feedback.setUserEmail(userEmail);
            feedback.setFeedback(feedbackText);

            club.getFeedback().add(feedback);
            return clubRepository.save(club);
        }
        return null;
    }

    public Club removeFeedback(String clubId, int feedbackId) {
        Optional<Club> clubOptional = clubRepository.findById(clubId);
        if (clubOptional.isPresent()) {
            Club club = clubOptional.get();

            if (club.getFeedback() != null) {
                club.getFeedback().removeIf(feedback -> feedback.getId() == feedbackId);
                return clubRepository.save(club);
            }
        }
        return null;
    }

    public boolean isSuperUser(String userId) {
        return SUPER_USER_IDS.contains(userId);
    }

    public String getUserEmailById(String userId) {
        return userRepository.findById(userId)
                .map(User::getEmail)
                .orElse(null);
    }

    public String getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElse(null);
    }

}
