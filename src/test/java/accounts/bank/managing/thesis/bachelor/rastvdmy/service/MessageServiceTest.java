package accounts.bank.managing.thesis.bachelor.rastvdmy.service;


import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Message;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.User;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.MessageRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * This class is used to test the functionality of the MessageService class.
 * It uses the Mockito framework for mocking dependencies and JUnit for running the tests.
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MessageService messageService;

    /**
     * This method is used to set up the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        messageRepository = mock(MessageRepository.class);
        userRepository = mock(UserRepository.class);
        messageService = new MessageService(messageRepository, userRepository);
    }

    /**
     * This method tests the functionality of the getMessageById method in the MessageService class.
     * It verifies that the method returns the list of messages.
     */
    @Test
    void testGetMessages() {
        // Mocking data
        List<Message> messages = new ArrayList<>();
        messages.add(new Message());
        when(messageRepository.findAll()).thenReturn(messages);

        // Testing the method
        List<Message> result = messageService.getMessages();

        // Assertions
        assert result.size() == 1; // Ensure one message is returned
    }

    /**
     * This method tests the functionality of the getMessageById method in the MessageService class.
     * It verifies that the method returns the correct message when a valid ID is provided.
     */
    @Test
    void testGetMessageById_ExistingId() {
        // Mocking data
        Long messageId = 1L;
        Message message = new Message();
        message.setId(messageId);
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

        // Testing the method
        Message result = messageService.getMessageById(messageId);

        // Assertions
        assert result.getId().equals(messageId); // Ensure the correct message is returned
    }

    /**
     * This method tests the functionality of the getMessageById method in the MessageService class.
     * It verifies that the method throws an exception when an invalid ID is provided.
     */
    @Test
    void testGetMessageById_NonExistingId() {
        // Mocking data
        Long messageId = 1L;
        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        // Testing the method and expecting an exception
        try {
            messageService.getMessageById(messageId);
        } catch (ApplicationException e) {
            // Assertions
            assert e.getHttpStatus().equals(HttpStatus.NOT_FOUND); // Ensure correct exception is thrown
        }
    }

    /**
     * This method tests the functionality of the getMessagesByContent method in the MessageService class.
     * It verifies that the method returns all messages with the provided content.
     */
    @Test
    void testGetMessagesByContent_NonEmptyContent() {
        // Mocking data
        String content = "Test Content";
        List<Message> messages = new ArrayList<>();
        messages.add(new Message());
        when(messageRepository.findByContent(content)).thenReturn(messages);

        // Testing the method
        List<Message> result = messageService.getMessagesByContent(content);

        // Assertions
        assert result.size() == 1; // Ensure one message is returned
    }

    /**
     * This method tests the functionality of the getMessagesByContent method in the MessageService class.
     * It verifies that the method throws an exception when an empty content is provided.
     */
    @Test
    void testGetMessagesByContent_EmptyContent() {
        // Mocking data
        String content = "";

        // Testing the method and expecting an exception
        try {
            messageService.getMessagesByContent(content);
        } catch (ApplicationException e) {
            // Assertions
            assert e.getHttpStatus().equals(HttpStatus.NOT_FOUND); // Ensure correct exception is thrown
        }
    }

    /**
     * This method tests the functionality of the getSortedMessagesBySenderId method in the MessageService class.
     * It verifies that the method throws an exception when the sender is not found.
     */
    @Test
    public void testGetSortedMessagesBySenderId_SenderNotFound() {
        Long senderId = 1L;
        String order = "asc";

        when(userRepository.findById(senderId)).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> messageService.getSortedMessagesBySenderId(senderId, order));
    }

    /**
     * This method tests the functionality of the getSortedMessagesByReceiverId method in the MessageService class.
     * It verifies that the method throws an exception when the receiver is not found.
     */
    @Test
    public void testGetSortedMessagesByReceiverId_ReceiverNotFound() {
        Long receiverId = 1L;
        String order = "asc";

        when(userRepository.findById(receiverId)).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> messageService.getSortedMessagesByReceiverId(receiverId, order));
    }

    /**
     * This method tests the functionality of the getSortedMessagesBySenderId method in the MessageService class.
     * It verifies that the method throws an exception when the sender does not exist.
     */
    @Test
    public void testGetSortedMessagesBySenderId_SenderDoesNotExist() {
        Long senderId = 1L;
        String order = "asc";

        when(userRepository.findById(senderId)).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> messageService.getSortedMessagesBySenderId(senderId, order));
    }

    /**
     * This method tests the functionality of the getSortedMessagesByReceiverId method in the MessageService class.
     * It verifies that the method throws an exception when the receiver does not exist.
     */
    @Test
    public void testGetSortedMessagesByReceiverId_ReceiverDoesNotExist() {
        Long receiverId = 1L;
        String order = "asc";

        when(userRepository.findById(receiverId)).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> messageService.getSortedMessagesByReceiverId(receiverId, order));
    }

    /**
     * This method tests the functionality of the sendMessage method in the MessageService class.
     * It verifies that the method sends a message when valid sender, receiver, and content are provided.
     */
    @Test
    void testSendMessage_ValidInput() {
        // Mocking data
        Long senderId = 1L;
        Long receiverId = 2L;
        String content = "Test message";
        User sender = new User();
        sender.setId(senderId);
        sender.setName("Sender Name");
        sender.setSurname("Sender Surname");
        User receiver = new User();
        receiver.setId(receiverId);
        receiver.setName("Receiver Name");
        receiver.setSurname("Receiver Surname");

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(messageRepository.save(any())).thenReturn(new Message());

        // Testing the method
        messageService.sendMessage(senderId, receiverId, content);

        // Verify that messageRepository.save was called once
        verify(messageRepository).save(any());
    }

    /**
     * This method tests the functionality of the sendMessage method in the MessageService class.
     * It verifies that the method throws an exception when an invalid sender or receiver is provided.
     */
    @Test
    void testSendMessage_InvalidSenderOrReceiver() {
        // Mocking data
        Long senderId = 1L;
        Long receiverId = 2L;
        String content = "Test message";
        User sender = new User();
        sender.setId(senderId); // No name or surname
        User receiver = new User();
        receiver.setId(receiverId); // No name or surname

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));

        // Testing the method and expecting an exception
        try {
            messageService.sendMessage(senderId, receiverId, content);
        } catch (ApplicationException e) {
            // Assertions
            assert e.getHttpStatus().equals(HttpStatus.BAD_REQUEST); // Ensure correct exception is thrown
        }
    }
}

