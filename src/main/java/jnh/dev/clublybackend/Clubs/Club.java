package jnh.dev.clublybackend.Clubs;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
@Document(collection = "Clubs")
public class Club {
    @Id
    private String id;
    private String name;
    private String description;
    private String category;
    private String image;
    private List<String> adminIds;
    private List<String> members;
    private List<String> announcements;
    private List<String> events;


    public Club() {
    }

    ;

    public Club(String name, String description, String category, String image, String adminId) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.image = image;
        this.adminIds = new ArrayList<>();
        this.adminIds.add(adminId);  // Add the creator as the first admin
        this.members = new ArrayList<>();
        this.members.add(adminId);   // Add the creator as the first member
        this.announcements = new ArrayList<>();
        this.events = new ArrayList<>();
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getAdminIds() {
        return adminIds;
    }

    public void setAdminIds(List<String> adminIds) {
        this.adminIds = adminIds;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public List<String> getAnnouncements() {
        return announcements;
    }

    public void setAnnouncements(List<String> announcements) {
        this.announcements = announcements;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }

}

