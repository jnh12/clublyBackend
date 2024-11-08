package jnh.dev.clublybackend.Events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Event {

    private String id;
    private String title;
    private int capacity;
    private List<String> members;
    private Date date;
    private String location;
    private boolean memberOnly;
    private String eventDescription;
    private String disclaimers;

    public Event(String title, int capacity, List<String> members, Date date, String location, boolean memberOnly, String eventDescription, String disclaimers) {
        this.title = title;
        this.capacity = capacity;
        this.members = members != null ? members : new ArrayList<>(); // Initialize if null
        this.date = date;
        this.location = location;
        this.memberOnly = memberOnly;
        this.eventDescription = eventDescription;
        this.disclaimers = disclaimers;
    }

}
