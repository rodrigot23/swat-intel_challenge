package com.rodrigo.si.resource.projection;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TripRouteDTO {

	private String origin;
	private String destiny;
	private LocalDateTime departure;
	private LocalTime arrival;
	private List<TripConnectionDTO> tripConnections;
}
