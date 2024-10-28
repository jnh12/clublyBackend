package jnh.dev.clublybackend.Clubs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClubDto {
    private String name;
    private String description;
    private String category;
    private String userId;
    private String image; // Base64 encoded image
}
