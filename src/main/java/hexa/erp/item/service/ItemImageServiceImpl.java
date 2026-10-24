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

	private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

	@Value("${item.image-directory}")
	private String imageDirectory;

	// 品目画像とサムネイルを保存する。
	@Override
	public String store(MultipartFile imageFile) throws IOException {

		if (imageFile == null || imageFile.isEmpty()) {
			throw new IOException("画像ファイルを選択してください。");
		}

		if (imageFile.getSize() > MAX_FILE_SIZE) {
			throw new IOException("登録できる画像は5MB以下です。");
		}

		String originalName = imageFile.getOriginalFilename();
		int extensionIndex = originalName == null ? -1 : originalName.lastIndexOf('.');

		String extension = extensionIndex < 0 ? ""
				: originalName.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);

		if (!"jpg".equals(extension) && !"jpeg".equals(extension) && !"png".equals(extension)
				&& !"gif".equals(extension)) {

			throw new IOException("登録できる画像形式はJPG、JPEG、PNG、GIFのみです。");
		}

		BufferedImage image;

		try (InputStream input = imageFile.getInputStream()) {
			image = ImageIO.read(input);
		}

		if (image == null) {
			throw new IOException("画像ファイルを読み込めません。");
		}

		File directory = new File(imageDirectory).getCanonicalFile();

		if (!directory.isDirectory() && !directory.mkdirs() && !directory.isDirectory()) {

			throw new IOException("画像の保存フォルダーを作成できません。");
		}

		String imagePath = UUID.randomUUID().toString() + "." + extension;

		File original = resolveFile(imagePath);
		File thumbnail = resolveFile(thumbnailName(imagePath));

		try {
			imageFile.transferTo(original);

			Thumbnails.of(image).size(160, 160).outputFormat("png").toFile(thumbnail);

			return imagePath;

		} catch (IOException | RuntimeException e) {
			delete(imagePath);

			throw new IOException("画像ファイルを保存できませんでした。", e);
		}
	}

	// 元画像またはサムネイルを取得する。
	@Override
	public File getFile(String imagePath, boolean thumbnail) throws IOException {

		File original = resolveFile(imagePath);

		if (thumbnail) {
			File thumbnailFile = resolveFile(thumbnailName(imagePath));

			if (thumbnailFile.isFile()) {
				return thumbnailFile;
			}
		}

		if (!original.isFile()) {
			throw new FileNotFoundException("画像ファイルが見つかりません。");
		}

		return original;
	}

	// 元画像とサムネイルを削除する。
	@Override
	public void delete(String imagePath) {
		if (imagePath == null || imagePath.trim().isEmpty()) {
			return;
		}

		deleteFile(imagePath);
		deleteFile(thumbnailName(imagePath));
	}

	// 画像ファイル1件を削除する。
	private void deleteFile(String fileName) {
		try {
			File file = resolveFile(fileName);

			if (Files.isRegularFile(file.toPath(), LinkOption.NOFOLLOW_LINKS)) {

				Files.deleteIfExists(file.toPath());
			}

		} catch (IOException | RuntimeException e) {
			log.warn("品目画像ファイルを削除できませんでした: " + fileName, e);
		}
	}

	// 画像パスの安全性を確認する。
	private File resolveFile(String fileName) throws IOException {

		if (fileName == null || fileName.trim().isEmpty() || fileName.contains("/") || fileName.contains("\\")
				|| fileName.contains(":") || ".".equals(fileName) || "..".equals(fileName)) {

			throw new FileNotFoundException("画像のパスが正しくありません。");
		}

		File directory = new File(imageDirectory).getCanonicalFile();

		File file = new File(directory, fileName);

		if (!directory.equals(file.getCanonicalFile().getParentFile())) {

			throw new FileNotFoundException("画像のパスが正しくありません。");
		}

		return file;
	}

	// サムネイルのファイル名を生成する。
	private String thumbnailName(String imagePath) {
		int extensionIndex = imagePath.lastIndexOf('.');

		String baseName = extensionIndex < 0 ? imagePath : imagePath.substring(0, extensionIndex);

		return "s_" + baseName + ".png";
	}
}
