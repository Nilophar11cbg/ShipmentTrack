package com.shipment.track.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shipment.track.model.ShipmentDetails;
import com.shipment.track.service.EmailTeService;
import com.shipment.track.service.ShipmentDetailsService;

@RestController
@RequestMapping("/api/ocr/")
@CrossOrigin(origins = "http://localhost:4200")
public class ShipmentOCRDetailsController {

	@Autowired
	private EmailTeService emailTeService;
	
	@Autowired
	ShipmentDetailsService shipmentDetailsService;

	
	 
	@GetMapping("/shipment")
	public ResponseEntity<String> createShipmentDetail() {
		try {
			// read email contents and insert data
			emailTeService.fetchEmails();
			
			return new ResponseEntity<>("Shipment created", HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * This method gets all shipment details
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
}
