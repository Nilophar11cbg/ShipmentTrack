package com.shipment.track.service;

import javax.mail.*;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
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
				extractTextFromPDF(is);
			}

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

			ShipmentDetails details = new ShipmentDetails();
			details.setSenderAddress(keyValueMap.get("from"));
			details.setReceiverAddress(keyValueMap.get("to"));
			details.setTrackingNumber(keyValueMap.get("shipmentNumber"));
			details.setOrderId(keyValueMap.get("orderNumber"));
			details.setStatus(keyValueMap.get("status"));
			String dueDate = keyValueMap.get("Due Date");
			System.out.println("Due date=> " + dueDate);

			if (dueDate != null && !dueDate.isEmpty()) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				java.util.Date parsedDate = sdf.parse(dueDate);

				java.sql.Date sqlDueDate = new java.sql.Date(parsedDate.getTime());
				details.setShipmentDate(sqlDueDate);
			}
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

			String soldByRegex = "(?i)Sold By\\s*:?\\s*([\\s\\S]*?)(?=\\s*(?:Shipping Address|Total Due|GST Registration No|PAN No|Order Number|$))";
			String shippingAddressRegex = "(?i)Shipping Address\\s*:?\\s*([\\s\\S]*?)(?=\\s*(?:Billing Address|Total Due|State|Order Number|$))";
			String orderNumberRegex = "(?i)Order Number\\s*:?\\s*([A-Za-z0-9-]+)";
			String statusRegex = "Status:?\\s*:?\\s*([A-Za-z0-9_]+)";
			String dueDateRegex = "(?i)Due Date\\s*:?\\s*(\\d{2}-\\d{2}-\\d{4})";

			keyValuePairs.put("from", extractPattern(text, soldByRegex));
			keyValuePairs.put("to", extractPattern(text, shippingAddressRegex));
			keyValuePairs.put("orderNumber", extractPattern(text, orderNumberRegex));
			keyValuePairs.put("status", extractPattern(text, statusRegex));
			keyValuePairs.put("Due Date", extractPattern(text, dueDateRegex));
			System.out.println("HashMap Elements: " + keyValuePairs);

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to process PDF file", e);
		}
		return keyValuePairs;
	}

	public String extractValueForKey(String text, String key) throws IOException {
		String pattern = "(?s)" + key + "\\s*[:]?\\s*(.*?)\\s*(?=\n|$)";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(text);

		if (m.find()) {
			return m.group(1);
		} else {
			return null;
		}
	}

	private String extractPattern(String text, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);

		if (matcher.find()) {
			return matcher.group(1).trim().replaceAll("\\r\\n|\\n", " ");
		}
		return null;
	}
}
