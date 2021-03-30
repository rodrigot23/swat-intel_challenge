package com.rodrigo.si.resource.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TripConnectionDTO {

	private String origin;
	private String destiny;
	private LocalDateTime departure;
	private LocalDateTime arrival;
	private String company;
	private BigDecimal price;
}
