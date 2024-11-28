package com.shipment.track.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shipment.track.model.ShipmentDetails;

/**
 * Repository to get shipment details
 */
public interface ShipmentDetailsRepository extends JpaRepository<ShipmentDetails, Long> {
	 
	 List<ShipmentDetails> getShipmentByStatus(String status);
	 
	 
}
