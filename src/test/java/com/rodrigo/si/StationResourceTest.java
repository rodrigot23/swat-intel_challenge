package com.rodrigo.si;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;

import org.json.JSONObject;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class StationResourceTest {

	@Autowired
	public MockMvc mvc;

	@Test
	@Order(1)
	public void uploadStationsTest() throws Exception {
		var fileBytes = getClass()
				.getClassLoader()
				.getResourceAsStream("trainStations.json")
				.readAllBytes();
		
		mvc.perform(multipart("/api/station/json").file("file", fileBytes))
		   .andExpect(status().isCreated());
	}

	@Test
	@Order(2)
	public void saveStationTest() throws Exception {
		
		var body = new HashMap<String, String>();
		body.put("name", "Estação Juscelino Kubitschek");
		body.put("station", "BSB");
		body.put("city", "Brasília");
		
		var json = new JSONObject(body);
		
		mvc.perform(
				post("/api/station")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json.toString()))
			.andExpect(status().isCreated());
	}
	
	@Test
	@Order(3)
	public void saveStationsAndGetStationsByNameTest() throws Exception {
		
		mvc.perform(get("/api/station/juscelino"))
		   .andExpect(status().isOk())
		   .andExpect(jsonPath("$.[0].name", is("Estação Juscelino Kubitschek")))
		   .andExpect(jsonPath("$.[0].station", is("BSB")))
		   .andExpect(jsonPath("$.[0].city", is("Brasília")));

	}
}
