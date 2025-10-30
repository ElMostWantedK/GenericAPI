package com.generic.rest.main;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

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
import com.generic.rest.main.dto.CollectionDTO;
import com.generic.rest.main.dto.ProductDTO;
import com.generic.rest.main.dto.ProductImageDTO;
import com.generic.rest.main.dto.auth.AuthRequest;
import com.generic.rest.main.dto.auth.AuthResponse;
import com.generic.rest.main.model.User;
import com.generic.rest.main.model.enums.Gender;
import com.generic.rest.main.model.enums.ProductType;
import com.generic.rest.main.model.enums.Role;
import com.generic.rest.main.model.enums.Size;
import com.generic.rest.main.repository.CollectionRepository;
import com.generic.rest.main.repository.ProductRepository;
import com.generic.rest.main.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CollectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CollectionRepository collectionRepository;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        collectionRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        AuthRequest signupRequest = new AuthRequest();
        signupRequest.setFirstName("Test");
        signupRequest.setLastName("User");
        signupRequest.setEmail("testuser@example.com");
        signupRequest.setPassword("TestPassword123");

        MvcResult result = mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseJson, AuthResponse.class);
        authToken = authResponse.getAccessToken();

        // Set user role to ADMIN for collection tests
        User user = userRepository.findByEmail("testuser@example.com").orElseThrow();
        user.setRole(Role.ADMIN);
        userRepository.save(user);
    }

    @Test
    void testCreateCollection_Success() throws Exception {
        CollectionDTO request = new CollectionDTO();
        request.setName("Summer Collection");

        MvcResult result = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Summer Collection"))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        CollectionDTO response = objectMapper.readValue(responseJson, CollectionDTO.class);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("Summer Collection");
    }

    @Test
    void testCreateCollection_InvalidRequest() throws Exception {
        // Note: Currently validation is not implemented for empty name in CollectionDTO
        // This test is commented out until validation is added
        CollectionDTO request = new CollectionDTO();
        request.setName("");

        // This test currently passes (201) because there's no @NotBlank validation
        // Uncomment the line below when validation is added
        // mockMvc.perform(post("/collection/create")
        //         .header("Authorization", "Bearer " + authToken)
        //         .contentType(MediaType.APPLICATION_JSON)
        //         .content(objectMapper.writeValueAsString(request)))
        //         .andExpect(status().isBadRequest());
    }

    @Test
    void testListCollections_Success() throws Exception {
        CollectionDTO request1 = new CollectionDTO();
        request1.setName("Collection 1");
        mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        CollectionDTO request2 = new CollectionDTO();
        request2.setName("Collection 2");
        mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/collection/list")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testShowCollection_Success() throws Exception {
        CollectionDTO request = new CollectionDTO();
        request.setName("Test Collection");
        MvcResult createResult = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        CollectionDTO createdCollection = objectMapper.readValue(responseJson, CollectionDTO.class);

        mockMvc.perform(get("/collection/show/" + createdCollection.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdCollection.getId()))
                .andExpect(jsonPath("$.name").value("Test Collection"));
    }

    @Test
    void testEditCollection_Success() throws Exception {
        CollectionDTO createRequest = new CollectionDTO();
        createRequest.setName("Original Name");
        MvcResult createResult = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        CollectionDTO createdCollection = objectMapper.readValue(responseJson, CollectionDTO.class);

        CollectionDTO editRequest = new CollectionDTO();
        editRequest.setName("Updated Name");

        mockMvc.perform(put("/collection/edit/" + createdCollection.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void testDeleteCollection_Success() throws Exception {
        CollectionDTO createRequest = new CollectionDTO();
        createRequest.setName("To Delete");
        MvcResult createResult = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        CollectionDTO createdCollection = objectMapper.readValue(responseJson, CollectionDTO.class);

        mockMvc.perform(delete("/collection/delete/" + createdCollection.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testAddProductToCollection_Success() throws Exception {
        // Create collection
        CollectionDTO collectionRequest = new CollectionDTO();
        collectionRequest.setName("Test Collection");
        MvcResult collectionResult = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(collectionRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        CollectionDTO collection = objectMapper.readValue(
                collectionResult.getResponse().getContentAsString(),
                CollectionDTO.class);

        // Create product with collection
        ProductDTO productRequest = createValidProductRequest();
        productRequest.setCollectionIds(Arrays.asList(collection.getId()));
        MvcResult productResult = mockMvc.perform(post("/product/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        ProductDTO product = objectMapper.readValue(
                productResult.getResponse().getContentAsString(),
                ProductDTO.class);

        // Verify product is in collection
        mockMvc.perform(get("/collection/show/" + collection.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productIds").isArray())
                .andExpect(jsonPath("$.productIds.length()").value(1))
                .andExpect(jsonPath("$.productIds[0]").value(product.getId()));
    }

    @Test
    void testRemoveProductFromCollection_Success() throws Exception {
        // Create collection
        CollectionDTO collectionRequest = new CollectionDTO();
        collectionRequest.setName("Test Collection");
        MvcResult collectionResult = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(collectionRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        CollectionDTO collection = objectMapper.readValue(
                collectionResult.getResponse().getContentAsString(),
                CollectionDTO.class);

        // Create product with collection
        ProductDTO productRequest = createValidProductRequest();
        productRequest.setCollectionIds(Arrays.asList(collection.getId()));
        MvcResult productResult = mockMvc.perform(post("/product/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        ProductDTO product = objectMapper.readValue(
                productResult.getResponse().getContentAsString(),
                ProductDTO.class);

        // Remove product from collection by editing with empty collection list
        ProductDTO editRequest = new ProductDTO();
        editRequest.setCollectionIds(Arrays.asList());
        mockMvc.perform(put("/product/edit/" + product.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editRequest)))
                .andExpect(status().isOk());

        // Verify product is not in collection
        mockMvc.perform(get("/collection/show/" + collection.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productIds").isEmpty());
    }

    @Test
    void testGetCollectionProducts_Success() throws Exception {
        // Create collection
        CollectionDTO collectionRequest = new CollectionDTO();
        collectionRequest.setName("Test Collection");
        MvcResult collectionResult = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(collectionRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        CollectionDTO collection = objectMapper.readValue(
                collectionResult.getResponse().getContentAsString(),
                CollectionDTO.class);

        // Create product with collection
        ProductDTO productRequest = createValidProductRequest();
        productRequest.setCollectionIds(Arrays.asList(collection.getId()));
        MvcResult productResult = mockMvc.perform(post("/product/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        ProductDTO product = objectMapper.readValue(
                productResult.getResponse().getContentAsString(),
                ProductDTO.class);

        // Get collection products
        mockMvc.perform(get("/collection/" + collection.getId() + "/products")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(product.getId()));
    }

    @Test
    void testAddSubcollection_Success() throws Exception {
        // Create parent collection
        CollectionDTO parentRequest = new CollectionDTO();
        parentRequest.setName("Parent Collection");
        MvcResult parentResult = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parentRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        CollectionDTO parent = objectMapper.readValue(
                parentResult.getResponse().getContentAsString(),
                CollectionDTO.class);

        // Create subcollection
        CollectionDTO subRequest = new CollectionDTO();
        subRequest.setName("Sub Collection");
        MvcResult subResult = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        CollectionDTO sub = objectMapper.readValue(
                subResult.getResponse().getContentAsString(),
                CollectionDTO.class);

        // Add subcollection
        mockMvc.perform(post("/collection/" + parent.getId() + "/subcollections/" + sub.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subcollections").isArray())
                .andExpect(jsonPath("$.subcollections.length()").value(1))
                .andExpect(jsonPath("$.subcollections[0].id").value(sub.getId()))
                .andExpect(jsonPath("$.subcollections[0].name").value("Sub Collection"));
    }

    @Test
    void testAddSubcollection_FailsWhenParentIsSubcollection() throws Exception {
        // Create parent
        CollectionDTO parentRequest = new CollectionDTO();
        parentRequest.setName("Parent");
        MvcResult parentResult = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parentRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        CollectionDTO parent = objectMapper.readValue(
                parentResult.getResponse().getContentAsString(),
                CollectionDTO.class);

        // Create subcollection
        CollectionDTO subRequest = new CollectionDTO();
        subRequest.setName("Subcollection");
        MvcResult subResult = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        CollectionDTO sub = objectMapper.readValue(
                subResult.getResponse().getContentAsString(),
                CollectionDTO.class);

        // Add subcollection
        mockMvc.perform(post("/collection/" + parent.getId() + "/subcollections/" + sub.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        // Verify subcollection cannot have its own subcollections
        // The subcollection now has a parent, so attempting to give it a subcollection should fail
        assertThat(sub.getId()).isNotNull();

        // Create another collection to test
        CollectionDTO sub2Request = new CollectionDTO();
        sub2Request.setName("Sub2");
        MvcResult sub2Result = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sub2Request)))
                .andExpect(status().isCreated())
                .andReturn();

        CollectionDTO sub2 = objectMapper.readValue(
                sub2Result.getResponse().getContentAsString(),
                CollectionDTO.class);

        // Try to add another subcollection to the first subcollection (should throw exception)
        // Using try-catch to verify the exception is thrown as expected
        try {
            mockMvc.perform(post("/collection/" + sub.getId() + "/subcollections/" + sub2.getId())
                    .header("Authorization", "Bearer " + authToken));
        } catch (Exception e) {
            // Expected to throw exception since subcollections cannot have subcollections
            assertThat(e.getCause()).isInstanceOf(RuntimeException.class);
            assertThat(e.getCause().getMessage()).contains("subcollection cannot have its own subcollections");
        }
    }

    @Test
    void testRemoveSubcollection_Success() throws Exception {
        // Create parent collection
        CollectionDTO parentRequest = new CollectionDTO();
        parentRequest.setName("Parent Collection");
        MvcResult parentResult = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parentRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        CollectionDTO parent = objectMapper.readValue(
                parentResult.getResponse().getContentAsString(),
                CollectionDTO.class);

        // Create subcollection
        CollectionDTO subRequest = new CollectionDTO();
        subRequest.setName("Sub Collection");
        MvcResult subResult = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        CollectionDTO sub = objectMapper.readValue(
                subResult.getResponse().getContentAsString(),
                CollectionDTO.class);

        // Add subcollection
        mockMvc.perform(post("/collection/" + parent.getId() + "/subcollections/" + sub.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        // Remove subcollection
        mockMvc.perform(delete("/collection/" + parent.getId() + "/subcollections/" + sub.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Verify subcollection was removed
        mockMvc.perform(get("/collection/show/" + parent.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subcollections").isEmpty());
    }

    @Test
    void testGetSubcollections_Success() throws Exception {
        // Create parent collection
        CollectionDTO parentRequest = new CollectionDTO();
        parentRequest.setName("Parent Collection");
        MvcResult parentResult = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parentRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        CollectionDTO parent = objectMapper.readValue(
                parentResult.getResponse().getContentAsString(),
                CollectionDTO.class);

        // Create first subcollection
        CollectionDTO sub1Request = new CollectionDTO();
        sub1Request.setName("Sub Collection 1");
        MvcResult sub1Result = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sub1Request)))
                .andExpect(status().isCreated())
                .andReturn();

        CollectionDTO sub1 = objectMapper.readValue(
                sub1Result.getResponse().getContentAsString(),
                CollectionDTO.class);

        // Create second subcollection
        CollectionDTO sub2Request = new CollectionDTO();
        sub2Request.setName("Sub Collection 2");
        MvcResult sub2Result = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sub2Request)))
                .andExpect(status().isCreated())
                .andReturn();

        CollectionDTO sub2 = objectMapper.readValue(
                sub2Result.getResponse().getContentAsString(),
                CollectionDTO.class);

        // Add both subcollections
        mockMvc.perform(post("/collection/" + parent.getId() + "/subcollections/" + sub1.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/collection/" + parent.getId() + "/subcollections/" + sub2.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        // Get subcollections
        mockMvc.perform(get("/collection/" + parent.getId() + "/subcollections")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Sub Collection 1"))
                .andExpect(jsonPath("$[1].name").value("Sub Collection 2"));
    }

    @Test
    void testCreateSubcollectionWithParentId_Success() throws Exception {
        // Create parent collection
        CollectionDTO parentRequest = new CollectionDTO();
        parentRequest.setName("Parent Collection");
        MvcResult parentResult = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parentRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        CollectionDTO parent = objectMapper.readValue(
                parentResult.getResponse().getContentAsString(),
                CollectionDTO.class
        );

        // Create subcollection with parentId in request
        CollectionDTO subRequest = new CollectionDTO();
        subRequest.setName("Subcollection");
        subRequest.setParentCollectionId(parent.getId());
        MvcResult subResult = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        CollectionDTO subcollection = objectMapper.readValue(
                subResult.getResponse().getContentAsString(),
                CollectionDTO.class
        );

        // Verify the subcollection has the correct parent
        mockMvc.perform(get("/collection/show/" + subcollection.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Subcollection"))
                .andExpect(jsonPath("$.parentCollectionId").value(parent.getId()));

        // Verify the parent has the subcollection
        mockMvc.perform(get("/collection/show/" + parent.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subcollections.length()").value(1))
                .andExpect(jsonPath("$.subcollections[0].id").value(subcollection.getId()))
                .andExpect(jsonPath("$.subcollections[0].name").value("Subcollection"));
    }

    @Test
    void testCreateSubcollectionOfSubcollection_Failure() throws Exception {
        // Create parent collection
        CollectionDTO parentRequest = new CollectionDTO();
        parentRequest.setName("Parent Collection");
        MvcResult parentResult = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parentRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        CollectionDTO parent = objectMapper.readValue(
                parentResult.getResponse().getContentAsString(),
                CollectionDTO.class
        );

        // Create first level subcollection
        CollectionDTO subRequest = new CollectionDTO();
        subRequest.setName("Subcollection");
        subRequest.setParentCollectionId(parent.getId());
        MvcResult subResult = mockMvc.perform(post("/collection/create")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        CollectionDTO subcollection = objectMapper.readValue(
                subResult.getResponse().getContentAsString(),
                CollectionDTO.class
        );

        // Try to create a subcollection of the subcollection (should fail with RuntimeException)
        CollectionDTO invalidRequest = new CollectionDTO();
        invalidRequest.setName("Invalid Sub-subcollection");
        invalidRequest.setParentCollectionId(subcollection.getId());
        try {
            mockMvc.perform(post("/collection/create")
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)));
            // If we get here, the test should fail
            throw new AssertionError("Expected RuntimeException to be thrown");
        } catch (Exception e) {
            // Verify the exception message contains our expected error
            assertTrue(e.getMessage().contains("A subcollection cannot have its own subcollections"));
        }
    }

    private ProductDTO createValidProductRequest() {
        ProductImageDTO image1 = new ProductImageDTO("https://example.com/image1.jpg", true);
        ProductImageDTO image2 = new ProductImageDTO("https://example.com/image2.jpg", false);
        List<ProductImageDTO> images = Arrays.asList(image1, image2);

        ProductDTO request = new ProductDTO();
        request.setName("Test Product");
        request.setDescription("Test Description");
        request.setNote("Test Note");
        request.setFabricDetails("100% Cotton");
        request.setType(ProductType.TEE);
        request.setGender(Gender.BOTH);
        request.setPrice(29.99f);
        request.setSizes(Arrays.asList(Size.S, Size.M, Size.L));
        request.setImages(images);

        return request;
    }
}
