package com.rodrigo.si.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rodrigo.si.model.Trip;

public interface TripRepository extends JpaRepository<Trip, String>{

}
