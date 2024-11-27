package com.shipment.track.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shipment.track.model.ShipmentCountResponse;
import com.shipment.track.model.ShipmentDetails;
import com.shipment.track.service.EmailService;
import com.shipment.track.service.ShipmentDetailsService;

@RestController
@RequestMapping("/api")
public class ShipmentDetailsController {

	@Autowired
	ShipmentDetailsService shipmentDetailsService;

	@Autowired
	private EmailService emailService;

	@GetMapping("/shipments")
	public ResponseEntity<List<ShipmentDetails>> getAllShipmentDetails() {
		try {
			List<ShipmentDetails> shipments = new ArrayList<>();

			if (shipments.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			shipmentDetailsService.getAllShipmentDetails().forEach(shipments::add);
			return new ResponseEntity<>(shipments, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/shipments/{id}")
	public ResponseEntity<ShipmentDetails> getShipmentById(@PathVariable("id") long id) {
		Optional<ShipmentDetails> tutorialData = shipmentDetailsService.getShipmentDetailsById(id);

		if (tutorialData.isPresent()) {
			return new ResponseEntity<>(tutorialData.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	
	@GetMapping("/shipments/status/{status}")
	public ResponseEntity<ShipmentCountResponse> getShipmentByStatus(@PathVariable("status") String status) {
		List<ShipmentDetails> lst = shipmentDetailsService.getShipmentByStatus(status);
		ShipmentCountResponse response = new ShipmentCountResponse(lst.size());
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/shipment")
	public ResponseEntity<String> createShipmentDetail() {
		try {
			
			//read email contents and insert data 
			emailService.fetchEmails();
			
			return new ResponseEntity<>("Shipment created", HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
		
	}
	 
