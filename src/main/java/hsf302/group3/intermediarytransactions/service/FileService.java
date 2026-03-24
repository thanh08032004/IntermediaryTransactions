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
        if (file == null || file.isEmpty()) return null;

        try {
            // tạo folder nếu chưa có
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            // tạo tên file unique
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            File dest = new File(UPLOAD_DIR + fileName);

            // lưu file
            file.transferTo(dest);

            // trả về URL để hiển thị
            return "/uploads/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Upload file failed", e);
        }
    }
}