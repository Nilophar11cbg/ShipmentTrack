package com.shipment.track.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PdfToImageExtractor {
    public static void extractImagesFromPdf(String pdfPath, String imageDir) throws IOException {
        File pdfFile = new File(pdfPath);
        PDDocument document = PDDocument.load(pdfFile);
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        int numberOfPages = document.getNumberOfPages();
        for (int i = 0; i < numberOfPages; i++) {
            BufferedImage image = pdfRenderer.renderImage(i);
            File imageFile = new File(imageDir + "/page_" + i + ".png");
            javax.imageio.ImageIO.write(image, "PNG", imageFile);
        }
        document.close();
    }
}
