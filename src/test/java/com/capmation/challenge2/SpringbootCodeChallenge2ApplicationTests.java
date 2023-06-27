package com.capmation.challenge2;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.annotation.DirtiesContext;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringbootCodeChallenge2ApplicationTests {

	@Autowired
    TestRestTemplate restTemplate;
	
	@BeforeEach
	public void setup() {
		restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
	}
	
	/*
	 * This unit test is successfully meet when an user owning a specific note query it and receive it
	 */
	@Test
    @DirtiesContext
    void shouldReturnANoteWhenDataIsSaved() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user1", "user1$$pwd")
                .getForEntity("/notes/101", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(101);

        String title = documentContext.read("$.title");
        assertThat(title).isEqualTo("Test Note 1");
        
        String body = documentContext.read("$.body");
        assertThat(body).isEqualTo("This is test note 1");
        
        try {
        	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        	String testedDateTimeStr="2023-06-26 08:00:00.000";  
			Date testedDateTime = dateFormat.parse(testedDateTimeStr);
			OffsetDateTime createdOn = OffsetDateTime.parse(documentContext.read("$.createdOn"));
	        assertThat(Date.from(createdOn.toInstant())).isEqualTo(testedDateTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        String owner = documentContext.read("$.owner");
        assertThat(owner).isEqualTo("user1");
    }

	/*
	 * This unit test is successfully meet when an user not owning a specific note query it and receive a not found response message
	 */
    @Test
    void shouldNotReturnAnExistingNoteNotOwnedByUser() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user1", "user1$$pwd")
                .getForEntity("/notes/104", String.class); //This note does not belong to user1

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }
    
    /*
	 * This unit test is successfully meet when an user owning multiple notes retrieves them
	 */
    @Test
    void shouldReturnAllNotesWhenListIsRequested() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user1", "user1$$pwd")
                .getForEntity("/notes", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        int cashCardCount = documentContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(3);

        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(101, 102, 103);

        JSONArray titles = documentContext.read("$..title");
        assertThat(titles).containsExactlyInAnyOrder("Test Note 1", "Test Note 2", "Test Note 3");
    }
    
    /*
	 * This unit test is successfully meet when an admin user retrieves all existing notes in the system
	 */
    @Test
    void shouldReturnAllNotesWhenListIsRequestedByAdmin() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user3", "user3$$pwd")
                .getForEntity("/notes", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        int cashCardCount = documentContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(6);

        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(101, 102, 103, 104, 105, 106);

        JSONArray amounts = documentContext.read("$..title");
        assertThat(amounts).containsExactlyInAnyOrder("Test Note 1", "Test Note 2", "Test Note 3", "Test Note 1", "Test Note 2", "Test Note 1");
    }
    
    /*
	 * This unit test is successfully meet when an user creates a new note and is able to query it later
	 */
    @Test
    @DirtiesContext
    void shouldCreateANewNote() {
    	Note newNote = new Note(null, "Test Note 4", "This is test note 4", null, null, null);
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("user1", "user1$$pwd")
                .postForEntity("/notes", newNote, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewNote = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("user1", "user1$$pwd")
                .getForEntity(locationOfNewNote, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        String title = documentContext.read("$.title");
        String body = documentContext.read("$.body");
        String owner = documentContext.read("$.owner");
        
        assertThat(id).isNotNull();
        assertThat(title).isEqualTo("Test Note 4");
        assertThat(body).isEqualTo("This is test note 4");
        assertThat(owner).isEqualTo("user1");
    }
    
    /*
	 * This unit test is successfully meet when an user updates an existing note and then query it validating that last changes where included 
	 * and also the modified date and time is greater then the original
	 */
    @Test
    @DirtiesContext
    void shouldUpdateAnExistingNote() {
    	// TODO: Complete the unit test based on the test method description
    }

}
