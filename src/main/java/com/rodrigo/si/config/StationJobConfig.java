package com.rodrigo.si.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodrigo.si.model.Station;

@Configuration
@EnableBatchProcessing
public class StationJobConfig {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	
	@Bean("stationJob")
	public Job job(@Qualifier("stationWriter") ItemWriter<Station> writer, @Qualifier("stationReader") ItemReader<Station> itemReader) {
		
		var step = stepBuilderFactory.get("station_step")
				.<Station, Station>chunk(100)
				.reader(itemReader)
				.writer(writer)
				.build();
		
		return jobBuilderFactory.get("station_load")
				.incrementer(new RunIdIncrementer())
				.start(step)
				.build();
	}
	
	@Bean("stationReader")
	public JsonItemReader<Station> itemReader() {
		var objectMapper = new ObjectMapper();
		
		var jsonObjectReader = new JacksonJsonObjectReader<Station>(Station.class);
		jsonObjectReader.setMapper(objectMapper);
		
		return new JsonItemReaderBuilder<Station>()
				.jsonObjectReader(jsonObjectReader)
				.resource(new FileSystemResource("src/main/resources/backend-aptitude-challenge-main/trainStations.json"))
				.name("station-reader")
				.build();
	}
}
