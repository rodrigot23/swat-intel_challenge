package com.rodrigo.si.resource.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.rodrigo.si.model.Station;
import com.rodrigo.si.model.Trip;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TripDTO {

	private String trip;
	private String origin;
	private String destiny;
	private LocalDate departureDate;
	private LocalTime departure;
	private LocalTime arrival;
	private String company;
	private BigDecimal value;
	
	
	public Trip toTripEntity() {
		var trip = new Trip();
		trip.setTrip(this.trip);
		trip.setOrigin(new Station());
		trip.getOrigin().setStation(origin);
		trip.setDestiny(new Station());
		trip.getDestiny().setStation(destiny);
		trip.setDepartureDate(departureDate);
		trip.setDeparture(departure);
		trip.setArrival(arrival);
		trip.setCompany(company);
		trip.setPrice(value);
		return trip;
	}
	
	public void populateDTOFromEntity(Trip trip) {
		this.trip = trip.getTrip();
		this.origin = trip.getOrigin().getStation();
	}
}
