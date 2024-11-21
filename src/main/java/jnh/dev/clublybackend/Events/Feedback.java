package jnh.dev.clublybackend.Events;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Feedback {

    private int id;
    private String feedback;
    private String userEmail;

}
