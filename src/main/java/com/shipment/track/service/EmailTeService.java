package com.shipment.track.service;

import javax.mail.*;
import javax.mail.search.FlagTerm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.shipment.track.model.ShipmentDetails;
import com.shipment.track.repository.ShipmentDetailsRepository;

import io.micrometer.common.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailTeService {

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
		try {
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
					// Process the email message and extract attachments
					handleEmailAttachment(message);
				}

				inbox.close(false);
				store.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleEmailAttachment(Message message) throws Exception {
		if (message.isMimeType("multipart/*")) {
			Multipart multipart = (Multipart) message.getContent();
			for (int i = 0; i < multipart.getCount(); i++) {
				BodyPart bodyPart = multipart.getBodyPart(i);
				if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
					// Extract the file and process it
					String filename = bodyPart.getFileName();
					InputStream inputStream = bodyPart.getInputStream();

					// Save the attachment to a temporary file
					File tempFile = saveAttachmentToFile(filename, inputStream);

					// Process the file (if it's a PDF, convert it to images and use Tesseract)
					processPdfFile(tempFile);
				}
			}
		}
	}

	private File saveAttachmentToFile(String filename, InputStream inputStream) throws IOException {
		File file = new File("D:\\demo\\28 Nov\\uploads/" + filename);
		try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				fileOutputStream.write(buffer, 0, bytesRead);
			}
		}
		return file;
	}

	private void processPdfFile(File file) {
		// Check if the file is a PDF, and then extract images
		if (file.getName().endsWith(".pdf")) {
			try {
				PdfToImageExtractor.extractImagesFromPdf(file.getAbsolutePath(),
						"D:\\demo\\28 Nov\\uploads\\temp-images");
				for (int i = 0; i < 5; i++) { // Adjust based on the number of pages
					String imagePath = "D:\\demo\\28 Nov\\uploads\\temp-images\\page_" + i + ".png";
					String extractedText = extractTextFromImage(imagePath);

					Map<String, String> keyValueMap = extractKeyValuePairs(extractedText);

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

					String shipmentNumber = keyValueMap.get("shipmentNumber");
					details.setTrackingNumber(shipmentNumber);

					if(StringUtils.isNotBlank(details.getOrderId()))
					repository.save(details);

				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	private Map<String, String> extractKeyValuePairs(String extractedText) {
		Map<String, String> keyValuePairs = new HashMap<>();

		String soldByRegex = "(?i)Sold By\\s*:?\\s*([\\s\\S]*?)(?=\\s*(?:State|Shipping Address|Total Due|GST Registration No|PAN No|Order Number|$))";
		String shippingAddressRegex = "(?i)Shipping Address\\s*:?\\s*([\\s\\S]*?)(?=\\s*(?:Status| Billing Address|Total Due|State|Order Number|$))";
		String orderNumberRegex = "(?i)Order Number\\s*:?\\s*([A-Za-z0-9-]+)";
		String statusRegex = "Status:?\\s*:?\\s*([A-Za-z0-9_]+)";
		String dueDateRegex = "(?i)Due Date\\s*:?\\s*(\\d{2}-\\d{2}-\\d{4})";
		String shipmentNumberRegex = "(?i)Invoice Number\\s*:?\\s*([A-Za-z0-9-]+)";

		System.out.println("Sold by " + extractPattern(extractedText, soldByRegex));
		
		keyValuePairs.put("from", extractPattern(extractedText, soldByRegex));
		keyValuePairs.put("to", extractPattern(extractedText, shippingAddressRegex));
		keyValuePairs.put("orderNumber", extractPattern(extractedText, orderNumberRegex));
		keyValuePairs.put("status", extractPattern(extractedText, statusRegex));
		keyValuePairs.put("Due Date", extractPattern(extractedText, dueDateRegex));
		keyValuePairs.put("shipmentNumber", extractPattern(extractedText, shipmentNumberRegex));
		return keyValuePairs;
	}

	private String extractPattern(String text, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);

		if (matcher.find()) {
			return matcher.group(1).trim().replaceAll("\\r\\n|\\n", " ");
		}
		return null;
	}

	private String extractTextFromImage(String imagePath) {
		TesseractService tesseractService = new TesseractService();
		return tesseractService.extractTextFromImage(imagePath);
	}
}
