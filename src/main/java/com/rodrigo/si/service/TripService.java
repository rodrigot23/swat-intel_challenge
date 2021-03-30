package com.rodrigo.si.service;

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

@Service
public class TripService {

	@Autowired
	private TripRepository tripRep;

	@Autowired
	private StationRepository stationRep;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	@Qualifier("csvTripJob")
	private Job job;

	@Autowired
	@Qualifier("jsonTripJob")
	private Job json;

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

	public BatchStatus batchCsv(MultipartFile file, String company) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		var jobParamBuilder = new JobParametersBuilder();
		jobParamBuilder.addLong("time", System.currentTimeMillis());
		jobParamBuilder.addString("company", company);

		return jobLauncher.run(job, jobParamBuilder.toJobParameters()).getStatus();
	}

	public BatchStatus batchJson(MultipartFile file, String company) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		var jobParamBuilder = new JobParametersBuilder();
		jobParamBuilder.addLong("time", System.currentTimeMillis());
		jobParamBuilder.addString("company", company);

		return jobLauncher.run(json, jobParamBuilder.toJobParameters()).getStatus();
	}

	public Optional<List<Trip>> listAll() {
		return Optional.of(tripRep.findAll());
	}
}
