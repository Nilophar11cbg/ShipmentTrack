package com.shipment.track.model;

public class ShipmentCountResponse {
	private long count;

	public ShipmentCountResponse(long count) {
		this.count = count;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}
}
