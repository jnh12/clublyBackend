package jnh.dev.clublybackend.Clubs;

import jnh.dev.clublybackend.Events.Announcments;
import jnh.dev.clublybackend.Events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Clubs")
public class Club {
    @Id
    private String id;
    private String name;
    private String description;
    private String category;
    private byte[] image;
    private List<String> adminIds;
    private List<String> members;
    private List<Announcments> announcements;
    private List<Event> events;
    private boolean isApproved;

}