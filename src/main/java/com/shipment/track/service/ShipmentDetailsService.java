package com.shipment.track.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shipment.track.model.ShipmentDetails;
import com.shipment.track.repository.ShipmentDetailsRepository;

@Service
public class ShipmentDetailsService {

	@Autowired
	ShipmentDetailsRepository repository;
	
	 public List<ShipmentDetails> getAllShipmentDetails() {
		return repository.findAll();
	 }
	 
		/*
		 * public Long getShipmentByStatus(String status) { return
		 * repository.getShipmentByStatus(status); }
		 * 
		 * public ShipmentDetails getShipmentDetailsById(Long shipmentId) { return
		 * repository.getShipmentDetails(shipmentId); }
		 */

}
