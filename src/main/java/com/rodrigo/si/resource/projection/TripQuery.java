package com.rodrigo.si.resource.projection;

import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TripQuery {

	private String origin;
	private String destiny;
	private LocalDate date;
}
