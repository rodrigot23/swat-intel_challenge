package com.rodrigo.si.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rodrigo.si.resource.projection.TripQueryDTO;
import com.rodrigo.si.resource.projection.TripRouteDTO;
import com.rodrigo.si.service.ConnectionService;

@RestController
@RequestMapping("/trip/connection")
public class ConnectionTripResource {

	@Autowired
	private ConnectionService connectionService;
	
	@GetMapping
	public ResponseEntity<TripRouteDTO> getConnectionTrip(@RequestBody TripQueryDTO trip) {
		var tripRoute = connectionService.getConnectionTrip(trip);
		if (tripRoute.isPresent()) {
			return ResponseEntity.ok(tripRoute.get());
		}
		return ResponseEntity.noContent().build();
	}
}
