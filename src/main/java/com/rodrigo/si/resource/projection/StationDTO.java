package com.rodrigo.si.resource.projection;

import com.rodrigo.si.model.Station;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StationDTO {

	private String name;
	private String station;
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
