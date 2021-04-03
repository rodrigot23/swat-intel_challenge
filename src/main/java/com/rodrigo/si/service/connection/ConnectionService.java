package com.rodrigo.si.service.connection;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rodrigo.si.model.Trip;
import com.rodrigo.si.model.repository.TripRepository;
import com.rodrigo.si.resource.projection.TripConnectionDTO;
import com.rodrigo.si.resource.projection.TripQueryDTO;
import com.rodrigo.si.resource.projection.TripRouteDTO;

@Service
public class ConnectionService {

	@Autowired
	private TripRepository tripRepository;

	@Autowired
	private ConnectionOperation connectionOperation;
	
	private Optional<List<Trip>> getBetweenOriginAndDetiny(String origin, String destiny, LocalDate date) {
		var trips = tripRepository.findByOriginAndDestinyAndDeparture(origin, destiny, date);

		Comparator<LocalTime> timeComp = (e1, e2) -> e1.compareTo(e2);
		
		var minDeparture = trips.get().stream().map(Trip::getDeparture).min(timeComp).get();
		var maxArrival = trips.get().stream().map(Trip::getArrival).max(timeComp).get();

		var tripsBetween = tripRepository.findByDepartureGreateThanAndArrivalLessThan(minDeparture, maxArrival);
		
		trips.get().addAll(tripsBetween.get());
		
		return trips;
	}

	@SuppressWarnings("unchecked")
	private List<Trip> getConnectionsTrips(String origin, String destiny, List<Trip> allTrips) {

		var single = connectionOperation.getSingleResult(allTrips, origin, destiny);

		if ( ! single.isEmpty()) return single.subList(0, 1);

		var origins = connectionOperation.getAllByOrigin(allTrips, origin);

		var nextSteps = connectionOperation.getNextSteps(allTrips, origins);

		var finalResult = new ArrayList<Trip>();

		if ( ! nextSteps.isEmpty()) {
			
			var result = connectionOperation.getAllByDestiny(nextSteps, destiny);

			if (result.isEmpty()) {
				// search recursively until find the destiny
				nextSteps.stream()
				.sorted((e1, e2) -> e1.getArrival().compareTo(e2.getArrival()))
				// we need to put previous step together in order to filter and add to stack of trips
				.map(e -> List.of(getConnectionsTrips(e.getDestiny().getStation(), destiny, allTrips), e))
				// filter by the elements who have a destiny
				.filter(e -> connectionOperation.thereIsDestiny((List<Trip>)e.get(0), destiny))
				// we need to take the first destiny, that is the shorted as long as it was sorted by arrival time before
				.findFirst()
				.stream()
				.forEach(e -> {
					finalResult.addAll((List<Trip>)e.get(0));
					finalResult.add((Trip)e.get(1));
					finalResult.addAll(connectionOperation.getPreviousStep(origins, (Trip)e.get(1)));
				});
				
			} else {
				var previous = connectionOperation.getPreviousStep(origins, result.get(0));
				finalResult.addAll(result.subList(0, 1));
				finalResult.add(previous.get(0));
			}
		}
		return finalResult;
	}

	public Optional<TripRouteDTO> getConnectionTrip(TripQueryDTO trip) {

		var trips = getBetweenOriginAndDetiny(trip.getOrigin(), trip.getDestiny(), trip.getDate());

		if (trips.isEmpty()) return Optional.ofNullable(null);

		var finalResult = getConnectionsTrips(trip.getOrigin(), trip.getDestiny(), trips.get());
		
		if (finalResult.isEmpty()) return Optional.ofNullable(null);
		
		Collections.reverse(finalResult);

		var tripRoute = new TripRouteDTO();

		tripRoute.setArrival(finalResult.get(finalResult.size()-1).getArrival());
		tripRoute.setDeparture(finalResult.get(0).getDepartureDate().atTime(finalResult.get(0).getDeparture()));
		tripRoute.setDestiny(finalResult.get(0).getDestiny().getStation());
		tripRoute.setOrigin(finalResult.get(0).getOrigin().getStation());

		var connections = finalResult.stream()
				.map(e -> new TripConnectionDTO().toTripConnectionDTO(e))
				.collect(Collectors.toList());

		tripRoute.setTripConnections(connections);

		return Optional.of(tripRoute);
	}
}
