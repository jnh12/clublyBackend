package jnh.dev.clublybackend.Clubs;

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

}
