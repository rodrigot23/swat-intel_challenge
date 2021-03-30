package com.rodrigo.si.resource;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rodrigo.si.model.Trip;
import com.rodrigo.si.resource.projection.TripDTO;
import com.rodrigo.si.service.TripService;

@RestController
@RequestMapping("/trip")
public class TripResource {

	@Autowired
	private TripService tripServ;
	
	@PostMapping
	@ResponseStatus(HttpStatus.OK)
	public void saveTrip(@RequestBody TripDTO trip) throws Exception {
		tripServ.save(trip);
	}
	
	@PostMapping("/csv")
	@ResponseStatus(HttpStatus.OK)
	public void saveTripCsv(@RequestParam("file") MultipartFile file, @RequestParam String company) throws Exception {
		tripServ.batchCsv(file, company);
	}
	
	@PostMapping("/json")
	@ResponseStatus(HttpStatus.OK)
	public void saveTripJson(@RequestParam("file") MultipartFile file, @RequestParam String company) throws Exception {
		tripServ.batchJson(file, company);
	}
	
	@GetMapping
	public ResponseEntity<List<Trip>> listTrips() throws Exception {
		var result = tripServ.listAll();
		if (result.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		return ResponseEntity.ok(result.get());
	}
	
}
