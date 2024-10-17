package api.mpba.rastvdmy.controller;

import api.mpba.rastvdmy.Application;
import api.mpba.rastvdmy.config.SecurityConfig;
import api.mpba.rastvdmy.dto.request.UserLoginRequest;
import api.mpba.rastvdmy.dto.request.UserProfileRequest;
import api.mpba.rastvdmy.dto.response.JwtAuthResponse;
import api.mpba.rastvdmy.entity.enums.UserRole;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = {Application.class, SecurityConfig.class}
)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    public void givenUserRequest_whenSignUp_thenReturnJwtAuthResponse() throws Exception {
        // Given
        UserProfileRequest userProfileRequest = new UserProfileRequest(
                UUID.randomUUID(),
                "John",
                "Doe",
                "2001-01-01",
                "Czechia",
                "john.doe@mpba.com",
                "Qwertyuiop123",
                "+420123456789",
                "avatar.jpg",
                UserStatus.STATUS_DEFAULT,
                UserRole.ROLE_DEFAULT);

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse(3600000, "mock-jwt-token");

        // When
        when(authService.signUp(userProfileRequest)).thenReturn(jwtAuthResponse);

        // Then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userProfileRequest)))
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    public void givenUserLoginRequest_whenAuthenticate_thenReturnJwtAuthResponse() throws Exception {
        // Given
        UserLoginRequest userLoginRequest = new UserLoginRequest("john.doe@example.com", "password");
        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse(3600000, "mock-jwt-token");

        // When
        when(authService.authenticate(userLoginRequest)).thenReturn(jwtAuthResponse);

        // Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginRequest)))
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }
}
