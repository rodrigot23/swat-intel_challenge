package com.rodrigo.si.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity(name="trip")
public class Trip {

	@Id
	@Column(name="trip_number", length=8, nullable=false)
	private String trip;
	
	@ManyToOne
	@JoinColumn(referencedColumnName="station", nullable=false)
	private Station origin;

	@ManyToOne
	@JoinColumn(referencedColumnName="station", nullable=false)
	private Station destiny;

	@Column(name="departure_date", nullable=false)
	private LocalDate departureDate;
	
	@Column(nullable=false)
	private LocalTime departure;
	
	@Column(nullable=false)
	private LocalTime arrival;
	
	@Column(nullable=false)
	private String company;
	
	@Column(nullable=false)
	private BigDecimal price;
	
}
