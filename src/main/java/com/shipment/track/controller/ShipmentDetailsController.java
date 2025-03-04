package com.shipment.track.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shipment.track.model.ShipmentCountResponse;
import com.shipment.track.model.ShipmentDetails;
import com.shipment.track.service.EmailService;
import com.shipment.track.service.ShipmentDetailsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class ShipmentDetailsController {

	private static final Logger logger = LoggerFactory.getLogger(ShipmentDetailsController.class);
	
	@Autowired
	ShipmentDetailsService shipmentDetailsService;

	@Autowired
	private EmailService emailService;

	/**
	 * This method gets all shipment details
	 * 
	 * @return
	 */
	@GetMapping("/shipments")
	public ResponseEntity<List<ShipmentDetails>> getAllShipmentDetails() {
		try {
			List<ShipmentDetails> shipments = new ArrayList<>();

			shipmentDetailsService.getAllShipmentDetails().forEach(shipments::add);

			if (shipments.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return new ResponseEntity<>(shipments, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * This method gets the shipment details count on basis of shipment id
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/shipments/{id}")
	public ResponseEntity<ShipmentDetails> getShipmentById(@PathVariable("id") long id) {
		Optional<ShipmentDetails> tutorialData = shipmentDetailsService.getShipmentDetailsById(id);

		if (tutorialData.isPresent()) {
			return new ResponseEntity<>(tutorialData.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * This method gets the shipment details count on basis of status
	 * 
	 * @param status
	 * @return
	 */
	@GetMapping("/shipments/status/{status}")
	public ResponseEntity<ShipmentCountResponse> getShipmentByStatus(@PathVariable("status") String status) {
		List<ShipmentDetails> lst = shipmentDetailsService.getShipmentByStatus(status);
		ShipmentCountResponse response = new ShipmentCountResponse(lst.size());
		return ResponseEntity.ok(response);
	}

	/**
	 * This method read emails from inbox and create entry for shipment details
	 * 
	 * @return
	 */
	@GetMapping("/shipment")
	public ResponseEntity<String> createShipmentDetail() {
		logger.info("Call to shipment creation invoked");
		try {
			emailService.fetchEmails();
			logger.info("Call to shipment creation completed");
			return new ResponseEntity<>("Shipment created", HttpStatus.CREATED);
		} catch (Exception e) {
			logger.error("problem occured in shipment creation");
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
