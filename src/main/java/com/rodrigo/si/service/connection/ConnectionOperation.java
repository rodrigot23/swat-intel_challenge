package com.rodrigo.si.service.connection;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.rodrigo.si.model.Trip;

@Component
public class ConnectionOperation {

	private BiPredicate<Trip, String> originFilter = null;
	private BiPredicate<Trip, String> destinyFilter = null;
	private BiPredicate<Trip, Trip> hourFilter = null;
	private BiPredicate<Trip, Trip> maximumHour = null;
	private Comparator<Trip> arrivalOrder = null;

	public ConnectionOperation() {
		buildRules();
	}

	private void buildRules() {
		this.originFilter = (e, o) -> e.getOrigin().getStation().equals(o);
		this.destinyFilter = (e, o) -> e.getDestiny().getStation().equals(o);
		this.hourFilter = (e, o) -> e.getDeparture().isAfter(o.getArrival());
		this.maximumHour = (e, o) -> e.getDeparture().getHour() - o.getArrival().getHour() <= 12;
		this.arrivalOrder = (e1, e2) ->  e1.getArrival().compareTo(e2.getArrival());
	}

	public List<Trip> getSingleResult(List<Trip> trips, String origin, String destiny) {
		var single = trips.stream()
				.filter(e -> originFilter.test(e, origin))
				.filter(e -> destinyFilter.test(e, destiny))
				.sorted(arrivalOrder)
				.collect(Collectors.toList());
		return single;
	}

	public List<Trip> getAllByOrigin(List<Trip> trips, String origin) {
		return trips.stream()
				.filter(e -> originFilter.test(e, origin))
				.sorted(arrivalOrder)
				.collect(Collectors.toList());
	}

	public List<Trip> getAllByDestiny(List<Trip> trips, String destiny) {
		return trips.stream()
				.filter(e -> destinyFilter.test(e, destiny))
				.sorted(arrivalOrder)
				.collect(Collectors.toList());
	}

	public Boolean thereIsDestiny(List<Trip> trips, String destiny) {
		return trips.stream().anyMatch(e -> destinyFilter.test(e, destiny));
	}
	
	public List<Trip> getPreviousStep(List<Trip> trips, Trip nextStep) {
		return trips.stream()
				.filter(e -> destinyFilter.test(e, nextStep.getOrigin().getStation()))
				.filter(e -> hourFilter.test(nextStep, e))
				.filter(e -> maximumHour.test(nextStep, e))
				.sorted(arrivalOrder)
				.collect(Collectors.toList());
	}
	
	public List<Trip> getNextSteps(List<Trip> trips, List<Trip> origins) {
		return origins.stream()
			.flatMap(o -> {
				var destinations = trips.stream()
						.filter(e -> originFilter.test(e, o.getDestiny().getStation()))
						.filter(e -> hourFilter.test(e, o))
						.filter(e -> maximumHour.test(e, o))
						.collect(Collectors.toList());
				return destinations.isEmpty() ? null : destinations.stream();
				})
			.filter(Objects::nonNull)
			.sorted(arrivalOrder)
			.collect(Collectors.toList());
	}
	
}
