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
	private BiPredicate<Trip, Trip> departureDateAndHourFilter = null;
	private BiPredicate<Trip, Trip> maximumHour = null;
	private Comparator<Trip> arrivalOrder = null;
	private Comparator<Trip> departureDate = null;

	public ConnectionOperation() {
		buildRules();
	}

	public BiPredicate<Trip, Trip> getDepartureDateAndHourFilter() {
		return departureDateAndHourFilter;
	}
	
	private void buildRules() {
		this.originFilter = (e, o) -> e.getOrigin().getStation().equals(o);
		this.destinyFilter = (e, o) -> e.getDestiny().getStation().equals(o);
		this.departureDateAndHourFilter = (e, o) -> {
			return (Objects.isNull(o)) ? true : (e.getDepartureDate().isAfter(o.getDepartureDate()) 
					|| e.getDepartureDate().isEqual(o.getDepartureDate()))
					&& e.getDeparture().isAfter(o.getArrival());
		};
		this.maximumHour = (e, o) -> e.getDeparture().getHour() - o.getArrival().getHour() <= 12;
		this.arrivalOrder = (e1, e2) ->  e1.getArrival().compareTo(e2.getArrival());
		this.departureDate = (e1, e2) ->  e1.getDepartureDate().compareTo(e2.getDepartureDate());
	}

	public List<Trip> getSingleResult(List<Trip> trips, String origin, String destiny, Trip previousTrip) {
		var single = trips.stream()
				.filter(e -> originFilter.test(e, origin))
				.filter(e -> destinyFilter.test(e, destiny))
				.filter(e -> departureDateAndHourFilter.test(e, previousTrip))
				.sorted(departureDate)
				.sorted(arrivalOrder)
				.collect(Collectors.toList());
		return single;
	}

	public List<Trip> getAllByOrigin(List<Trip> trips, String origin) {
		return trips.stream()
				.filter(e -> originFilter.test(e, origin))
				.sorted(departureDate)
				.sorted(arrivalOrder)
				.collect(Collectors.toList());
	}

	public List<Trip> getAllByDestiny(List<Trip> trips, String destiny, Trip previousTrip) {
		return trips.stream()
				.filter(e -> destinyFilter.test(e, destiny))
				.filter(e -> departureDateAndHourFilter.test(e, previousTrip))
				.sorted(departureDate)
				.sorted(arrivalOrder)
				.collect(Collectors.toList());
	}

	public Boolean thereIsDestiny(List<Trip> trips, String destiny) {
		return trips.stream().anyMatch(e -> destinyFilter.test(e, destiny));
	}
	
	public List<Trip> getPreviousStep(List<Trip> trips, Trip nextStep) {
		return trips.stream()
				.filter(e -> destinyFilter.test(e, nextStep.getOrigin().getStation()))
				.filter(e -> departureDateAndHourFilter.test(nextStep, e))
				.filter(e -> maximumHour.test(nextStep, e))
				.sorted(departureDate)
				.sorted(arrivalOrder)
				.collect(Collectors.toList());
	}
	
	public List<Trip> getNextSteps(List<Trip> trips, List<Trip> origins) {
		return origins.stream()
			.flatMap(o -> {
				var destinations = trips.stream()
						.filter(e -> originFilter.test(e, o.getDestiny().getStation()))
						.filter(e -> departureDateAndHourFilter.test(e, o))
						.filter(e -> maximumHour.test(e, o))
						.collect(Collectors.toList());
				return destinations.isEmpty() ? null : destinations.stream();
				})
			.filter(Objects::nonNull)
			.sorted(departureDate)
			.sorted(arrivalOrder)
			.collect(Collectors.toList());
	}
	
}
