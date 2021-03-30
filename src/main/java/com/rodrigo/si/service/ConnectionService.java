package com.rodrigo.si.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


	private TripRouteDTO createTripRoute(Trip tripOneStep) {
		var tripRoute = new TripRouteDTO();
		tripRoute.setArrival(tripOneStep.getArrival());
		tripRoute.setDeparture(tripOneStep.getDepartureDate().atTime(tripOneStep.getDeparture()));
		tripRoute.setDestiny(tripOneStep.getDestiny().getStation());
		tripRoute.setOrigin(tripOneStep.getOrigin().getStation());

		return tripRoute;
	}

	private List<TripConnectionDTO> createTripConnections(Trip...trips) {
		var conn = new ArrayList<TripConnectionDTO>();
		var s = Stream.of(trips);

		s.forEach(trip -> {
			var tripStep = new TripConnectionDTO();

			tripStep.setArrival(trip.getDepartureDate().atTime(trip.getArrival()));
			tripStep.setDeparture(trip.getDepartureDate().atTime(trip.getDeparture()));
			tripStep.setDestiny(trip.getDestiny().getStation());
			tripStep.setOrigin(trip.getOrigin().getStation());
			tripStep.setCompany(trip.getCompany());
			tripStep.setPrice(trip.getPrice());

			conn.add(tripStep);
		});

		return conn;
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

	private List<Trip> getConnectionsTrips(String origin, String destiny, List<Trip> result) {

		var allOrigins = result.stream().filter(e -> e.getOrigin().getStation().equals(origin)).collect(Collectors.toList());

		var allDestinys = result.stream().filter(e -> e.getDestiny().getStation().equals(destiny)).collect(Collectors.toList());

		var isThere = allOrigins.stream().anyMatch(e -> {
			return allDestinys.stream().anyMatch(e2 -> e2.getOrigin().getStation().equals(e.getDestiny().getStation()));
		});

		var res = new ArrayList<Trip>();
		
		if (isThere) {
			for (var ori : allOrigins) {
				for (var dest : allDestinys) {
					res.add(ori);
					res.add(dest);
				}
			}
		} else {
			var allOthers = result.stream().filter(e -> ! e.getOrigin().getStation().equals(origin)).collect(Collectors.toList());
			for (Trip e : allOrigins) {
				res.addAll(getConnectionsTrips(e.getOrigin().getStation(), e.getDestiny().getStation(), allOthers));
			}
		}
		return res;
	}

	public Optional<TripRouteDTO> getConnectionTrip(TripQueryDTO trip) {

		var result = getBetweenOriginAndDetiny(trip.getOrigin(), trip.getDestiny(), trip.getDate());

		// First of all, try to find a straight trip
		Predicate<Trip> originPred = e -> e.getOrigin().getStation().equals(trip.getOrigin());
		Predicate<Trip> destinyPred = e -> e.getDestiny().getStation().equals(trip.getDestiny());

		if (result.stream().anyMatch(originPred.and(destinyPred))) {
			var tripOneStep = result.stream().filter(originPred.and(destinyPred)).collect(Collectors.toList()).get(0);

			var tripRoute = createTripRoute(tripOneStep);

			tripRoute.setTripConnections(createTripConnections(tripOneStep));

			return Optional.of(tripRoute);
		}

		// If doesn't exist a straight trip, them let's search for steps between origin and destiny
		// TODO: implement parser to tripRoute
		var finalResult = getConnectionsTrips(trip.getOrigin(), trip.getDestiny(), result);

		return Optional.of(new TripRouteDTO());
	}
}
