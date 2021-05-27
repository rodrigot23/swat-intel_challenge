package com.rodrigo.si.config;

import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import com.rodrigo.si.model.Trip;
import com.rodrigo.si.model.repository.StationRepository;

@Configuration
@EnableBatchProcessing
public class CsvJobConfig {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	private StationRepository stationRep;
	
	@Bean("csvTripJob")
	public Job job(
			@Qualifier("csvTripWriter") ItemWriter<Trip> writer, 
			@Qualifier("csvTripReader") ItemReader<Trip> itemReader) {
		
		var step = stepBuilderFactory.get("csv_step")
				.<Trip, Trip>chunk(100)
				.reader(itemReader)
				.writer(writer)
				.build();
		
		return jobBuilderFactory.get("csv_load")
				.incrementer(new RunIdIncrementer())
				.start(step)
				.build();
	}
	
	@Bean("csvTripReader")
	@JobScope
	public FlatFileItemReader<Trip> itemReader(
			@Qualifier("lineMapper") LineMapper<Trip> lineMapper,
			@Value("#{jobParameters['path_csv']}") String path) throws MalformedURLException {
		
		var fileItemReader = new FlatFileItemReader<Trip>();
		fileItemReader.setResource(new FileSystemResource(path));
		fileItemReader.setName("itrain-reader");
		fileItemReader.setLinesToSkip(1);
		fileItemReader.setLineMapper(lineMapper);
		fileItemReader.setStrict(false);
		return fileItemReader;
	}
	
	@Bean("lineMapper")
	@StepScope
	public LineMapper<Trip> lineMapper(
			@Qualifier("fieldSetMapper") FieldSetMapper<Trip> fieldSetMapper) {
		
		var tokenizer = new DelimitedLineTokenizer();
		tokenizer.setDelimiter(",");
		tokenizer.setStrict(false);

		var lineMapper = new DefaultLineMapper<Trip>();
        lineMapper.setFieldSetMapper(fieldSetMapper);
		lineMapper.setLineTokenizer(tokenizer);
		return lineMapper;
	}
	
	@Bean("fieldSetMapper")
	@StepScope
	public FieldSetMapper<Trip> fieldSetMapper(@Value("#{jobParameters['company']}") String company) {
		int TRIP = 0, ORIGEM = 1, DESTINY = 2, DEPARTURE_DATE = 3, DEPARTURE = 4, ARRIVAL = 5, PRICE = 6;

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm[:ss.SSSSSS]");
		
		return fs -> {
        	var trip = new Trip();
        	trip.setTrip(fs.readString(TRIP));
        	
        	var departureDate = LocalDate.from(dateFormatter.parse(fs.readString(DEPARTURE_DATE)));
    		trip.setDepartureDate(departureDate);
        	
    		var departure = LocalTime.from(timeFormatter.parse(fs.readString(DEPARTURE)));
    		trip.setDeparture(departure);
        	
    		var arrival = LocalTime.from(timeFormatter.parse(fs.readString(ARRIVAL)));
    		trip.setArrival(arrival);
    		trip.setCompany(company);
    		trip.setPrice(fs.readBigDecimal(PRICE));
    		
    		var originStation = stationRep.findById(fs.readString(ORIGEM));
    		var destinyStation = stationRep.findById(fs.readString(DESTINY));
    		
    		trip.setOrigin(originStation.get());
    		trip.setDestiny(destinyStation.get());
    		
        	return trip;
        };
	}
}
