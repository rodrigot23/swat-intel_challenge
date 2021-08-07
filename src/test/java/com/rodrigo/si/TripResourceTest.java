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
public class TripResourceTest {

	@Autowired
	public MockMvc mvc;
	
	@Test
	@Order(1)
	public void uploadTripsTest() throws Exception {
		var tripResourceTest = new StationResourceTest();
		tripResourceTest.mvc = mvc;
		tripResourceTest.uploadStationsTest();

		var fileBytes = getClass().getClassLoader().getResourceAsStream("iTrain.csv").readAllBytes();
		
		mvc.perform(multipart("/api/trip/csv").file("file", fileBytes).param("company", "iTrain"))
		   .andExpect(status().isCreated());
		
		fileBytes = getClass().getClassLoader().getResourceAsStream("uberOnRails.json").readAllBytes();
		
		mvc.perform(multipart("/api/trip/json").file("file", fileBytes).param("company", "uberOnRails"))
		   .andExpect(status().isCreated());
	}
	
	@Test
	@Order(2)
	public void saveTripTest() throws Exception {
		
		var body = new HashMap<String, String>();
		
		body.put("trip", "P243B976");
	    body.put("origin", "BSB");
	    body.put("destiny", "VCP");
	    body.put("departureDate", "2021-02-10");
	    body.put("departure", "06:40");
	    body.put("arrival", "19:40");
		body.put("company", "ITrain");
	    body.put("value", "948.46");
	    
	    var json = new JSONObject(body);
		
	    mvc.perform(post("/api/trip").contentType(MediaType.APPLICATION_JSON).content(json.toString()))
	       .andExpect(status().isCreated());
	}
	
	@Test
	@Order(3)
	public void singleConnectionTest() throws Exception {
		
		mvc.perform(get("/api/trip/connection").param("origin", "BSB").param("destiny", "MCZ"))
		   .andExpect(status().isOk())
		   .andExpect(jsonPath("$.origin", is("BSB")))
		   .andExpect(jsonPath("$.destiny", is("MCZ")))
		   .andExpect(jsonPath("$.departure", is("2021-02-18T20:00:00")))
		   .andExpect(jsonPath("$.arrival", is("23:00:00")))
		   .andExpect(jsonPath("$.tripConnections.[0].origin", is("BSB")))
		   .andExpect(jsonPath("$.tripConnections.[0].destiny", is("MCZ")))
		   .andExpect(jsonPath("$.tripConnections.[0].company", is("iTrain")))
		   .andExpect(jsonPath("$.tripConnections.[0].departure", is("2021-02-18T20:00:00")))
		   .andExpect(jsonPath("$.tripConnections.[0].arrival", is("2021-02-18T23:00:00")))
		   .andExpect(jsonPath("$.tripConnections.[0].price", is(413.13)));
	}
	
	@Test
	@Order(4)
	public void twoConnectionTest() throws Exception {
		
		mvc.perform(get("/api/trip/connection").param("origin", "VIX").param("destiny", "FLN"))
		   .andExpect(status().isOk())
		   .andExpect(jsonPath("$.origin", is("VIX")))
		   .andExpect(jsonPath("$.destiny", is("FLN")))
		   .andExpect(jsonPath("$.departure", is("2021-02-18T12:30:00")))
		   .andExpect(jsonPath("$.arrival", is("23:30:00")))
		   .andExpect(jsonPath("$.tripConnections.[0].origin", is("VIX")))
		   .andExpect(jsonPath("$.tripConnections.[0].destiny", is("BSB")))
		   .andExpect(jsonPath("$.tripConnections.[0].company", is("iTrain")))
		   .andExpect(jsonPath("$.tripConnections.[0].departure", is("2021-02-18T12:30:00")))
		   .andExpect(jsonPath("$.tripConnections.[0].arrival", is("2021-02-18T15:00:00")))
		   .andExpect(jsonPath("$.tripConnections.[0].price", is(298.13)))
		   .andExpect(jsonPath("$.tripConnections.[1].origin", is("BSB")))
		   .andExpect(jsonPath("$.tripConnections.[1].destiny", is("FLN")))
		   .andExpect(jsonPath("$.tripConnections.[1].company", is("iTrain")))
		   .andExpect(jsonPath("$.tripConnections.[1].departure", is("2021-02-18T19:00:00")))
		   .andExpect(jsonPath("$.tripConnections.[1].arrival", is("2021-02-18T23:30:00")))
		   .andExpect(jsonPath("$.tripConnections.[1].price", is(369.19)));
	}
	
	@Test
	@Order(5)
	public void notFoundConnectionTest() throws Exception {
		
		mvc.perform(get("/api/trip/connection").param("origin", "MCZ").param("destiny", "VCP"))
		   .andExpect(status().isNoContent());
		
	}
	
	@Test
	@Order(6)
	public void notFoundConnectionWithDateTest() throws Exception {
		
		mvc.perform(get("/api/trip/connection").param("origin", "BSB").param("destiny", "MCZ").param("date", "19/02/2021"))
		   .andExpect(status().isNoContent());
	}
}
