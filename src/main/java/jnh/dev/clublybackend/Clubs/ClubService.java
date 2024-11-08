package jnh.dev.clublybackend.Clubs;

import jnh.dev.clublybackend.Events.Announcments;
import jnh.dev.clublybackend.Events.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ClubService {
    @Autowired
    private ClubRepository clubRepository;

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
                return clubRepository.save(club);
            }
            return club;
        }).orElse(null);
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
            return clubRepository.save(club);
        }
        return null;
    }

    public Club joinEvent(String clubId, String eventId, String userId) {
        Optional<Club> clubOptional = clubRepository.findById(clubId);
        if (clubOptional.isPresent()) {
            Club club = clubOptional.get();

            boolean isUserMember = club.getMembers().contains(userId);

            for (Event event : club.getEvents()) {
                if (eventId.equals(event.getId()) && event.getCapacity() > 0) {
                    if (event.isMemberOnly() && !isUserMember) {
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





}
