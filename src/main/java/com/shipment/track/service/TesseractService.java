package com.shipment.track.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import java.io.File;
import java.io.IOException;

public class TesseractService {

    public String extractTextFromImage(String imagePath) {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata"); // Path to the Tesseract language files
        try {
            File imgFile = new File(imagePath);
            return tesseract.doOCR(imgFile);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
