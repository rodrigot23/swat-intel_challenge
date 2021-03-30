package com.rodrigo.si.batch.trip.json;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.rodrigo.si.model.Trip;
import com.rodrigo.si.model.repository.StationRepository;

@StepScope
@Component("jsonTripProcessor")
public class JsonTripProcessor implements ItemProcessor<Map<String, Object>, Trip> {

	@Autowired
	private StationRepository stationRep;
	
	@Value("#{jobParameters['company']}")
	private String company;
	
	private DateTimeFormatter dateFormatter = null;
	private DateTimeFormatter timeFormatter = null;
	
	public JsonTripProcessor() {
		dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		timeFormatter = DateTimeFormatter.ofPattern("HH:mm[:ss.SSSSSS]");
	}
	
	@Override
	public Trip process(Map<String, Object> item) throws Exception {
		var trip = new Trip();

		var origin = item.get("origin").toString();
		var originStation = stationRep.findById(origin);

		var destiny = item.get("destiny").toString();
		var destinyStation = stationRep.findById(destiny);
		
		trip.setOrigin(originStation.get());
		trip.setDestiny(destinyStation.get());
		
		trip.setTrip(item.get("trip").toString());
		
		var departureDate = LocalDate.from(dateFormatter.parse(item.get("departureDate").toString()));
		trip.setDepartureDate(departureDate);

		var departure = LocalTime.from(timeFormatter.parse(item.get("departure").toString()));
		trip.setDeparture(departure);

		var arrival = LocalTime.from(timeFormatter.parse(item.get("arrival").toString()));
		trip.setArrival(arrival);
		
		trip.setPrice(new BigDecimal(item.get("value").toString()));
		
		trip.setCompany(company);
		
		return trip;
	}
}
