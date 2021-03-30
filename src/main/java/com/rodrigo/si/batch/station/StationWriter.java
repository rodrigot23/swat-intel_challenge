package com.rodrigo.si.batch.station;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rodrigo.si.model.Station;
import com.rodrigo.si.model.repository.StationRepository;

@Component("stationWriter")
public class StationWriter implements ItemWriter<Station> {

	@Autowired
	private StationRepository stationRep;

	@Override
	public void write(List<? extends Station> items) throws Exception {
		items.forEach(e -> stationRep.save(e));
	}
}
