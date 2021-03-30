package com.rodrigo.si.service;

import java.io.IOException;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rodrigo.si.model.Station;
import com.rodrigo.si.model.repository.StationRepository;
import com.rodrigo.si.resource.projection.StationDTO;

@Service
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
	
	public BatchStatus batchJson(MultipartFile file) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, IOException {
		var jobParamBuilder = new JobParametersBuilder();
		jobParamBuilder.addLong("time", System.currentTimeMillis());
		
		var jobExecution = jobLauncher.run(job, jobParamBuilder.toJobParameters());

		System.out.println("JobExecution: " + jobExecution.getStatus());

		System.out.println("Batch is Running...");
		while (jobExecution.isRunning()) {
			System.out.println("...");
		}

		return jobExecution.getStatus();
	}
}
