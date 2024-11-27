package com.shipment.track.service;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.shipment.track.model.Constants;
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
			// Process the attachment based on its type (PDF, DOCX, Image)
			if (filename.endsWith(".pdf")) {
				extractTextFromPDF(is);
			}
			/*
			 * else if (filename.endsWith(".jpg") || filename.endsWith(".png")) {
			 * extractTextFromImage(is); } else if (filename.endsWith(".docx")) {
			 * extractTextFromWord(is); }
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param is
	 */
	private void extractTextFromPDF(InputStream is) {
		try {
			System.out.println("Extraction started for PDF....");
			Map<String, String> keyValueMap = extractKeyValuePairs(is);

			// insert the data
			ShipmentDetails details = new ShipmentDetails();
			details.setSenderAddress(keyValueMap.get("From"));
			details.setReceiverAddress(keyValueMap.get("To"));
			// details.setTrackingNumber(keyValueMap.get("Shipment Number"));
			details.setOrderId(keyValueMap.get("Order Number"));
			details.setStatus(keyValueMap.get("Status"));
			String dueDate = keyValueMap.get("Due Date");
			System.out.println("Due date=> " + dueDate);
			
			/*
			 * if(StringUtils.isNotEmpty(dueDate)) { //DateTimeFormatter formatter =
			 * DateTimeFormatter.ofPattern("dd-MM-YYYY");
			 * 
			 * SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-YYYY"); java.util.Date
			 * date = sdf1.parse(dueDate); java.sql.Date sqlStartDate = new
			 * java.sql.Date(date.getTime());
			 * 
			 * details.setShipmentDate(sqlStartDate); }
			 */
			repository.save(details);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Map<String, String> extractKeyValuePairs(InputStream is) {
		Map<String, String> keyValuePairs = new HashMap<>();

		try (PDDocument document = PDDocument.load(is)) {
			PDFTextStripper pdfStripper = new PDFTextStripper();
			String text = pdfStripper.getText(document);

			// Extract data using regex
			/*
			 * keyValuePairs.put("From", extractPattern(text, Constants.FROM_REGEX));
			 * keyValuePairs.put("To", extractPattern(text, Constants.TO_REGEX));
			 * keyValuePairs.put("Shipment Number", extractPattern(text,
			 * Constants.SHIPMENT_NUMBER_REGEX)); keyValuePairs.put("Order Number",
			 * extractPattern(text, Constants.ORDER_NUMBER_REGEX));
			 * keyValuePairs.put("Status", extractPattern(text, Constants.STATUS_REGEX));
			 * keyValuePairs.put("Due Date", extractPattern(text,
			 * Constants.DUE_DATE_REGEX));
			 */
			
			//new amazon receipt reading
			 keyValuePairs.put("From", extractPattern(text,"\"(?<=Sold By\\\\s)\\\\s*:?\\\\s*([\\\\s\\\\S]*?)(?=To\\\\s)\""));
			 keyValuePairs.put("To", extractPattern(text,"\"(?<=Shipping Address\\\\s)\\\\s*:?\\\\s*([\\\\s\\\\S]*?)(?=State\\\\s)\""));
			 keyValuePairs.put("Order Number", extractPattern(text,"Order Number\\s*:?\\s*(\\d+)"));
			 keyValuePairs.put("Shipment Number", extractPattern(text,"Invoice Number\\s*:?\\s*([A-Za-z0-9_]+)"));
			 keyValuePairs.put("Due Date", extractPattern(text,"Order Date\\s*:?\\s*([A-Za-z0-9_]+)"));
			 
			
			System.out.println("HashMap Elements: " + keyValuePairs);

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to process PDF file", e);
		}

		return keyValuePairs;
	}

	private String extractPattern(String text, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);

		if (matcher.find()) {
			return matcher.group(1).trim().replaceAll("\\r\\n|\\n", " "); // Clean up newlines
		}
		return null;
	}
}
