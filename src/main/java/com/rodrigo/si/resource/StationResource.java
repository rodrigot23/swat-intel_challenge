package com.rodrigo.si.resource;

import java.io.IOException;
import java.util.List;

import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rodrigo.si.model.Station;
import com.rodrigo.si.resource.projection.StationDTO;
import com.rodrigo.si.service.StationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/station")
public class StationResource {

	@Autowired
	private StationService stationResource;
	
	
	@Operation(description = "This create a new Station.")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void saveTrip(@RequestBody StationDTO station) throws Exception {
		stationResource.save(station);
	}

	@Operation(description = "This get all stations in a pageable way.")
	@GetMapping
	public ResponseEntity<Page<Station>> getAllStations(@Schema(example = "1") @RequestParam Integer page) {
		var stations = stationResource.getAllStations(page);
		return ResponseEntity.ok(stations);
	}
	
	@Operation(description = "This get stations by name.")
	@GetMapping("/{name}")
	public ResponseEntity<List<StationDTO>> getStations(@PathVariable("name") String name) {
		var stations = stationResource.getStationsByName(name);
		if (stations.isPresent()) {
			return ResponseEntity.ok(stations.get());
		}
		return ResponseEntity.noContent().build();
	}
	
	@Operation(description = "This persist all stations from a json file."
			+ "<br> (file_example_path: '/src/test/java/resources/trainStations.json')")
	@PostMapping(value = "/json", consumes= {"multipart/form-data"})
	public ResponseEntity<String> saveJsonFile(
			@RequestParam("file") MultipartFile file) 
			throws JobParametersInvalidException, IOException, InterruptedException {
		stationResource.batchJson(file);
		return ResponseEntity.status(HttpStatus.CREATED).body("All stations persisted!");
	}
}
