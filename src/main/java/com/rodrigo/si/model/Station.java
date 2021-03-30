package com.rodrigo.si.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity(name="station")
public class Station {

	@Id
	@Column(length=3, nullable=false)
	private String station;

	@Column(nullable=false)
	private String name;

	@Column(nullable=false)
	private String city;
}
