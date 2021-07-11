package com.rodrigo.si.service;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rodrigo.si.model.Station;
import com.rodrigo.si.model.repository.StationRepository;
import com.rodrigo.si.resource.projection.StationDTO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StationService {

	@Autowired
	private StationRepository stationRep;
	
	@Autowired
	private EntityManager entityManager;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	@Qualifier("stationJob")
	private Job job;
	
	public Page<Station> getAllStations(Integer page) {
		
		Pageable pageable = PageRequest.of(page, 5);
		
		return stationRep.findAll(pageable);
	}
	
	public Optional<List<StationDTO>> getStationsByName(String name) {
		var builder = entityManager.getCriteriaBuilder();
		
		var query = builder.createQuery(Station.class);
		var root = query.from(Station.class);
		
		var nameLike = builder.like(builder.lower(root.get("name")), "%"+name.toLowerCase()+"%");
		
		query.select(root);
		query.where(nameLike);
		
		var result = entityManager.createQuery(query)
				.getResultList()
				.stream()
				.map(e -> new StationDTO().populateDTOFromEntity(e))
				.collect(Collectors.toList());
		
		return Optional.of(result);
		
	}

	public void save(StationDTO stationDTO) {
		var station = stationDTO.toStationEntity();
		stationRep.save(station);
	}
	
	public BatchStatus batchJson(MultipartFile file) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, IOException, InterruptedException {

		var path = Files.write(Files.createTempFile("station_json", file.getName()), file.getBytes());
		
		var jobParamBuilder = new JobParametersBuilder();
		jobParamBuilder.addLong("time", System.currentTimeMillis());
		jobParamBuilder.addString("station_json", path.toString());
		
		var jobExecution = jobLauncher.run(job, jobParamBuilder.toJobParameters());
		log.debug("JobExecution (Station): " + jobExecution.getStatus());

		log.debug("Batch is Running...");
		while (jobExecution.isRunning()) Thread.sleep(500);
		log.debug("Batch finished!");
		
		return jobExecution.getStatus();
	}
}
