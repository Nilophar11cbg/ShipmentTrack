package com.shipment.track.service;

import opennlp.tools.namefind.*;
import opennlp.tools.util.Span;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class OpenNLPService {

	private static final Logger logger = LoggerFactory.getLogger(OpenNLPService.class);
    private TokenNameFinderModel model;

    public OpenNLPService() throws IOException {
        // Load the model from an absolute path
        File modelFile = new File("C:/openNLP/data/en-field-model.bin");
        this.model = new TokenNameFinderModel(new FileInputStream(modelFile));
    }

    /**
     * 
     * @param text
     * @return
     */
    public Map<String, String> extractEntities(String text) {
        NameFinderME nameFinder = new NameFinderME(model);
        String[] tokens = text.split("\\s+");
        
        logger.info("Tokens Identified : " +  Arrays.toString(tokens));
        
        Span[] spans = nameFinder.find(tokens);

        Map<String, String> extractedData = new HashMap<>();
        for (Span span : spans) {
            StringBuilder entityValue = new StringBuilder();
            for (int i = span.getStart(); i < span.getEnd(); i++) {
                entityValue.append(tokens[i]).append(" ");
            }
            extractedData.put(span.getType(), entityValue.toString().trim());
        }
        nameFinder.clearAdaptiveData();
        logger.info("Extracted Entities: " + extractedData);
        return extractedData;
    }
}
