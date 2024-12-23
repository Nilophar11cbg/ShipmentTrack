package com.shipment.track.service;

import javax.mail.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Properties;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.shipment.track.model.ShipmentDetails;
import com.shipment.track.repository.ShipmentDetailsRepository;

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

	public void fetchEmails() {
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

			Message[] messages = inbox.getMessages();
			System.out.println("Messages available in Inbox " + messages.length);


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

	    private void extractDataFromPDF(File pdfFile) {
	        try {
	            System.out.println("Extraction started for PDF....");
	            Map<String, String> extractedData = pdfExtractionService.extractDataFromPDF(pdfFile);

	            ShipmentDetails details = new ShipmentDetails();
                 details.setSenderAddress(extractedData.get("SOLDBY"));
	            details.setReceiverAddress(extractedData.get("SHIPPINGADDRESS"));
	            details.setTrackingNumber(extractedData.get("INVOICEDETAILS"));
	            details.setOrderId(extractedData.get("ORDERNUMBER"));
	            details.setStatus(extractedData.get("STATUS"));

	            String dueDate = extractedData.get("DUEDATE");
	            if (dueDate != null && !dueDate.isEmpty()) {
	                dueDate = dueDate.replaceAll("[^\\d-]", "").trim(); 
	                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	                try {
	                    java.util.Date parsedDate = sdf.parse(dueDate);
	                    java.sql.Date sqlDueDate = new java.sql.Date(parsedDate.getTime());
	                    details.setShipmentDate(sqlDueDate);
	                } catch (java.text.ParseException e) {
	                    System.err.println("Failed to parse due date: " + dueDate);
	                    e.printStackTrace();
	                }
	            }

	            repository.save(details);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
}
