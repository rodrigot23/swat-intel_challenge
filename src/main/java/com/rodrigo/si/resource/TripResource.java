package com.rodrigo.si.resource;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.rodrigo.si.resource.projection.TripDTO;
import com.rodrigo.si.resource.projection.TripQuery;
import com.rodrigo.si.resource.projection.TripRouteDTO;
import com.rodrigo.si.service.TripService;
import com.rodrigo.si.service.connection.ConnectionService;

@RestController
@RequestMapping("/trip")
public class TripResource {

	@Autowired
	private TripService tripServ;
	
	@Autowired
	private ConnectionService connectionService;
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void saveTrip(@RequestBody TripDTO trip) throws Exception {
		tripServ.save(trip);
	}
	
	@PostMapping("/csv")
	@ResponseStatus(HttpStatus.CREATED)
	public void saveTripCsv(@RequestParam("file") MultipartFile file, @RequestParam String company) throws Exception {
		tripServ.batchCsv(file, company);
	}
	
	@PostMapping("/json")
	@ResponseStatus(HttpStatus.CREATED)
	public void saveTripJson(@RequestParam("file") MultipartFile file, @RequestParam String company) throws Exception {
		tripServ.batchJson(file, company);
	}
	
	@GetMapping("/connection")
	public ResponseEntity<TripRouteDTO> getConnectionTrip(@RequestParam String origin, @RequestParam String destiny, @RequestParam(required=false) @DateTimeFormat(pattern="dd/MM/yyyy") LocalDate date) {
		
		var trip = new TripQuery();
		trip.setOrigin(origin);
		trip.setDestiny(destiny);
		trip.setDate(date);
		
		var tripRoute = connectionService.getConnectionTrip(trip);
		if (tripRoute.isPresent()) {
			return ResponseEntity.ok(tripRoute.get());
		}
		return ResponseEntity.noContent().build();
	}
}
