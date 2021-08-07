package com.rodrigo.si.resource.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.rodrigo.si.model.Station;
import com.rodrigo.si.model.Trip;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TripDTO {

	@Schema(example = "P243B976")
	private String trip;
	@Schema(example = "BSB")
	private String origin;
	@Schema(example = "VCP")
	private String destiny;
	@Schema(example = "2021-07-10")
	private LocalDate departureDate;
	@Schema(example = "06:40")
	private LocalTime departure;
	@Schema(example = "19:40")
	private LocalTime arrival;
	@Schema(example = "ITrain")
	private String company;
	@Schema(example = "948.46")
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
