package com.rodrigo.si.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rodrigo.si.model.Trip;
import com.rodrigo.si.resource.projection.TripConnectionDTO;
import com.rodrigo.si.resource.projection.TripQueryDTO;
import com.rodrigo.si.resource.projection.TripRouteDTO;

@Service
public class ConnectionService {

	@Autowired
	private EntityManager entityManager;


	private TripConnectionDTO createTripConnection(Trip trip) {
		var tripStep = new TripConnectionDTO();

		tripStep.setArrival(trip.getDepartureDate().atTime(trip.getArrival()));
		tripStep.setDeparture(trip.getDepartureDate().atTime(trip.getDeparture()));
		tripStep.setDestiny(trip.getDestiny().getStation());
		tripStep.setOrigin(trip.getOrigin().getStation());
		tripStep.setCompany(trip.getCompany());
		tripStep.setPrice(trip.getPrice());

		return tripStep;
	}

	private List<Trip> getBetweenOriginAndDetiny(String origin, String destiny, LocalDate date) {
		var builder = entityManager.getCriteriaBuilder();
		var query = builder.createQuery(Trip.class);
		var root = query.from(Trip.class);

		var originPred = builder.like(root.get("origin").get("station"), origin);
		var destinyPred = builder.like(root.get("destiny").get("station"), destiny);
		var datePred = builder.equal(root.get("departureDate"), date);

		query.select(root);

		if (Objects.nonNull(date)) {
			query.where(builder.or(originPred, destinyPred, datePred));			
		} else {
			query.where(builder.or(originPred, destinyPred));
		}

		query.orderBy(builder.asc(root.get("departure")));
		return entityManager.createQuery(query).getResultList();
	}

	private List<Trip> getConnectionsTrips(String origin, String destiny, List<Trip> allTrips) {

		BiPredicate<Trip, String> originFilter = (e, o) -> e.getOrigin().getStation().equals(o);
		BiPredicate<Trip, String> destinyFilter = (e, o) -> e.getDestiny().getStation().equals(o);
		BiPredicate<Trip, Trip> hourFilter = (e, o) -> e.getDeparture().isAfter(o.getArrival());
		BiPredicate<Trip, Trip> maximumHour = (e, o) -> e.getDeparture().getHour() - o.getArrival().getHour() <= 12;
		Comparator<Trip> arrivalOrder = (e1, e2) ->  e1.getArrival().compareTo(e2.getArrival());

		var single = allTrips.stream()
				.filter(e -> originFilter.test(e, origin))
				.filter(e -> destinyFilter.test(e, destiny))
				.sorted(arrivalOrder)
				.collect(Collectors.toList());

		if ( ! single.isEmpty()) {
			return single.subList(0, 1);
		}

		var origins = allTrips.stream().filter(e -> originFilter.test(e, origin)).collect(Collectors.toList());

		var nextSteps = origins.stream()
				.flatMap(o -> {
					var destinations = allTrips.stream()
							.filter(e -> originFilter.test(e, o.getDestiny().getStation()))
							.filter(e -> hourFilter.test(e, o))
							.filter(e -> maximumHour.test(e, o))
							.collect(Collectors.toList());
					return destinations.isEmpty() ? null : destinations.stream();
				})
				.filter(Objects::nonNull)
				.sorted(arrivalOrder)
				.collect(Collectors.toList());

		var finalResult = new ArrayList<Trip>();

		if ( ! nextSteps.isEmpty()) {
			var result = nextSteps.stream()
					.filter(e -> destinyFilter.test(e, destiny))
					.sorted(arrivalOrder)
					.collect(Collectors.toList());

			if (result.isEmpty()) {
				for (var re : nextSteps) {

					var res = getConnectionsTrips(re.getDestiny().getStation(), destiny, allTrips);
					if (res.stream().anyMatch(e -> destinyFilter.test(e, destiny))) {
						finalResult.addAll(res);
						finalResult.add(re);
						finalResult.addAll(origins.stream()
								.filter(e -> destinyFilter.test(e, re.getOrigin().getStation()))
								.filter(e -> hourFilter.test(re, e))
								.filter(e -> maximumHour.test(re, e))
								.sorted(arrivalOrder)
								.collect(Collectors.toList()));
						break;
					}
				}
			} else {
				finalResult.addAll(result.subList(0, 1));
			}
		}
		return finalResult;
	}

	public Optional<TripRouteDTO> getConnectionTrip(TripQueryDTO trip) {

		var result = getBetweenOriginAndDetiny(trip.getOrigin(), trip.getDestiny(), trip.getDate());

		var finalResult = getConnectionsTrips(trip.getOrigin(), trip.getDestiny(), result);
		
		if (finalResult.isEmpty()) {
			return Optional.ofNullable(null);
		}
		Collections.reverse(result);

		var tripRoute = new TripRouteDTO();

		tripRoute.setArrival(finalResult.get(finalResult.size()-1).getArrival());
		tripRoute.setDeparture(finalResult.get(0).getDepartureDate().atTime(finalResult.get(0).getDeparture()));
		tripRoute.setDestiny(finalResult.get(0).getDestiny().getStation());
		tripRoute.setOrigin(finalResult.get(0).getOrigin().getStation());

		var connections = finalResult.stream().map(e -> createTripConnection(e)).collect(Collectors.toList());

		tripRoute.setTripConnections(connections);
		
		return Optional.of(tripRoute);
	}
}
