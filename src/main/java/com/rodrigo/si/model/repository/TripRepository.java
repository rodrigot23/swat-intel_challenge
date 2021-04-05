package com.rodrigo.si.model.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rodrigo.si.model.Trip;

public interface TripRepository extends JpaRepository<Trip, String>{

	@Query("select min(t.departure) from trip t "
			+ "where (t.origin.station = :ori or t.destiny.station = :dest) "
			+ "and (:depart is null or t.departureDate = :depart)")
	Optional<LocalTime> findMinDepartureTime(
			@Param("ori") String origin, 
			@Param("dest") String destiny, 
			@Param("depart") LocalDate departure);
	
	@Query("select max(t.arrival) from trip t "
			+ "where (t.origin.station = :ori or t.destiny.station = :dest) "
			+ "and (:depart is null or t.departureDate = :depart)")
	Optional<LocalTime> findMaxArrivalTime(
			@Param("ori") String origin, 
			@Param("dest") String destiny, 
			@Param("depart") LocalDate departure);
	
	
	@Query("select t from trip t "
			+ "where t.departure >= :depart and t.arrival <= :arrival "
			+ "and (:departDate is null or t.departureDate = :departDate)")
	Optional<List<Trip>> findByDepartureGreateThanAndArrivalLessThan(
			@Param("depart") LocalTime departure, 
			@Param("arrival") LocalTime arrival,
			@Param("departDate") LocalDate departureDate);
	
}
