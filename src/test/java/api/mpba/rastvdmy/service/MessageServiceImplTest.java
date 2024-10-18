package api.mpba.rastvdmy.service;

import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.entity.Message;
import api.mpba.rastvdmy.entity.UserProfile;
import api.mpba.rastvdmy.entity.enums.UserRole;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.MessageRepository;
import api.mpba.rastvdmy.repository.UserProfileRepository;
import api.mpba.rastvdmy.service.impl.BankAccountServiceImpl;
import api.mpba.rastvdmy.service.impl.MessageServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserProfile sender;

    @Mock
    private UserProfile receiver;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private MessageServiceImpl messageService;

    @BeforeEach
    void setUp() {
        sender = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("John")
                .surname("Doe")
                .dateOfBirth("2001-01-01")
                .countryOfOrigin("Czechia")
                .email("jhondoe@mpba.com")
                .password("Password123")
                .phoneNumber("+420123456789")
                .avatar("User.png")
                .status(UserStatus.STATUS_DEFAULT)
                .role(UserRole.ROLE_DEFAULT)
                .build();
        userProfileRepository.save(sender);

        receiver = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("Jane")
                .surname("Doe")
                .dateOfBirth("2001-01-01")
                .countryOfOrigin("Czechia")
                .email("janedoe@mpba.com")
                .password("Password123")
                .phoneNumber("+420111222333")
                .avatar("User.png")
                .status(UserStatus.STATUS_DEFAULT)
                .role(UserRole.ROLE_DEFAULT)
                .build();
        userProfileRepository.save(receiver);
    }

    @Test
    void getMessages_ShouldReturnDecryptedMessages() {
        List<Message> messages = List.of(
                new Message(UUID.randomUUID(), "encryptedContent", LocalDateTime.now(), sender, receiver),
                new Message(UUID.randomUUID(), "encryptedContent2", LocalDateTime.now(), sender, receiver)
        );

        when(messageRepository.findAll()).thenReturn(messages);

        try (MockedStatic<EncryptionUtil> encryptionMock = Mockito.mockStatic(EncryptionUtil.class)) {
            SecretKey mockKey = mock(SecretKey.class);

            encryptionMock.when(EncryptionUtil::getSecretKey).thenReturn(mockKey);

            encryptionMock.when(() -> EncryptionUtil.decrypt(eq("encryptedContent"), eq(mockKey)))
                    .thenReturn("decryptedContent1");
            encryptionMock.when(() -> EncryptionUtil.decrypt(eq("encryptedContent2"), eq(mockKey)))
                    .thenReturn("decryptedContent2");

            List<Message> result = messageService.getMessages();

            assertNotNull(result);
            assertEquals(2, result.size());

            assertEquals("decryptedContent1", result.get(0).getContent());
            assertEquals("decryptedContent2", result.get(1).getContent());

            verify(messageRepository).findAll();
        }
    }

    @Test
    void getMessages_ShouldReturnEmptyList_WhenNoMessagesPresent() {
        when(messageRepository.findAll()).thenReturn(Collections.emptyList());

        List<Message> result = messageService.getMessages();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(messageRepository).findAll();
    }

    @Test
    void getMessages_ShouldThrowApplicationException_WhenDecryptionFails() {
        List<Message> messages = List.of(
                new Message(UUID.randomUUID(), "encryptedContent", LocalDateTime.now(), sender, receiver)
        );

        when(messageRepository.findAll()).thenReturn(messages);

        try (MockedStatic<EncryptionUtil> encryptionMock = Mockito.mockStatic(EncryptionUtil.class)) {
            SecretKey mockKey = mock(SecretKey.class);

            encryptionMock.when(EncryptionUtil::getSecretKey).thenReturn(mockKey);

            encryptionMock.when(() -> EncryptionUtil.decrypt(eq("encryptedContent"), eq(mockKey)))
                    .thenThrow(new RuntimeException("Decryption error"));

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> messageService.getMessages());

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
            assertEquals("Error while decrypting account data.", exception.getMessage());

            verify(messageRepository).findAll();
        }
    }

    @Test
    void sendMessage_ShouldSendSuccessfully() throws Exception {
        when(userProfileRepository.findByEmail("janedoe@mpba.com")).thenReturn(Optional.of(receiver));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<EncryptionUtil> encryptionMock = Mockito.mockStatic(EncryptionUtil.class)) {
            SecretKey mockKey = mock(SecretKey.class);
            encryptionMock.when(EncryptionUtil::getSecretKey).thenReturn(mockKey);
            encryptionMock.when(
                    () -> EncryptionUtil.encrypt("Hello, Jane!", mockKey)
            ).thenReturn("encryptedContent");

            UserProfile mockSender = spy(sender);
            try (MockedStatic<BankAccountServiceImpl> bankAccountServiceMock =
                         Mockito.mockStatic(BankAccountServiceImpl.class)) {
                bankAccountServiceMock.when(
                        () -> BankAccountServiceImpl.getUserData(request, jwtService, userProfileRepository)
                ).thenReturn(mockSender);

                Message result = messageService.sendMessage(
                        request,
                        "janedoe@mpba.com",
                        "Hello, Jane!"
                );

                assertNotNull(result);
                assertEquals("encryptedContent", result.getContent());
                assertThat(result.getSender()).usingRecursiveComparison().isEqualTo(sender);
                assertEquals(receiver, result.getReceiver());
                verify(messageRepository).save(any(Message.class));
            }
        }
    }

    @Test
    void sendMessage_ShouldThrowException_WhenUserIsBlocked() {
        sender.setStatus(UserStatus.STATUS_BLOCKED);

        try (MockedStatic<BankAccountServiceImpl> bankAccountServiceMock =
                     Mockito.mockStatic(BankAccountServiceImpl.class)) {
            bankAccountServiceMock.when(
                    () -> BankAccountServiceImpl.getUserData(request, jwtService, userProfileRepository)
            ).thenReturn(sender);

            ApplicationException exception = assertThrows(ApplicationException.class, () ->
                    messageService.sendMessage(request, "janedoe@mpba.com", "Hello, Jane!"));

            assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
            assertEquals("Operation is forbidden. User is blocked.", exception.getMessage());
        }
    }

    @Test
    void sendMessage_ShouldThrowException_WhenContentIsEmpty() {
        try (MockedStatic<BankAccountServiceImpl> bankAccountServiceMock =
                     Mockito.mockStatic(BankAccountServiceImpl.class)) {
            bankAccountServiceMock.when(
                    () -> BankAccountServiceImpl.getUserData(request, jwtService, userProfileRepository)
            ).thenReturn(sender);

            ApplicationException exception = assertThrows(ApplicationException.class, () ->
                    messageService.sendMessage(request, "janedoe@mpba.com", ""));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals("Message must contain a text.", exception.getMessage());
        }
    }

    @Test
    void sendMessage_ShouldThrowException_WhenReceiverNotFound() {
        try (MockedStatic<BankAccountServiceImpl> bankAccountServiceMock =
                     Mockito.mockStatic(BankAccountServiceImpl.class)) {
            bankAccountServiceMock.when(
                    () -> BankAccountServiceImpl.getUserData(request, jwtService, userProfileRepository)
            ).thenReturn(sender);

            when(userProfileRepository.findByEmail("janedoe@mpba.com")).thenReturn(Optional.empty());

            ApplicationException exception = assertThrows(ApplicationException.class, () ->
                    messageService.sendMessage(request, "janedoe@mpba.com", "Hello, Jane!"));

            assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
            assertEquals("Specified receiver not found.", exception.getMessage());
        }
    }
}