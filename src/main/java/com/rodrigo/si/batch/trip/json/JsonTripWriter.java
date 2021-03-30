package com.rodrigo.si.batch.trip.json;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rodrigo.si.model.Trip;
import com.rodrigo.si.model.repository.TripRepository;

@Component("jsonTripWriter")
public class JsonTripWriter implements ItemWriter<Trip> {
	
	@Autowired
	private TripRepository tripRep;

	@Override
	public void write(List<? extends Trip> items) throws Exception {
		items.forEach(e -> tripRep.save(e));
	}
}
