package com.example.carsharing.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carsharing.dto.car.CarRequestDto;
import com.example.carsharing.model.enums.CarType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CarControllerTest {
    public static final String CARS_URL = "/cars";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Returns 201 Created when a valid car request is made")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Sql(scripts = "/database/delete-all-cars.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void create_WhenValidRequest_ThenReturnsCreated() throws Exception {
        CarRequestDto request = getValidCarRequestDto();

        mockMvc.perform(post(CARS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.model").value(request.model()))
                .andExpect(jsonPath("$.brand").value(request.brand()))
                .andExpect(jsonPath("$.type").value(request.type().name()))
                .andExpect(jsonPath("$.inventory").value(request.inventory()))
                .andExpect(jsonPath("$.dailyFee").value(500.00));
    }

    @Test
    @DisplayName("Returns 400 Bad Request when an invalid car request is made")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Sql(scripts = "/database/delete-all-cars.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void create_WhenInvalidRequest_ThenReturns400() throws Exception {
        CarRequestDto invalidRequest = new CarRequestDto(
                "",
                "",
                CarType.SEDAN,
                5,
                BigDecimal.valueOf(500.00)
        );

        mockMvc.perform(post(CARS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors").value(hasItem(containsString("model"))))
                .andExpect(jsonPath("$.errors").value(hasItem(containsString("brand"))));
    }

    @Test
    @DisplayName("Returns 403 Forbidden when user who is not manager is trying to create a car")
    @WithMockUser(username = "user", roles = {"CUSTOMER"})
    void create_WhenUserIsNotManager_ThenReturns403() throws Exception {
        CarRequestDto request = getValidCarRequestDto();

        mockMvc.perform(post(CARS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Returns 200 OK when getting cars with default pageable")
    @WithMockUser(username = "user", roles = {"CUSTOMER"})
    @Sql(scripts = {
            "/database/delete-all-cars.sql",
            "/database/add-cars.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/database/delete-all-cars.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAll_WhenDefaultPageable_ThenReturns200() throws Exception {
        mockMvc.perform(get(CARS_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(15))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content.length()").value(3))

                .andExpect(jsonPath("$.content[0].model").value("Model S"))
                .andExpect(jsonPath("$.content[1].model").value("X5"))
                .andExpect(jsonPath("$.content[2].model").value("Civic"));
    }

    @Test
    @DisplayName("Returns 200 OK when car exists by id")
    @WithMockUser(username = "user", roles = {"CUSTOMER"})
    @Sql(scripts = {"/database/delete-all-cars.sql", "/database/add-cars.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/database/delete-all-cars.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findById_WhenValidId_ThenReturns200() throws Exception {
        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM cars WHERE model = 'Model S' LIMIT 1",
                Long.class
        );

        mockMvc.perform(get(CARS_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.model").value("Model S"))
                .andExpect(jsonPath("$.brand").value("Tesla"))
                .andExpect(jsonPath("$.type").value("SEDAN"))
                .andExpect(jsonPath("$.inventory").value(10))
                .andExpect(jsonPath("$.dailyFee").value(120.0));
    }

    @Test
    @DisplayName("Returns 404 Not Found when car does not exist")
    @WithMockUser(username = "user", roles = {"CUSTOMER"})
    @Sql(scripts = {"/database/delete-all-cars.sql", "/database/add-cars.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/database/delete-all-cars.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findById_WhenCarNotFound_ThenReturns404() throws Exception {
        long missingId = 999_999L;

        mockMvc.perform(get(CARS_URL + "/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Returns 200 OK when manager updates existing car")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Sql(scripts = {"/database/delete-all-cars.sql", "/database/add-cars.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/database/delete-all-cars.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void update_WhenManagerAndValidRequest_ThenReturns200() throws Exception {
        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM cars WHERE model = 'Model S' LIMIT 1",
                Long.class
        );

        CarRequestDto updateRequest = new CarRequestDto(
                "Model S Plaid",
                "Tesla",
                CarType.SEDAN,
                7,
                BigDecimal.valueOf(130.00)
        );

        mockMvc.perform(put(CARS_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.model").value("Model S Plaid"))
                .andExpect(jsonPath("$.brand").value("Tesla"))
                .andExpect(jsonPath("$.type").value("SEDAN"))
                .andExpect(jsonPath("$.inventory").value(7))
                .andExpect(jsonPath("$.dailyFee").value(130.0));
    }

    @Test
    @DisplayName("Returns 404 Not Found when manager updates non-existing car")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Sql(scripts = {"/database/delete-all-cars.sql", "/database/add-cars.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/database/delete-all-cars.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void update_WhenCarNotFound_ThenReturns404() throws Exception {
        long missingId = 999_999L;

        CarRequestDto updateRequest = new CarRequestDto(
                "Any",
                "Brand",
                CarType.SEDAN,
                1,
                BigDecimal.valueOf(50.00)
        );

        mockMvc.perform(put(CARS_URL + "/{id}", missingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Returns 204 No Content when manager deletes existing car")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Sql(scripts = {"/database/delete-all-cars.sql", "/database/add-cars.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/database/delete-all-cars.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void delete_WhenManagerAndValidId_ThenReturns204() throws Exception {
        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM cars WHERE model = 'Model S' LIMIT 1",
                Long.class
        );
        mockMvc.perform(delete(CARS_URL + "/{id}", id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(CARS_URL + "/{id}", id))
                .andExpect(status().isNotFound());
    }

    private CarRequestDto getValidCarRequestDto() {
        return new CarRequestDto(
                "Model 3",
                "Tesla",
                CarType.SEDAN,
                5,
                BigDecimal.valueOf(500.00)
        );
    }
}
