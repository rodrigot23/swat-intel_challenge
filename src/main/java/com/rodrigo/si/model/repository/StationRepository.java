package com.rodrigo.si.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.rodrigo.si.model.Station;

public interface StationRepository extends JpaRepository<Station, String>{

	Page<Station> findAll(Pageable pageable);
}
