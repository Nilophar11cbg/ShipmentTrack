package com.shipment.track.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shipment.track.model.ShipmentDetails;
import com.shipment.track.service.ShipmentDetailsService;

@RestController
@RequestMapping("/api")
public class ShipmentDetailsController {

	@Autowired
	ShipmentDetailsService shipmentDetailsService; 
	
	
	@GetMapping("/shipments")
	public ResponseEntity<List<ShipmentDetails>> getAllShipmentDetails() {
		try {
			List<ShipmentDetails> shipments = new ArrayList<>();

			shipmentDetailsService.getAllShipmentDetails().forEach(shipments::add);

			return new ResponseEntity<>(shipments, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	

}
