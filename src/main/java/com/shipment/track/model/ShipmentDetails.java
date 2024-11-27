package com.shipment.track.model;



import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="Shipment_details")
public class ShipmentDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long shipmentId;
	private String orderId;
	private String status;
	private String senderName;
	private String senderAddress;
	private String receiverName;
	private String receiverAddress;
	private String trackingNumber;
	private  Date shipmentDate;
	
}
