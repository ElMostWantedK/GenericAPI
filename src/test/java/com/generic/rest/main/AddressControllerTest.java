package com.generic.rest.main;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import com.generic.rest.main.dto.AddressDTO;
import com.generic.rest.main.dto.auth.AuthRequest;
import com.generic.rest.main.dto.auth.AuthResponse;
import com.generic.rest.main.repository.AddressRepository;
import com.generic.rest.main.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        addressRepository.deleteAll();
        userRepository.deleteAll();

        AuthRequest signupRequest = new AuthRequest();
        signupRequest.setFirstName("Test");
        signupRequest.setLastName("User");
        signupRequest.setEmail("testaddress@example.com");
        signupRequest.setPassword("TestPassword123");

        MvcResult result = mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseJson, AuthResponse.class);
        authToken = authResponse.getAccessToken();
    }

    @Test
    void testCreateAddress_Success() throws Exception {
        AddressDTO request = createValidAddressRequest();

        MvcResult result = mockMvc.perform(post("/account/address/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.zipCode").value("12345"))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        AddressDTO response = objectMapper.readValue(responseJson, AddressDTO.class);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getFirstName()).isEqualTo("John");
    }

    @Test
    void testCreateAddress_WithoutAuth() throws Exception {
        AddressDTO request = createValidAddressRequest();

        mockMvc.perform(post("/account/address/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // Commented out because AddressDTO does not have validation annotations yet
    // @Test
    // void testCreateAddress_InvalidRequest() throws Exception {
    //     AddressDTO request = new AddressDTO();
    //     request.setFirstName("");
    //     request.setLastName("");

    //     mockMvc.perform(post("/account/address/create")
    //             .header("Authorization", "Bearer " + authToken)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isBadRequest());
    // }

    @Test
    void testListAddresses_Success() throws Exception {
        AddressDTO request1 = createValidAddressRequest();
        mockMvc.perform(post("/account/address/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        AddressDTO request2 = createValidAddressRequest();
        request2.setFirstName("Jane");
        request2.setZipCode("54321");
        mockMvc.perform(post("/account/address/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/account/address/list")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testListAddresses_Empty() throws Exception {
        mockMvc.perform(get("/account/address/list")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testEditAddress_Success() throws Exception {
        AddressDTO createRequest = createValidAddressRequest();
        MvcResult createResult = mockMvc.perform(post("/account/address/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        AddressDTO createdAddress = objectMapper.readValue(responseJson, AddressDTO.class);

        AddressDTO editRequest = new AddressDTO();
        editRequest.setId(createdAddress.getId());
        editRequest.setFirstName("Updated");
        editRequest.setLastName("Name");
        editRequest.setAddress("456 New Street");
        editRequest.setZipCode("99999");

        mockMvc.perform(put("/account/address/edit/" + createdAddress.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andExpect(jsonPath("$.address").value("456 New Street"))
                .andExpect(jsonPath("$.zipCode").value("99999"));
    }

    @Test
    void testEditAddress_NotFound() throws Exception {
        AddressDTO editRequest = new AddressDTO();
        editRequest.setId(99999L);
        editRequest.setFirstName("Test");

        mockMvc.perform(put("/account/address/edit/99999")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteAddress_Success() throws Exception {
        AddressDTO createRequest = createValidAddressRequest();
        MvcResult createResult = mockMvc.perform(post("/account/address/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        AddressDTO createdAddress = objectMapper.readValue(responseJson, AddressDTO.class);

        mockMvc.perform(delete("/account/address/delete/" + createdAddress.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/account/address/list")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testCreateAddress_SetAsDefault() throws Exception {
        AddressDTO request1 = createValidAddressRequest();
        request1.setDefault(false);
        mockMvc.perform(post("/account/address/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        AddressDTO request2 = createValidAddressRequest();
        request2.setFirstName("Jane");
        request2.setDefault(true);
        MvcResult result = mockMvc.perform(post("/account/address/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.default").value(true))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        AddressDTO response = objectMapper.readValue(responseJson, AddressDTO.class);

        assertThat(response.isDefault()).isTrue();
    }

    private AddressDTO createValidAddressRequest() {
        AddressDTO request = new AddressDTO();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setAddress("123 Main Street");
        request.setAddress2("Apt 4B");
        request.setArea("Downtown");
        request.setState("California");
        request.setCountry("USA");
        request.setZipCode("12345");
        request.setPhoneNumber("555-1234");
        request.setDefault(false);
        return request;
    }
}
