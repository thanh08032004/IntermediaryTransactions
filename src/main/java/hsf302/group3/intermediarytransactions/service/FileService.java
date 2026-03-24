package hsf302.group3.intermediarytransactions.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileService {

    private static final String UPLOAD_DIR = "uploads/";

    public String upload(MultipartFile file) {
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/";

            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            file.transferTo(new File(uploadDir + fileName));

            return "/images/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}