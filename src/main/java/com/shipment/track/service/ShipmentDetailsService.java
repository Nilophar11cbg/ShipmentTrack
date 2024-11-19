package com.shipment.track.service;

import java.util.List;
import java.util.Optional;

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

	public List<ShipmentDetails> getShipmentByStatus(String status) {
		return repository.getShipmentByStatus(status);
	}

	public Optional<ShipmentDetails> getShipmentDetailsById(long shipmentId) {
		return repository.findById(shipmentId);
	}

	
}
