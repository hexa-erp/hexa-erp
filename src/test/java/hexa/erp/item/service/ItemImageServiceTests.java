package hexa.erp.item.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import lombok.extern.log4j.Log4j;

@Log4j
public class ItemImageServiceTests {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	// 画像の保存と取得を確認
	@Test
	public void testStoreAndGetFile() throws Exception {
		ItemImageServiceImpl service = createService();

		MockMultipartFile imageFile = createImageFile("item.png");

		log.info("========== IMAGE STORE TEST ==========");
		log.info("ORIGINAL FILE NAME: " + imageFile.getOriginalFilename());
		log.info("FILE SIZE: " + imageFile.getSize());

		String imagePath = service.store(imageFile);

		File original = service.getFile(imagePath, false);

		File thumbnail = service.getFile(imagePath, true);

		log.info("IMAGE PATH: " + imagePath);
		log.info("ORIGINAL FILE: " + original.getAbsolutePath());
		log.info("THUMBNAIL FILE: " + thumbnail.getAbsolutePath());

		assertNotNull(imagePath);
		assertTrue(original.isFile());
		assertTrue(thumbnail.isFile());

		service.delete(imagePath);

		log.info("ORIGINAL EXISTS AFTER DELETE: " + original.exists());
		log.info("THUMBNAIL EXISTS AFTER DELETE: " + thumbnail.exists());

		assertFalse(original.exists());
		assertFalse(thumbnail.exists());
	}

	// 不正な拡張子を拒否することを確認
	@Test(expected = IOException.class)
	public void testInvalidExtension() throws Exception {
		ItemImageServiceImpl service = createService();

		MockMultipartFile textFile = new MockMultipartFile("imageFile", "item.txt", "text/plain",
				"not image".getBytes("UTF-8"));

		log.info("========== IMAGE EXTENSION TEST ==========");
		log.info("ORIGINAL FILE NAME: " + textFile.getOriginalFilename());

		service.store(textFile);
	}

	// 画像Serviceを生成
	private ItemImageServiceImpl createService() throws IOException {

		ItemImageServiceImpl service = new ItemImageServiceImpl();

		ReflectionTestUtils.setField(service, "imageDirectory", temporaryFolder.newFolder("items").getAbsolutePath());

		return service;
	}

	// テスト画像を生成
	private MockMultipartFile createImageFile(String fileName) throws IOException {

		BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);

		ByteArrayOutputStream output = new ByteArrayOutputStream();

		ImageIO.write(image, "png", output);

		return new MockMultipartFile("imageFile", fileName, "image/png", output.toByteArray());
	}
}
