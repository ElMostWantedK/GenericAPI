package com.generic.rest.main;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.generic.rest.main.dto.auth.AuthRequest;
import com.generic.rest.main.dto.auth.AuthResponse;
import com.generic.rest.main.model.User;
import com.generic.rest.main.model.enums.Role;
import com.generic.rest.main.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testSignup_Success() throws Exception {
        AuthRequest signupRequest = new AuthRequest();
        signupRequest.setFirstName("John");
        signupRequest.setLastName("Doe");
        signupRequest.setEmail("john.doe@example.com");
        signupRequest.setPassword("SecurePass123");

        MvcResult result = mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseJson, AuthResponse.class);

        assertThat(authResponse.getAccessToken()).isNotNull();
        assertThat(userRepository.findByEmail("john.doe@example.com")).isPresent();
    }

    @Test
    void testSignup_InvalidEmail() throws Exception {
        AuthRequest signupRequest = new AuthRequest();
        signupRequest.setFirstName("John");
        signupRequest.setLastName("Doe");
        signupRequest.setEmail("invalid-email");
        signupRequest.setPassword("SecurePass123");

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignup_ShortPassword() throws Exception {
        AuthRequest signupRequest = new AuthRequest();
        signupRequest.setFirstName("John");
        signupRequest.setLastName("Doe");
        signupRequest.setEmail("john@example.com");
        signupRequest.setPassword("short");

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignup_DuplicateEmail() throws Exception {
        AuthRequest signupRequest = new AuthRequest();
        signupRequest.setFirstName("John");
        signupRequest.setLastName("Doe");
        signupRequest.setEmail("duplicate@example.com");
        signupRequest.setPassword("SecurePass123");

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void testLogin_Success() throws Exception {
        AuthRequest signupRequest = new AuthRequest();
        signupRequest.setFirstName("Jane");
        signupRequest.setLastName("Smith");
        signupRequest.setEmail("jane.smith@example.com");
        signupRequest.setPassword("MyPassword123");

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setEmail("jane.smith@example.com");
        loginRequest.setPassword("MyPassword123");

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseJson, AuthResponse.class);

        assertThat(authResponse.getAccessToken()).isNotNull();
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        AuthRequest signupRequest = new AuthRequest();
        signupRequest.setFirstName("Test");
        signupRequest.setLastName("User");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("CorrectPass123");

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("WrongPassword");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogin_NonExistentUser() throws Exception {
        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("SomePassword123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetRole_User() throws Exception {
        AuthRequest signupRequest = new AuthRequest();
        signupRequest.setFirstName("Test");
        signupRequest.setLastName("User");
        signupRequest.setEmail("testuser@example.com");
        signupRequest.setPassword("TestPassword123");

        MvcResult signupResult = mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String signupResponseJson = signupResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(signupResponseJson, AuthResponse.class);
        String authToken = authResponse.getAccessToken();

        MvcResult result = mockMvc.perform(get("/auth/role")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        AuthResponse roleResponse = objectMapper.readValue(responseJson, AuthResponse.class);

        assertThat(roleResponse.getRole()).isEqualTo("USER");
    }

    @Test
    void testGetRole_Admin() throws Exception {
        AuthRequest signupRequest = new AuthRequest();
        signupRequest.setFirstName("Admin");
        signupRequest.setLastName("User");
        signupRequest.setEmail("admin@example.com");
        signupRequest.setPassword("AdminPassword123");

        MvcResult signupResult = mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String signupResponseJson = signupResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(signupResponseJson, AuthResponse.class);
        String authToken = authResponse.getAccessToken();

        // Set user role to ADMIN
        User user = userRepository.findByEmail("admin@example.com").orElseThrow();
        user.setRole(Role.ADMIN);
        userRepository.save(user);

        MvcResult result = mockMvc.perform(get("/auth/role")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        AuthResponse roleResponse = objectMapper.readValue(responseJson, AuthResponse.class);

        assertThat(roleResponse.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void testGetRole_WithoutAuth() throws Exception {
        mockMvc.perform(get("/auth/role"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetAccountMenu_User() throws Exception {
        AuthRequest signupRequest = new AuthRequest();
        signupRequest.setFirstName("Test");
        signupRequest.setLastName("User");
        signupRequest.setEmail("menuuser@example.com");
        signupRequest.setPassword("TestPassword123");

        MvcResult signupResult = mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String signupResponseJson = signupResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(signupResponseJson, AuthResponse.class);
        String authToken = authResponse.getAccessToken();

        MvcResult result = mockMvc.perform(get("/auth/account-menu")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0]").value("ORDER"))
                .andExpect(jsonPath("$.items[1]").value("ADDRESS"))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        AuthResponse menuResponse = objectMapper.readValue(responseJson, AuthResponse.class);

        assertThat(menuResponse.getItems()).hasSize(2);
        assertThat(menuResponse.getItems()).containsExactly("ORDER", "ADDRESS");
    }

    @Test
    void testGetAccountMenu_Admin() throws Exception {
        AuthRequest signupRequest = new AuthRequest();
        signupRequest.setFirstName("Admin");
        signupRequest.setLastName("User");
        signupRequest.setEmail("menuadmin@example.com");
        signupRequest.setPassword("AdminPassword123");

        MvcResult signupResult = mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String signupResponseJson = signupResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(signupResponseJson, AuthResponse.class);
        String authToken = authResponse.getAccessToken();

        // Set user role to ADMIN
        User user = userRepository.findByEmail("menuadmin@example.com").orElseThrow();
        user.setRole(Role.ADMIN);
        userRepository.save(user);

        MvcResult result = mockMvc.perform(get("/auth/account-menu")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(4))
                .andExpect(jsonPath("$.items[0]").value("ORDER"))
                .andExpect(jsonPath("$.items[1]").value("ADDRESS"))
                .andExpect(jsonPath("$.items[2]").value("PRODUCTS"))
                .andExpect(jsonPath("$.items[3]").value("BILLING"))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        AuthResponse menuResponse = objectMapper.readValue(responseJson, AuthResponse.class);

        assertThat(menuResponse.getItems()).hasSize(4);
        assertThat(menuResponse.getItems()).containsExactly("ORDER", "ADDRESS", "PRODUCTS", "BILLING");
    }

    @Test
    void testGetAccountMenu_WithoutAuth() throws Exception {
        mockMvc.perform(get("/auth/account-menu"))
                .andExpect(status().isForbidden());
    }
}
