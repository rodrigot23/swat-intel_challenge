package com.rodrigo.si.service;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rodrigo.si.model.Trip;
import com.rodrigo.si.model.repository.StationRepository;
import com.rodrigo.si.model.repository.TripRepository;
import com.rodrigo.si.resource.projection.TripDTO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TripService {

	@Autowired
	private TripRepository tripRep;

	@Autowired
	private StationRepository stationRep;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	@Qualifier("csvTripJob")
	private Job job_csv;

	@Autowired
	@Qualifier("jsonTripJob")
	private Job job_json;

	public void save(TripDTO tripDTO) throws Exception {
		var trip = tripDTO.toTripEntity();

		var origin = trip.getOrigin().getStation();
		var originStation = stationRep.findById(origin);

		if (originStation.isEmpty()) 
			throw new Exception("The station '"+origin+"' doesn't exists!");

		var destiny = trip.getDestiny().getStation();
		var destinyStation = stationRep.findById(destiny);

		if (destinyStation.isEmpty()) 
			throw new Exception("The station '"+destiny+"' doesn't exists!");

		trip.setOrigin(originStation.get());
		trip.setDestiny(destinyStation.get());

		tripRep.save(trip);
	}

	public BatchStatus batchCsv(MultipartFile file, String company) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, IOException, InterruptedException {

		var path = Files.write(Files.createTempFile("trip_csv", file.getName()), file.getBytes());
		
		var jobParamBuilder = new JobParametersBuilder();
		jobParamBuilder.addLong("time", System.currentTimeMillis());
		jobParamBuilder.addString("company", company);
		jobParamBuilder.addString("path_csv", path.toString());

		var jobExecution = jobLauncher.run(job_csv, jobParamBuilder.toJobParameters());
		
		log.debug("JobExecution (trip_csv): " + jobExecution.getStatus());

		log.debug("Batch is Running...");
		while (jobExecution.isRunning()) Thread.sleep(500);
		log.debug("Batch finished!");
		
		return jobExecution.getStatus();
	}

	public BatchStatus batchJson(MultipartFile file, String company) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, IOException, InterruptedException {
		
		var path = Files.write(Files.createTempFile("trip_json", file.getName()), file.getBytes());
		
		var jobParamBuilder = new JobParametersBuilder();
		jobParamBuilder.addLong("time", System.currentTimeMillis());
		jobParamBuilder.addString("company", company);
		jobParamBuilder.addString("path_json", path.toString());

		var jobExecution = jobLauncher.run(job_json, jobParamBuilder.toJobParameters());
		
		log.debug("JobExecution (trip_json): " + jobExecution.getStatus());

		log.debug("Batch is Running...");
		while (jobExecution.isRunning()) Thread.sleep(500);
		log.debug("Batch finished!");
		
		return jobExecution.getStatus();
	}

	public Optional<List<Trip>> listAll() {
		return Optional.of(tripRep.findAll());
	}
}
