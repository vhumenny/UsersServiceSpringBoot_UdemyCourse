package com.appsdeveloperblog.tutorials.junit.ui.controllers;

import com.appsdeveloperblog.tutorials.junit.security.SecurityConstants;
import com.appsdeveloperblog.tutorials.junit.ui.response.UserRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.Arrays;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestPropertySource(locations = "/application-test.properties",
//        properties = "server.port=8081")
@TestMethodOrder()
public class UsersControllerIntegrationTest {

    @Value("${server.port}")
    private int serverPort;

    @LocalServerPort
    private int localServerPort;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    @DisplayName("User can be created")
    void testCreateUser_whenValidDetailsProvided_returnUserDetails() throws JSONException {
        //Arrange

        JSONObject userDetailsRequestJson = new JSONObject();
        userDetailsRequestJson.put("firstName", "Vladimir");
        userDetailsRequestJson.put("lastName", "Gumennyi");
        userDetailsRequestJson.put("email", "test@test.com");
        userDetailsRequestJson.put("password", "12345678");
        userDetailsRequestJson.put("repeatPassword", "12345678");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(userDetailsRequestJson.toString(), headers);

        //Act
        ResponseEntity<UserRest> createdUserDetailsEntity = testRestTemplate
                .postForEntity("/users", request, UserRest.class);
        UserRest createdUserDetails = createdUserDetailsEntity.getBody();

        //Assert
        Assertions.assertEquals(HttpStatus.OK, createdUserDetailsEntity.getStatusCode());
        Assertions.assertEquals(userDetailsRequestJson.getString("firstName"),
                createdUserDetails.getFirstName(),
                "The returned users first name seems to be incorrect.");

    }

    @Test
    @DisplayName("GET /users requires JWT")
    void testGetUsers_whenMissingJWT_returns403() {
        //Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        HttpEntity request = new HttpEntity(null, headers);

        //Act
        ResponseEntity<List<UserRest>> response = testRestTemplate.exchange("/users",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                });

        //Assert
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Http status code 403 FORBIDDEN should have returned");
    }

    @Test
    @DisplayName("/login works")
    void testUserLogin_whenValidCredentialsProvided_returnsJWTinAuthorizationHeader() throws JSONException {
        //Arrange
        JSONObject loginCredentials = new JSONObject();
        loginCredentials.put("email", "test3@test.com");
        loginCredentials.put("password", "12345678");

        HttpEntity<String> request = new HttpEntity<>(loginCredentials.toString());
        //Act
        ResponseEntity response = testRestTemplate.postForEntity("/users/login", request, null);

        //Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(), "Http status code should be 200.");
        Assertions.assertNotNull(response.getHeaders().getValuesAsList(SecurityConstants.HEADER_STRING).get(0),
                "Response should contain Authorization header with JWT");
        Assertions.assertNotNull(response.getHeaders().getValuesAsList("UserID").get(0),
                "Response should contain User id in Response header.");

    }
}
