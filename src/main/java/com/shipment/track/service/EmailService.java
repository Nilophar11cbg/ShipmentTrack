package com.shipment.track.service;

import javax.mail.*;
import javax.mail.search.FlagTerm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.shipment.track.controller.ShipmentDetailsController;
import com.shipment.track.model.ShipmentDetails;
import com.shipment.track.repository.ShipmentDetailsRepository;

import io.micrometer.common.util.StringUtils;

@Service
public class EmailService {

	@Value("${email.host}")
	private String host;

	@Value("${email.port}")
	private String port;

	@Value("${email.username}")
	private String username;

	@Value("${email.password}")
	private String password;

	@Value("${email.protocol}")
	private String protocol;

	@Autowired
	ShipmentDetailsRepository repository;
	
	@Autowired
	PDFExtractionService pdfExtractionService;
	
	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

	public void fetchEmails() {
		logger.info("Reading of emails started ");
		Properties props = new Properties();
		props.put("mail.store.protocol", protocol);
		props.put("mail.imap.host", host);
		props.put("mail.imap.port", port);
		props.put("mail.imap.ssl.enable", "true");
		props.put("mail.imap.starttls.enable", "true");

		Session session = Session.getInstance(props);
		try {
			Store store = session.getStore("imap");
			store.connect(username, password);
			Folder inbox = store.getFolder("INBOX");
			inbox.open(Folder.READ_ONLY);
			
			// Fetch unread emails
			Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));


			for (Message message : messages) {
				if (message.isMimeType("multipart/*")) {
					Multipart multipart = (Multipart) message.getContent();
					for (int i = 0; i < multipart.getCount(); i++) {
						BodyPart bodyPart = multipart.getBodyPart(i);
						String disposition = bodyPart.getDisposition();
						if (BodyPart.ATTACHMENT.equalsIgnoreCase(disposition)) {
							handleAttachment(bodyPart);
						}
					}
				}
			}

			inbox.close(false);
			store.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	 private void handleAttachment(BodyPart bodyPart) {
	        try {
	            String filename = bodyPart.getFileName();
	            InputStream is = bodyPart.getInputStream();

	            if (filename.endsWith(".pdf")) {
	            	logger.info("Email contains attachment as PDF " + filename);
	                File tempFile = File.createTempFile("attachment", ".pdf");
	                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
	                    byte[] buffer = new byte[1024];
	                    int bytesRead;
	                    while ((bytesRead = is.read(buffer)) != -1) {
	                        fos.write(buffer, 0, bytesRead);
	                    }
	                }
	                extractDataFromPDF(tempFile);

	                tempFile.delete();
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	 /**
	  * Extracts contents from PDF
	  * @param pdfFile
	  */
	    private void extractDataFromPDF(File pdfFile) {
	        try {
	        	logger.info("Extraction started for PDF....");
	            Map<String, String> extractedData = pdfExtractionService.extractDataFromPDF(pdfFile);

	            ShipmentDetails details = new ShipmentDetails();
                 details.setSenderAddress(extractedData.get("SOLDBY"));
	            details.setReceiverAddress(extractedData.get("SHIPPINGADDRESS"));
	         //   details.setTrackingNumber(extractedData.get("INVOICEDETAILS"));
	            details.setOrderId(extractedData.get("ORDERNUMBER"));
	            String status = extractedData.get("STATUS");
	            if(StringUtils.isNotEmpty(status))
	            	details.setStatus(status.toUpperCase());

	            String dueDate = extractedData.get("DUEDATE");
	            if (dueDate != null && !dueDate.isEmpty()) {
	                dueDate = dueDate.replaceAll("[^\\d-]", "").trim(); 
	                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	                try {
	                    java.util.Date parsedDate = sdf.parse(dueDate);
	                    java.sql.Date sqlDueDate = new java.sql.Date(parsedDate.getTime());
	                    details.setShipmentDate(sqlDueDate);
	                } catch (java.text.ParseException e) {
	                	logger.error("Failed to parse due date " + dueDate +"with error " + e.getMessage());
	                }
	            }
	            logger.info("Extraction completed for PDF....");
	            
	            repository.save(details);
	            
	            logger.info("PDF contents saved to database");
	        } catch (Exception e) {
	        	logger.error("Exception occured in extracting data from PDF " + e.getMessage());
	        }
	    }
}
