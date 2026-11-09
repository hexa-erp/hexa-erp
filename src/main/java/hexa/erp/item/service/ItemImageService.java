package hexa.erp.item.service;

import java.io.File;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface ItemImageService {

	// 品目画像を保存する。
	String store(MultipartFile imageFile) throws IOException;

	// 品目画像を取得する。
	File getFile(String imagePath, boolean thumbnail) throws IOException;

	// 品目画像を削除する。
	void delete(String imagePath);
}
