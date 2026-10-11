package hexa.erp.item.service;

import java.io.File;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface ItemImageService {

    // 품목 이미지 저장
    String store(MultipartFile imageFile) throws IOException;

    // 품목 이미지 조회
    File getFile(
        String imagePath,
        boolean thumbnail
    ) throws IOException;

    // 품목 이미지 삭제
    void delete(String imagePath);
}
