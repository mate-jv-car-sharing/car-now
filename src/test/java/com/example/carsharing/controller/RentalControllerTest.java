package com.example.carsharing.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carsharing.dto.rental.CreateRentalRequestDto;
import com.example.carsharing.dto.rental.RentalResponseDto;
import com.example.carsharing.exception.EntityNotFoundException;
import com.example.carsharing.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RentalControllerTest {
    public static final String RENTALS_URL = "/rentals";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private com.example.carsharing.service.rental.RentalService rentalService;

    @Test
    @DisplayName("Returns 201 Created when a valid rental request is made")
    void create_WhenValidRequest_ThenReturnsCreated() throws Exception {
        User user = mockUser(1L);
        CreateRentalRequestDto request = getValidCreateRentalRequestDto();

        RentalResponseDto response = new RentalResponseDto(
                10L,
                LocalDate.now(),
                request.returnDate(),
                null,
                request.carId(),
                user.getId()
        );

        when(rentalService.create(any(User.class), any(CreateRentalRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post(RENTALS_URL)
                        .with(authentication(authCustomer(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.carId").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.returnDate").value(request.returnDate().toString()));

        verify(rentalService).create(any(User.class), eq(request));
    }

    @Test
    @DisplayName("Returns 400 Bad Request when an invalid rental request is made")
    void create_WhenInvalidRequest_ThenReturns400() throws Exception {
        User user = mockUser(1L);

        CreateRentalRequestDto invalidRequest = new CreateRentalRequestDto(
                null,
                1L
        );

        mockMvc.perform(post(RENTALS_URL)
                        .with(authentication(authCustomer(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors").value(hasItem(containsString("returnDate"))));
    }

    @Test
    @DisplayName("Returns 403 Forbidden when user who is not manager/customer is trying to create a rental")
    void create_WhenUserHasInvalidRole_ThenReturns403() throws Exception {
        User user = mockUser(1L);
        CreateRentalRequestDto request = getValidCreateRentalRequestDto();

        mockMvc.perform(post(RENTALS_URL)
                        .with(authentication(authWithRole(user, "ROLE_DRIVER"))) // invalid for this endpoint
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Returns 200 OK when rental exists by id")
    void findById_WhenValidId_ThenReturns200() throws Exception {
        User user = mockUser(1L);
        Long id = 10L;

        RentalResponseDto response = new RentalResponseDto(
                id,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5),
                null,
                1L,
                user.getId()
        );

        when(rentalService.findById(any(User.class), eq(id))).thenReturn(response);

        mockMvc.perform(get(RENTALS_URL + "/{id}", id)
                        .with(authentication(authCustomer(user))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.carId").value(1));

        verify(rentalService).findById(any(User.class), eq(id));
    }

    @Test
    @DisplayName("Returns 404 Not Found when rental does not exist")
    void findById_WhenRentalNotFound_ThenReturns404() throws Exception {
        User user = mockUser(1L);
        long missingId = 999_999L;

        when(rentalService.findById(any(User.class), eq(missingId)))
                .thenThrow(new EntityNotFoundException("Can't find rental with id " + missingId));

        mockMvc.perform(get(RENTALS_URL + "/{id}", missingId)
                        .with(authentication(authCustomer(user))))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Returns 400 Bad Request when rental id is invalid")
    void findById_WhenInvalidId_ThenReturns400() throws Exception {
        User user = mockUser(1L);

        mockMvc.perform(get(RENTALS_URL + "/{id}", "invalid-id")
                        .with(authentication(authCustomer(user))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Returns 200 OK when getting rentals with default pageable")
    void getRentals_WhenDefaultPageable_ThenReturns200() throws Exception {
        User user = mockUser(1L);

        List<RentalResponseDto> content = List.of(
                new RentalResponseDto(1L, LocalDate.now(), LocalDate.now().plusDays(3), null, 1L, 1L),
                new RentalResponseDto(2L, LocalDate.now(), LocalDate.now().plusDays(5), null, 2L, 1L)
        );

        when(rentalService.getRentals(any(User.class), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(content, PageRequest.of(0, 15), 2));

        mockMvc.perform(get(RENTALS_URL)
                        .with(authentication(authCustomer(user)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(15))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2));

        verify(rentalService).getRentals(any(User.class), any(), any(Pageable.class));
    }

    @Test
    @DisplayName("Returns 204 No Content when returning rental")
    void returnRental_WhenValidId_ThenReturns204() throws Exception {
        User user = mockUser(1L);
        Long id = 10L;

        doNothing().when(rentalService).returnRental(any(User.class), eq(id));

        mockMvc.perform(post(RENTALS_URL + "/{id}/return", id)
                        .with(authentication(authCustomer(user))))
                .andExpect(status().isNoContent());

        verify(rentalService).returnRental(any(User.class), eq(id));
    }

    @Test
    @DisplayName("Returns 404 Not Found when returning non-existing rental")
    void returnRental_WhenRentalNotFound_ThenReturns404() throws Exception {
        User user = mockUser(1L);
        Long missingId = 999_999L;

        doThrow(new EntityNotFoundException("Can't find rental with id " + missingId))
                .when(rentalService).returnRental(any(User.class), eq(missingId));

        mockMvc.perform(post(RENTALS_URL + "/{id}/return", missingId)
                        .with(authentication(authCustomer(user))))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.error").exists());
    }

    private CreateRentalRequestDto getValidCreateRentalRequestDto() {
        return new CreateRentalRequestDto(
                LocalDate.now().plusDays(3),
                1L
        );
    }

    private User mockUser(Long id) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private UsernamePasswordAuthenticationToken authCustomer(User user) {
        return authWithRole(user, "ROLE_CUSTOMER");
    }

    private UsernamePasswordAuthenticationToken authWithRole(User user, String role) {
        return new UsernamePasswordAuthenticationToken(
                user,
                "N/A",
                List.of(new SimpleGrantedAuthority(role))
        );
    }
}
