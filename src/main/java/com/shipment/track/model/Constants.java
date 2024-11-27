package com.shipment.track.model;

public class Constants {

	
	public static String FROM_REGEX = "(?<=From\\s)\\s*:?\\s*([\\s\\S]*?)(?=To\\s)";
	public static String TO_REGEX = "(?<=To\\s)\\s*:?\\s*([\\s\\S]*?)(?=Shipment Number\\s)";

	public static String SHIPMENT_NUMBER_REGEX = "Shipment Number\\s*:?\\s*([A-Za-z0-9-]+)";
	public static String ORDER_NUMBER_REGEX = "Order Number\\s*:?\\s*(\\d+)";
	public static String STATUS_REGEX = "Status\\s*:?\\s*([A-Za-z0-9_]+)";
	public static String DUE_DATE_REGEX = "Due Date\\s*:?\\s*([\\d-]+)";
	
}
