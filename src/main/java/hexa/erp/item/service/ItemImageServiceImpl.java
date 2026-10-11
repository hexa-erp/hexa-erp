package hexa.erp.item.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.Locale;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.log4j.Log4j;
import net.coobird.thumbnailator.Thumbnails;

@Service
@Log4j
public class ItemImageServiceImpl implements ItemImageService {

    private static final long MAX_FILE_SIZE =
        5L * 1024 * 1024;

    @Value("${item.image-directory}")
    private String imageDirectory;

    // 품목 이미지와 썸네일 저장
    @Override
    public String store(MultipartFile imageFile)
            throws IOException {

        if (imageFile == null || imageFile.isEmpty()) {
            throw new IOException(
                "사진 파일을 선택해 주세요."
            );
        }

        if (imageFile.getSize() > MAX_FILE_SIZE) {
            throw new IOException(
                "사진은 5MB 이하만 등록할 수 있습니다."
            );
        }

        String originalName = imageFile.getOriginalFilename();
        int extensionIndex = originalName == null
            ? -1
            : originalName.lastIndexOf('.');

        String extension = extensionIndex < 0
            ? ""
            : originalName
                .substring(extensionIndex + 1)
                .toLowerCase(Locale.ROOT);

        if (!"jpg".equals(extension)
                && !"jpeg".equals(extension)
                && !"png".equals(extension)
                && !"gif".equals(extension)) {

            throw new IOException(
                "JPG, JPEG, PNG, GIF 사진만 등록할 수 있습니다."
            );
        }

        BufferedImage image;

        try (InputStream input = imageFile.getInputStream()) {
            image = ImageIO.read(input);
        }

        if (image == null) {
            throw new IOException(
                "읽을 수 없는 사진 파일입니다."
            );
        }

        File directory =
            new File(imageDirectory).getCanonicalFile();

        if (!directory.isDirectory()
                && !directory.mkdirs()
                && !directory.isDirectory()) {

            throw new IOException(
                "사진 저장 폴더를 만들 수 없습니다."
            );
        }

        String imagePath =
            UUID.randomUUID().toString()
                + "."
                + extension;

        File original = resolveFile(imagePath);
        File thumbnail =
            resolveFile(thumbnailName(imagePath));

        try {
            imageFile.transferTo(original);

            Thumbnails.of(image)
                .size(160, 160)
                .outputFormat("png")
                .toFile(thumbnail);

            return imagePath;

        } catch (IOException | RuntimeException e) {
            delete(imagePath);

            throw new IOException(
                "사진 파일을 저장하지 못했습니다.",
                e
            );
        }
    }

    // 원본 또는 썸네일 조회
    @Override
    public File getFile(
            String imagePath,
            boolean thumbnail)
            throws IOException {

        File original = resolveFile(imagePath);

        if (thumbnail) {
            File thumbnailFile =
                resolveFile(thumbnailName(imagePath));

            if (thumbnailFile.isFile()) {
                return thumbnailFile;
            }
        }

        if (!original.isFile()) {
            throw new FileNotFoundException(
                "사진 파일을 찾을 수 없습니다."
            );
        }

        return original;
    }

    // 원본과 썸네일 삭제
    @Override
    public void delete(String imagePath) {
        if (imagePath == null
                || imagePath.trim().isEmpty()) {
            return;
        }

        deleteFile(imagePath);
        deleteFile(thumbnailName(imagePath));
    }

    // 단일 이미지 파일 삭제
    private void deleteFile(String fileName) {
        try {
            File file = resolveFile(fileName);

            if (Files.isRegularFile(
                    file.toPath(),
                    LinkOption.NOFOLLOW_LINKS)) {

                Files.deleteIfExists(file.toPath());
            }

        } catch (IOException | RuntimeException e) {
            log.warn(
                "품목 사진 파일을 삭제하지 못했습니다: "
                    + fileName,
                e
            );
        }
    }

    // 안전한 이미지 경로 확인
    private File resolveFile(String fileName)
            throws IOException {

        if (fileName == null
                || fileName.trim().isEmpty()
                || fileName.contains("/")
                || fileName.contains("\\")
                || fileName.contains(":")
                || ".".equals(fileName)
                || "..".equals(fileName)) {

            throw new FileNotFoundException(
                "올바르지 않은 사진 경로입니다."
            );
        }

        File directory =
            new File(imageDirectory).getCanonicalFile();

        File file = new File(directory, fileName);

        if (!directory.equals(
                file.getCanonicalFile().getParentFile())) {

            throw new FileNotFoundException(
                "올바르지 않은 사진 경로입니다."
            );
        }

        return file;
    }

    // 썸네일 파일명 생성
    private String thumbnailName(String imagePath) {
        int extensionIndex = imagePath.lastIndexOf('.');

        String baseName = extensionIndex < 0
            ? imagePath
            : imagePath.substring(0, extensionIndex);

        return "s_" + baseName + ".png";
    }
}
