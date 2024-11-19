package com.shipment.track.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.shipment.track.model.ShipmentDetails;

public interface ShipmentDetailsRepository extends JpaRepository<ShipmentDetails, Long> {
	 
	// Long getShipmentByStatus(String status);
	 
	// ShipmentDetails getShipmentDetails(Long shipmentId);
	 
}
