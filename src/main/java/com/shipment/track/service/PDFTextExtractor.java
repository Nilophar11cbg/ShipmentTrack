package com.shipment.track.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class PDFTextExtractor {

	public static String extractText(File pdfFile) throws IOException {
	    try (PDDocument document = PDDocument.load(pdfFile)) {
	        PDFTextStripper stripper = new PDFTextStripper();
	        String text = stripper.getText(document);
	        return text;
	    }
	}

}
