package jnh.dev.clublybackend.Clubs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ClubService {

    // Your Club repository
    @Autowired
    private ClubRepository clubRepository;

    public String uploadImage(MultipartFile file) throws IOException {
        // Define the directory to save the uploaded image
        String directory = "src/main/resources/images"; // Update this to your desired path
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs(); // Create the directory if it does not exist
        }

        // Construct the file path and save the file
        String filePath = directory + "/" + file.getOriginalFilename();
        Path path = Paths.get(filePath);
        Files.copy(file.getInputStream(), path);

        return "/images/" + file.getOriginalFilename();
    }
}

