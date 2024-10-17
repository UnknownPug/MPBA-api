package api.mpba.rastvdmy.controller;

import api.mpba.rastvdmy.Application;
import api.mpba.rastvdmy.config.SecurityConfig;
import api.mpba.rastvdmy.controller.mapper.MessageMapper;
import api.mpba.rastvdmy.dto.request.MessageRequest;
import api.mpba.rastvdmy.dto.response.MessageResponse;
import api.mpba.rastvdmy.entity.Message;
import api.mpba.rastvdmy.entity.UserProfile;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.security.core.userdetails.User;
import api.mpba.rastvdmy.entity.enums.UserRole;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.service.JwtService;
import api.mpba.rastvdmy.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = {Application.class, SecurityConfig.class}
)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class MessageControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @InjectMocks
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockBean
    private MessageService messageService;

    @MockBean
    private MessageMapper messageMapper;

    private String jwtToken;
    private UserProfile sender;
    private UserProfile receiver;

    @BeforeEach
    public void setUp() {
        UserDetails userDetails = User.withUsername("john.doe@mpba.com")
                .password("Qwertyuiop123")
                .authorities("ROLE_DEFAULT")
                .build();

        when(jwtService.generateToken(userDetails)).thenReturn("mockedJwtToken");
        jwtToken = jwtService.generateToken(userDetails);
        when(jwtService.isTokenValid(jwtToken, userDetails)).thenReturn(true);

        // Set the security context
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        sender = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("John")
                .surname("Doe")
                .dateOfBirth("2001-01-01")
                .countryOrigin("Czechia")
                .email("john.doe@mpba.com")
                .password("Qwertyuiop123")
                .phoneNumber("+420123456789")
                .avatar("avatar.jpg")
                .status(UserStatus.STATUS_DEFAULT)
                .role(UserRole.ROLE_DEFAULT)
                .build();

        receiver = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("Jane")
                .surname("Doe")
                .dateOfBirth("2001-01-01")
                .countryOrigin("Czechia")
                .email("jane.doe@mpba.com")
                .password("Qwertyuiop123")
                .phoneNumber("+420133436589")
                .avatar("avatar.jpg")
                .status(UserStatus.STATUS_DEFAULT)
                .role(UserRole.ROLE_DEFAULT)
                .build();
    }

    @Test
    public void callGetMessages_ShouldReturn_ListOfMessages() throws Exception {
        // Given
        Message message1 = Message.builder()
                .id(UUID.randomUUID())
                .content("Hi Jane")
                .timestamp(LocalDateTime.now())
                .sender(sender)
                .receiver(receiver)
                .build();

        Message message2 = Message.builder()
                .id(UUID.randomUUID())
                .content("Hello John")
                .timestamp(LocalDateTime.now())
                .sender(receiver)
                .receiver(sender)
                .build();

        List<Message> messages = List.of(message1, message2);

        // When
        when(messageService.getMessages()).thenReturn(messages);
        when(messageMapper.toResponse(any(MessageRequest.class))).thenAnswer(invocation -> {
            MessageRequest messageRequest = invocation.getArgument(0);
            return new MessageResponse(
                    messageRequest.receiverEmail(),
                    messageRequest.content(),
                    messageRequest.senderEmail(),
                    messageRequest.timestamp()
            );
        });

        // Then
        mockMvc.perform(
                        get("/api/v1/messages")
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].receiver_email").value(message1.getReceiver().getEmail()))
                .andExpect(jsonPath("$[0].content").value(message1.getContent()))
                .andExpect(jsonPath("$[0].sender_email").value(message1.getSender().getEmail()))
                .andExpect(jsonPath("$[1].receiver_email").value(message2.getReceiver().getEmail()))
                .andExpect(jsonPath("$[1].content").value(message2.getContent()))
                .andExpect(jsonPath("$[1].sender_email").value(message2.getSender().getEmail()));
    }

    @Test
    public void callSendMessage_ShouldReturn_CreatedMessage() throws Exception {
        // Given
        MessageRequest messageRequest = new MessageRequest(
                "jane.doe@mpba.com",
                "Hello Jane",
                "john.doe@mpba.com",
                LocalDateTime.now()
        );

        Message message = Message.builder()
                .id(UUID.randomUUID())
                .content("Hi Jane")
                .timestamp(LocalDateTime.now())
                .sender(sender)
                .receiver(receiver)
                .build();

        // When
        when(messageService.sendMessage(
                any(HttpServletRequest.class), eq(messageRequest.receiverEmail()), eq(messageRequest.content()))
        ).thenReturn(message);
        when(messageMapper.toResponse(any(MessageRequest.class))).thenReturn(new MessageResponse(
                message.getReceiver().getEmail(),
                message.getContent(),
                message.getSender().getEmail(),
                message.getTimestamp()
        ));

        // Then
        mockMvc.perform(
                        post("/api/v1/messages")
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(messageRequest))
                )
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.receiver_email").value(message.getReceiver().getEmail()))
                .andExpect(jsonPath("$.content").value(message.getContent()))
                .andExpect(jsonPath("$.sender_email").value(message.getSender().getEmail()));
    }
}
