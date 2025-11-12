package com.boombet.auth_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.boombet.auth_service.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
	}

	@Test
	void registerEndpoint_shouldCreateUser_whenRequestIsValid() throws Exception {
		String userJson = "{\"email\": \"test.from.java@example.com\", \"password\": \"password123\"}";
		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(userJson))
				.andExpect(status().isOk())
				.andExpect(content().string("User registered successfully!"));
		assertTrue(userRepository.findByEmail("test.from.java@example.com").isPresent(),
				"User should be saved to the database");
	}
	
	@Test
	void registerEndpoint_shouldReturnBadRequest_whenEmailIsAlreadyTaken() throws Exception {
		String userJson = "{\"email\": \"duplicate@example.com\", \"password\": \"password123\"}";
		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(userJson));
		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(userJson))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("Email already taken"));
	}
}
