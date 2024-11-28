package com.shipment.track.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shipment.track.model.ShipmentDetails;
import com.shipment.track.repository.ShipmentDetailsRepository;

/**
 * 
 */
@Service
public class ShipmentDetailsService {

	@Autowired
	ShipmentDetailsRepository repository;

	/**
	 * This method gets all shipment details
	 * @return
	 */
	public List<ShipmentDetails> getAllShipmentDetails() {
		return repository.findAll();
	}

	/**
	 * This method gets shipment details as per status provided
	 * @param status
	 * @return
	 */
	public List<ShipmentDetails> getShipmentByStatus(String status) {
		return repository.getShipmentByStatus(status);
	}

	/**
	 * This method gets shipment details as per status shipment id
	 * @param shipmentId
	 * @return
	 */
	public Optional<ShipmentDetails> getShipmentDetailsById(long shipmentId) {
		return repository.findById(shipmentId);
	}
}
