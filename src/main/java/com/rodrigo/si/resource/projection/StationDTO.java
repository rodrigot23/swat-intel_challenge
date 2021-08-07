package com.rodrigo.si.resource.projection;

import com.rodrigo.si.model.Station;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StationDTO {

	@Schema(example = "Estação Juscelino Kubitschek")
	private String name;
	@Schema(example = "BSB")
	private String station;
	@Schema(example = "Brasília")
	private String city;
	
	
	public Station toStationEntity() {
		var station = new Station();
		station.setStation(this.station);
		station.setCity(city);
		station.setName(name);
		return station;
	}
	
	public StationDTO populateDTOFromEntity(Station station) {
		this.name = station.getName();
		this.station = station.getStation();
		this.city = station.getCity();
		return this;
	}
}
