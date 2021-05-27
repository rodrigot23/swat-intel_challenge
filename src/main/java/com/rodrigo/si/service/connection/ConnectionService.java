package com.rodrigo.si.service.connection;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rodrigo.si.model.Trip;
import com.rodrigo.si.model.repository.TripRepository;
import com.rodrigo.si.resource.projection.TripConnectionDTO;
import com.rodrigo.si.resource.projection.TripQuery;
import com.rodrigo.si.resource.projection.TripRouteDTO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConnectionService {

	@Autowired
	private TripRepository tripRepository;

	@Autowired
	private ConnectionOperation connectionOperation;

	private Optional<List<Trip>> getBetweenOriginAndDestiny(String origin, String destiny, LocalDate date) {
		log.debug("Getting trips between origin and destiny from database");
		var minDeparture = tripRepository.findMinDepartureTime(origin, destiny, date);
		var maxArrival = tripRepository.findMaxArrivalTime(origin, destiny, date);

		if (minDeparture.isEmpty() || maxArrival.isEmpty()) {
			return Optional.ofNullable(null);
		}
		
		var tripsBetween = tripRepository.findByDepartureGreateThanAndArrivalLessThan(minDeparture.get(), maxArrival.get(), date);

		return tripsBetween;
	}

	@SuppressWarnings("unchecked")
	private List<Trip> getConnectionsTrips(String origin, String destiny, List<Trip> allTrips, Trip previousTrip) {
		
		var single = connectionOperation.getSingleResult(allTrips, origin, destiny, previousTrip);
		
		if ( ! single.isEmpty()) {
			return previousTrip == null? single : single.subList(0, 1);
		}

		var origins = connectionOperation.getAllByOrigin(allTrips, origin);

		var nextSteps = connectionOperation.getNextSteps(allTrips, origins);

		var finalResult = new ArrayList<Trip>();

		if (nextSteps.isEmpty()) return finalResult;

		var result = connectionOperation.getAllByDestiny(nextSteps, destiny, previousTrip);

		if (result.isEmpty()) {
			// search recursively until find the destiny
			nextSteps.stream()
			.sorted((e1, e2) -> e1.getDepartureDate().compareTo(e2.getDepartureDate()))
			.sorted((e1, e2) -> e1.getArrival().compareTo(e2.getArrival()))
			.filter(e -> connectionOperation.getDepartureDateAndHourFilter().test(e, previousTrip))
			.filter(e -> ! e.getDestiny().getStation().equals(origin))
			// we need to put previous step together in order to filter and add to stack of trips
			.map(e -> {
				var allTrips2 = allTrips.stream().filter(e2 -> ! e2.getTrip().equals(e.getTrip())).collect(Collectors.toList());
				return List.of(getConnectionsTrips(e.getDestiny().getStation(), destiny, allTrips2, e), e);
			})
			// filter by the elements who have a destiny
			.filter(e -> connectionOperation.thereIsDestiny((List<Trip>)e.get(0), destiny))
			// we need to take the first destiny, that is the shortest as long as it was sorted by arrival time before
//			.findFirst()
//			.stream()
			.forEach(e -> {
				finalResult.addAll((List<Trip>)e.get(0));
				finalResult.add((Trip)e.get(1));
				var p = connectionOperation.getPreviousStep(origins, (Trip)e.get(1));
				if ( ! p.isEmpty()) finalResult.add(p.get(0));
			});

		} else {
			var previous = connectionOperation.getPreviousStep(origins, result.get(0));
			finalResult.addAll(result.subList(0, 1));
			finalResult.add(previous.get(0));
		}

		return finalResult;
	}

	public Optional<TripRouteDTO> getConnectionTrip(TripQuery trip) {

		var trips = getBetweenOriginAndDestiny(trip.getOrigin(), trip.getDestiny(), trip.getDate());

		if (trips.isEmpty()) return Optional.ofNullable(null);

		var finalResult = getConnectionsTrips(trip.getOrigin(), trip.getDestiny(), trips.get(), null);

		if (finalResult.isEmpty()) return Optional.ofNullable(null);

		Collections.reverse(finalResult);

		var tripRoute = new TripRouteDTO();

		tripRoute.setArrival(finalResult.get(finalResult.size()-1).getArrival());
		tripRoute.setDeparture(finalResult.get(0).getDepartureDate().atTime(finalResult.get(0).getDeparture()));
		tripRoute.setDestiny(finalResult.get(finalResult.size()-1).getDestiny().getStation());
		tripRoute.setOrigin(finalResult.get(0).getOrigin().getStation());

		var connections = finalResult.stream()
				.map(e -> new TripConnectionDTO().toTripConnectionDTO(e))
				.collect(Collectors.toList());

		tripRoute.setTripConnections(connections);

		return Optional.of(tripRoute);
	}
}
