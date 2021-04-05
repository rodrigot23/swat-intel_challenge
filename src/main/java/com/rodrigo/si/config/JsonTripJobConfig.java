package com.rodrigo.si.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
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
import com.rodrigo.si.model.Trip;

@Configuration("uberOnRailsJob")
@EnableBatchProcessing
public class JsonTripJobConfig {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	
	@Bean("jsonTripJob")
	public Job job(
			@Qualifier("jsonTripWriter") ItemWriter<Trip> writer, 
			@Qualifier("jsonTripProcessor") ItemProcessor<Map<String, Object>, Trip> processor,
			@Qualifier("jsonTripReader") ItemReader<Map<String, Object>> itemReader) {
		
		var step = stepBuilderFactory.get("uber_step")
				.<Map<String, Object>, Trip>chunk(100)
				.reader(itemReader)
				.processor(processor)
				.writer(writer)
				.build();
		
		return jobBuilderFactory.get("uber_load")
				.incrementer(new RunIdIncrementer())
				.start(step)
				.build();
	}
	
	@Bean("jsonTripReader")
	public JsonItemReader<Map<String, Object>> itemReader() {
		var objectMapper = new ObjectMapper();
		
		@SuppressWarnings("unchecked")
		var jsonObjectReader = new JacksonJsonObjectReader<Map<String, Object>>((Class<? extends Map<String, Object>>) new HashMap<String, Object>().getClass());

		jsonObjectReader.setMapper(objectMapper);
		
		return new JsonItemReaderBuilder<Map<String, Object>>()
				.jsonObjectReader(jsonObjectReader)
				.resource(new FileSystemResource("src/test/java/resources/backend-aptitude-challenge-main/uberOnRails.json"))
				.name("uberonrails-reader")
				.build();
	}
}
