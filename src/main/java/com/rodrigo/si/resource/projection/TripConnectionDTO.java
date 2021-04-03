package com.rodrigo.si.resource.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.rodrigo.si.model.Trip;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TripConnectionDTO {

	private String origin;
	private String destiny;
	private LocalDateTime departure;
	private LocalDateTime arrival;
	private String company;
	private BigDecimal price;
	
	public TripConnectionDTO toTripConnectionDTO(Trip trip) {
		this.setArrival(trip.getDepartureDate().atTime(trip.getArrival()));
		this.setDeparture(trip.getDepartureDate().atTime(trip.getDeparture()));
		this.setDestiny(trip.getDestiny().getStation());
		this.setOrigin(trip.getOrigin().getStation());
		this.setCompany(trip.getCompany());
		this.setPrice(trip.getPrice());
		return this;
	}
}
