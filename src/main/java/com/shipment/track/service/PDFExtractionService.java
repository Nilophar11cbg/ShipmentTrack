package com.shipment.track.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;

@Service
public class PDFExtractionService {

    @Autowired
    private OpenNLPService openNLPService;

    public Map<String, String> extractDataFromPDF(File pdfFile) throws Exception {
        String text = PDFTextExtractor.extractText(pdfFile);
        
        return openNLPService.extractEntities(text);
    }
}
