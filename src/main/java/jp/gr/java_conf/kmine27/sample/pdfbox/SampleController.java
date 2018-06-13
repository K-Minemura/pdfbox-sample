package jp.gr.java_conf.kmine27.sample.pdfbox;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

	@Autowired
	private ApplicationContext context;

	@RequestMapping(value = "basic", produces = MediaType.APPLICATION_PDF_VALUE)
	public Resource basic() throws InvalidPasswordException, IOException {

		Resource templateResource = context.getResource("classpath:pdf/basic.pdf");
		
		try (
				PDDocument template = PDDocument.load(templateResource.getFile());
				PDDocument doc = new PDDocument();
				) {
			PDPage page = copyPage(template.getPage(0));
			doc.addPage(page);
			writeContents(doc, page);
			File outputFile = File.createTempFile("basic", ".pdf");
			outputFile.deleteOnExit();
			doc.save(outputFile);
			return new FileSystemResource(outputFile);
		}
		
	}

	private void writeContents(PDDocument doc, PDPage page) throws IOException {
		try (
				PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.APPEND, false);
				) {
//			PDFont font = PDType1Font.HELVETICA_BOLD;

			PDFont font = PDType0Font.load(doc, context.getResource("classpath:font/ipagp.ttf").getFile());

			contentStream.beginText();
			contentStream.setFont(font, 12);
			contentStream.newLineAtOffset(100F, 100F);
			contentStream.showText("hello world ! こんにちは！髙橋さん");
			contentStream.endText();
			
			PDImageXObject image = PDImageXObject
					.createFromFileByContent(context.getResource("classpath:image/SpringBoot.png").getFile(), doc);
			contentStream.drawImage(image, 100F, 200F, image.getWidth() / 2, image.getHeight() / 2);
			
		}

	}

	private PDPage copyPage(PDPage page) {
		COSDictionary pageDict = page.getCOSObject();
		COSDictionary newPageDict = new COSDictionary(pageDict);

		newPageDict.removeItem(COSName.ANNOTS);

		PDPage newPage = new PDPage(newPageDict);
		return newPage;
	}
}
