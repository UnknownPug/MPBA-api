package api.mpba.rastvdmy.controller;

import api.mpba.rastvdmy.Application;
import api.mpba.rastvdmy.config.SecurityConfig;
import api.mpba.rastvdmy.dto.request.AdminUpdateUserRequest;
import api.mpba.rastvdmy.dto.request.UserUpdateRequest;
import api.mpba.rastvdmy.entity.UserProfile;
import api.mpba.rastvdmy.entity.enums.UserRole;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.service.UserProfileService;
import api.mpba.rastvdmy.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = {Application.class, SecurityConfig.class}
)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class UserProfileControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserProfileService userProfileService;

    @MockBean
    private AuthenticationManager authManager;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthenticationProvider authProvider;

    private String jwtToken;

    @BeforeEach
    public void setUpAdminUser() {
        UserDetails adminUserDetails = User.withUsername("admin@mpba.com")
                .password("AdminPassword123")
                .authorities("ROLE_ADMIN")
                .build();

        when(jwtService.generateToken(adminUserDetails)).thenReturn("adminJwtToken");
        jwtToken = jwtService.generateToken(adminUserDetails);
        when(jwtService.isTokenValid(jwtToken, adminUserDetails)).thenReturn(true);

        // Set the security context for admin user
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        adminUserDetails, null, adminUserDetails.getAuthorities())
        );
    }

    public void setUpDefaultUser() {
        UserDetails defaultUserDetails = User.withUsername("user@mpba.com")
                .password("UserPassword123")
                .authorities("ROLE_DEFAULT")
                .build();

        when(jwtService.generateToken(defaultUserDetails)).thenReturn("userJwtToken");
        jwtToken = jwtService.generateToken(defaultUserDetails);
        when(jwtService.isTokenValid(jwtToken, defaultUserDetails)).thenReturn(true);

        // Set the security context for default user
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        defaultUserDetails, null, defaultUserDetails.getAuthorities())
        );
    }

    @Test
    public void testGetUsers_ShouldReturn_ListOfUsers() throws Exception {
        // Given
        setUpAdminUser();
        UserProfile userProfile1 = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("John")
                .surname("Doe")
                .dateOfBirth("1990-01-01")
                .countryOfOrigin("Czechia")
                .email("john.doe@example.com")
                .password("Qwertyuiop123")
                .phoneNumber("+420123456789")
                .avatar("avatar.png")
                .status(UserStatus.STATUS_DEFAULT)
                .role(UserRole.ROLE_DEFAULT)
                .build();

        UserProfile userProfile2 = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("Jane")
                .surname("Doe")
                .dateOfBirth("1990-01-01")
                .countryOfOrigin("Czechia")
                .email("jane.doe@example.com")
                .password("Qwertyuiop123")
                .phoneNumber("+420111222333")
                .avatar("avatar.png")
                .status(UserStatus.STATUS_DEFAULT)
                .role(UserRole.ROLE_DEFAULT)
                .build();

        List<UserProfile> userProfiles = List.of(userProfile1, userProfile2);

        // When
        when(userProfileService.getUsers(any(HttpServletRequest.class))).thenReturn(userProfiles);

        // Then
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(userProfile1.getId().toString()))
                .andExpect(jsonPath("$[0].name").value(userProfile1.getName()))
                .andExpect(jsonPath("$[0].surname").value(userProfile1.getSurname()))
                .andExpect(jsonPath("$[0].date_of_birth").value(userProfile1.getDateOfBirth()))
                .andExpect(jsonPath("$[0].country_of_origin").value(userProfile1.getCountryOfOrigin()))
                .andExpect(jsonPath("$[0].email").value(userProfile1.getEmail()))
                .andExpect(jsonPath("$[0].phone_number").value(userProfile1.getPhoneNumber()))
                .andExpect(jsonPath("$[0].avatar").value(userProfile1.getAvatar()))
                .andExpect(jsonPath("$[0].status").value(userProfile1.getStatus().toString()))
                .andExpect(jsonPath("$[0].role").value(userProfile1.getRole().toString()))
                .andExpect(jsonPath("$[1].id").value(userProfile2.getId().toString()))
                .andExpect(jsonPath("$[1].name").value(userProfile2.getName()))
                .andExpect(jsonPath("$[1].surname").value(userProfile2.getSurname()))
                .andExpect(jsonPath("$[1].date_of_birth").value(userProfile2.getDateOfBirth()))
                .andExpect(jsonPath("$[1].country_of_origin").value(userProfile2.getCountryOfOrigin()))
                .andExpect(jsonPath("$[1].email").value(userProfile2.getEmail()))
                .andExpect(jsonPath("$[1].phone_number").value(userProfile2.getPhoneNumber()))
                .andExpect(jsonPath("$[1].avatar").value(userProfile2.getAvatar()))
                .andExpect(jsonPath("$[1].status").value(userProfile2.getStatus().toString()))
                .andExpect(jsonPath("$[1].role").value(userProfile2.getRole().toString()));
    }

    @Test
    public void testFilterAndSortUsers_ShouldReturn_SortedAndFilteredUsers() throws Exception {
        // Given
        setUpAdminUser();
        UserProfile userProfile1 = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("John")
                .surname("Doe")
                .dateOfBirth("1990-01-01")
                .countryOfOrigin("Czechia")
                .email("john.doe@example.com")
                .password("Qwertyuiop123")
                .phoneNumber("+420123456789")
                .avatar("avatar.png")
                .status(UserStatus.STATUS_DEFAULT)
                .role(UserRole.ROLE_DEFAULT)
                .build();

        UserProfile userProfile2 = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("Jane")
                .surname("Doe")
                .dateOfBirth("1990-01-01")
                .countryOfOrigin("Czechia")
                .email("jane.doe@example.com")
                .password("Qwertyuiop123")
                .phoneNumber("+420111222333")
                .avatar("avatar.png")
                .status(UserStatus.STATUS_DEFAULT)
                .role(UserRole.ROLE_DEFAULT)
                .build();

        List<UserProfile> userProfiles = List.of(userProfile1, userProfile2);
        Page<UserProfile> pagedResponse = new PageImpl<>(userProfiles);

        // When
        when(userProfileService.filterAndSortUsers(any(HttpServletRequest.class),
                any(Pageable.class))).thenReturn(pagedResponse);

        // Then
        mockMvc.perform(get("/api/v1/users/filter")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(userProfile1.getId().toString()))
                .andExpect(jsonPath("$.content[0].name").value(userProfile1.getName()))
                .andExpect(jsonPath("$.content[0].surname").value(userProfile1.getSurname()))
                .andExpect(jsonPath("$.content[0].date_of_birth").value(userProfile1.getDateOfBirth()))
                .andExpect(jsonPath("$.content[0].country_of_origin").value(userProfile1.getCountryOfOrigin()))
                .andExpect(jsonPath("$.content[0].email").value(userProfile1.getEmail()))
                .andExpect(jsonPath("$.content[0].phone_number").value(userProfile1.getPhoneNumber()))
                .andExpect(jsonPath("$.content[0].avatar").value(userProfile1.getAvatar()))
                .andExpect(jsonPath("$.content[0].status").value(userProfile1.getStatus().toString()))
                .andExpect(jsonPath("$.content[0].role").value(userProfile1.getRole().toString()))
                .andExpect(jsonPath("$.content[1].id").value(userProfile2.getId().toString()))
                .andExpect(jsonPath("$.content[1].name").value(userProfile2.getName()))
                .andExpect(jsonPath("$.content[1].surname").value(userProfile2.getSurname()))
                .andExpect(jsonPath("$.content[1].date_of_birth").value(userProfile2.getDateOfBirth()))
                .andExpect(jsonPath("$.content[1].country_of_origin").value(userProfile2.getCountryOfOrigin()))
                .andExpect(jsonPath("$.content[1].email").value(userProfile2.getEmail()))
                .andExpect(jsonPath("$.content[1].phone_number").value(userProfile2.getPhoneNumber()))
                .andExpect(jsonPath("$.content[1].avatar").value(userProfile2.getAvatar()))
                .andExpect(jsonPath("$.content[1].status").value(userProfile2.getStatus().toString()));
    }

    @Test
    public void testGetUser_ShouldReturn_UserProfile() throws Exception {
        // Given
        setUpAdminUser();
        UserProfile userProfile = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("John")
                .surname("Doe")
                .dateOfBirth("1990-01-01")
                .countryOfOrigin("Czechia")
                .email("john.doe@example.com")
                .password("Qwertyuiop123")
                .phoneNumber("+420123456789")
                .avatar("avatar.png")
                .status(UserStatus.STATUS_DEFAULT)
                .role(UserRole.ROLE_DEFAULT)
                .build();

        // When
        when(userProfileService.getUser(any(HttpServletRequest.class))).thenReturn(userProfile);

        // Then
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userProfile.getId().toString()))
                .andExpect(jsonPath("$.name").value(userProfile.getName()))
                .andExpect(jsonPath("$.surname").value(userProfile.getSurname()))
                .andExpect(jsonPath("$.date_of_birth").value(userProfile.getDateOfBirth()))
                .andExpect(jsonPath("$.country_of_origin").value(userProfile.getCountryOfOrigin()))
                .andExpect(jsonPath("$.email").value(userProfile.getEmail()))
                .andExpect(jsonPath("$.phone_number").value(userProfile.getPhoneNumber()))
                .andExpect(jsonPath("$.avatar").value(userProfile.getAvatar()))
                .andExpect(jsonPath("$.status").value(userProfile.getStatus().toString()))
                .andExpect(jsonPath("$.role").value(userProfile.getRole().toString()));
    }

    @Test
    public void testGetUserById_ShouldReturn_UserProfile() throws Exception {
        // Given
        setUpAdminUser();
        UUID userId = UUID.randomUUID();
        UserProfile userProfile = UserProfile.builder()
                .id(userId)
                .name("John")
                .surname("Doe")
                .dateOfBirth("1990-01-01")
                .countryOfOrigin("Czechia")
                .email("john.doe@example.com")
                .password("Qwertyuiop123")
                .phoneNumber("+420123456789")
                .avatar("avatar.png")
                .status(UserStatus.STATUS_DEFAULT)
                .role(UserRole.ROLE_DEFAULT)
                .build();

        // When
        when(userProfileService.getUserById(any(HttpServletRequest.class), eq(userId))).thenReturn(userProfile);

        // Then
        mockMvc.perform(get("/api/v1/users/" + userId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userProfile.getId().toString()))
                .andExpect(jsonPath("$.name").value(userProfile.getName()))
                .andExpect(jsonPath("$.surname").value(userProfile.getSurname()))
                .andExpect(jsonPath("$.date_of_birth").value(userProfile.getDateOfBirth()))
                .andExpect(jsonPath("$.country_of_origin").value(userProfile.getCountryOfOrigin()))
                .andExpect(jsonPath("$.email").value(userProfile.getEmail()))
                .andExpect(jsonPath("$.phone_number").value(userProfile.getPhoneNumber()))
                .andExpect(jsonPath("$.avatar").value(userProfile.getAvatar()))
                .andExpect(jsonPath("$.status").value(userProfile.getStatus().toString()))
                .andExpect(jsonPath("$.role").value(userProfile.getRole().toString()));
    }

    @Test
    public void testUpdateUser_ShouldReturn_UserTokenResponse() throws Exception {
        // Given
        setUpDefaultUser();
        UserUpdateRequest updateRequest = new UserUpdateRequest(
                "jana.doe@mpba.com",
                "Qwertyuiop123",
                "+420987654321"
        );

        UserProfile updatedUserProfile = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("Jana")
                .surname("Doe")
                .dateOfBirth("1990-01-01")
                .countryOfOrigin("Czechia")
                .email(updateRequest.email())
                .password(updateRequest.password())
                .phoneNumber(updateRequest.phoneNumber())
                .avatar("avatar.png")
                .status(UserStatus.STATUS_DEFAULT)
                .role(UserRole.ROLE_DEFAULT)
                .build();

        // When
        when(userProfileService.updateUser(any(HttpServletRequest.class), any(UserUpdateRequest.class)))
                .thenReturn(updatedUserProfile);
        when(userProfileService.generateToken(any(UserProfile.class))).thenReturn("new_jwt_token");

        // Then
        mockMvc.perform(patch("/api/v1/users/me")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("new_jwt_token"));
    }

    @Test
    public void testUpdateUserSpecificCredentials_ShouldReturn_UserTokenResponse() throws Exception {
        // Given
        setUpAdminUser();
        UUID userId = UUID.randomUUID();
        AdminUpdateUserRequest updateRequest = new AdminUpdateUserRequest(
                "Valencia",
                "United Kingdom"

        );
        UserProfile updatedUserProfile = UserProfile.builder()
                .id(userId)
                .name("John")
                .surname(updateRequest.surname())
                .dateOfBirth("1990-01-01")
                .countryOfOrigin(updateRequest.countryOfOrigin())
                .email("jana.doe@mpba.com")
                .password("Qwertyuiop123")
                .phoneNumber("+420987654321")
                .avatar("avatar.png")
                .status(UserStatus.STATUS_DEFAULT)
                .role(UserRole.ROLE_DEFAULT)
                .build();

        // When
        when(userProfileService.updateUserSpecificCredentials(any(HttpServletRequest.class),
                eq(userId), any(AdminUpdateUserRequest.class))).thenReturn(updatedUserProfile);
        when(userProfileService.generateToken(any(UserProfile.class))).thenReturn("new_jwt_token");

        // Then
        mockMvc.perform(patch("/api/v1/users/" + userId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("new_jwt_token"));
    }

    @Test
    public void testManageUserAvatar_Upload_ShouldReturn_UserTokenResponse() throws Exception {
        // Given
        setUpDefaultUser();
        MockMultipartFile userAvatar = new MockMultipartFile(
                "user_avatar",
                "avatar.png",
                "image/png",
                "avatar content".getBytes()
        );

        // When
        doNothing().when(userProfileService).uploadUserAvatar(any(HttpServletRequest.class), any(MultipartFile.class));
        when(userProfileService.getUser(any(HttpServletRequest.class))).thenReturn(UserProfile.builder()
                .id(UUID.randomUUID())
                .name("John")
                .surname("Doe")
                .dateOfBirth("1990-01-01")
                .countryOfOrigin("Czechia")
                .email("john.doe@example.com")
                .password("Qwertyuiop123")
                .phoneNumber("+420123456789")
                .avatar("avatar.png")
                .status(UserStatus.STATUS_DEFAULT)
                .role(UserRole.ROLE_DEFAULT)
                .build());
        when(userProfileService.generateToken(any(UserProfile.class))).thenReturn("new_jwt_token");

        // Then
        mockMvc.perform(multipart("/api/v1/users/me/avatar")
                        .file(userAvatar)
                        .param("action", "upload")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("new_jwt_token"));
    }

    @Test
    public void testManageUserAvatar_Remove_ShouldReturn_UserTokenResponse() throws Exception {
        // Given
        setUpDefaultUser();

        // When
        doNothing().when(userProfileService).removeUserAvatar(any(HttpServletRequest.class));
        when(userProfileService.getUser(any(HttpServletRequest.class))).thenReturn(UserProfile.builder()
                .id(UUID.randomUUID())
                .name("John")
                .surname("Doe")
                .dateOfBirth("1990-01-01")
                .countryOfOrigin("Czechia")
                .email("john.doe@example.com")
                .password("Qwertyuiop123")
                .phoneNumber("+420123456789")
                .avatar(null)
                .status(UserStatus.STATUS_DEFAULT)
                .role(UserRole.ROLE_DEFAULT)
                .build());
        when(userProfileService.generateToken(any(UserProfile.class))).thenReturn("new_jwt_token");

        // Then
        mockMvc.perform(multipart("/api/v1/users/me/avatar")
                        .param("action", "remove")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("new_jwt_token"));
    }

    @Test
    public void testManageUserAvatar_InvalidAction_ShouldReturn_BadRequest() throws Exception {
        // Given
        setUpDefaultUser();

        // Then
        mockMvc.perform(patch("/api/v1/users/me/avatar")
                        .param("action", "invalid")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateUserRole_ShouldReturn_NoContent() throws Exception {
        // Given
        setUpAdminUser();
        UUID userId = UUID.randomUUID();

        // When
        doNothing().when(userProfileService).updateUserRole(any(HttpServletRequest.class), eq(userId));

        // Then
        mockMvc.perform(patch("/api/v1/users/role/" + userId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testUpdateUserStatus_ShouldReturn_NoContent() throws Exception {
        // Given
        setUpAdminUser();
        UUID userId = UUID.randomUUID();

        // When
        doNothing().when(userProfileService).updateUserStatus(any(HttpServletRequest.class), eq(userId));

        // Then
        mockMvc.perform(patch("/api/v1/users/status/" + userId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteMyProfile_ShouldReturn_NoContent() throws Exception {
        // Given
        setUpDefaultUser();

        // When
        doNothing().when(userProfileService).deleteUser(any(HttpServletRequest.class));

        // Then
        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteUserProfile_ShouldReturn_NoContent() throws Exception {
        // Given
        setUpAdminUser();
        UUID userId = UUID.randomUUID();

        // When
        doNothing().when(userProfileService).deleteUserByEmail(any(HttpServletRequest.class), eq(userId));

        // Then
        mockMvc.perform(delete("/api/v1/users/" + userId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }
}