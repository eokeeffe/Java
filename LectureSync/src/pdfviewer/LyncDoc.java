package pdfviewer;

import java.io.IOException;

import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;

public class LyncDoc {

	private String filePath;
	private Document document;

	public LyncDoc(String filePath) {
		this.filePath = filePath;
		document = new Document();
		try {
			document.setFile(filePath);
		} catch (PDFException e) {
			e.printStackTrace();
		} catch (PDFSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Document getDocument() {
		return document;
	}
	
	public String getFilePath() {
		return filePath;
	}
}
