package com.rodrigo.si.resource;

import java.io.IOException;
import java.util.List;

import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.rodrigo.si.resource.projection.StationDTO;
import com.rodrigo.si.service.StationService;

@RestController
@RequestMapping("/station")
public class StationResource {

	@Autowired
	private StationService stationResource;
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void saveTrip(@RequestBody StationDTO station) throws Exception {
		stationResource.save(station);
	}
	
	@GetMapping("/{name}")
	public ResponseEntity<List<StationDTO>> getStations(@PathVariable("name") String name) {
		var stations = stationResource.getStationsByName(name);
		if (stations.isPresent()) {
			return ResponseEntity.ok(stations.get());
		}
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("/json")
	@ResponseStatus(HttpStatus.CREATED)
	public void saveJsonFile(@RequestParam("file") MultipartFile file) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, IOException, InterruptedException {
		stationResource.batchJson(file);
	}
}
