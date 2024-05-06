package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Message;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.User;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.MessageRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is responsible for managing messages.
 * It is annotated with @Service to indicate that it's a Spring managed service.
 * It uses MessageRepository and UserRepository to interact with the database.
 */
@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /**
     * Constructs a new MessageService with the given repositories.
     *
     * @param messageRepository The MessageRepository to use.
     * @param userRepository    The UserRepository to use.
     */
    @Autowired
    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all messages.
     *
     * @return A list of all messages.
     */
    @Cacheable(value = "messages")
    public List<Message> getMessages() {
        return messageRepository.findAll();
    }

    /**
     * Retrieves a message by its ID.
     *
     * @param messageId The ID of the message to retrieve.
     * @return The retrieved message.
     */
    @Cacheable(value = "messages", key = "#messageId")
    public Message getMessageById(Long messageId) {
        return messageRepository.findById(messageId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Message with id " + messageId + " not found.")
        );
    }

    /**
     * Retrieves messages by their content.
     *
     * @param content The content of the messages to retrieve.
     * @return The retrieved messages.
     */
    @Cacheable(value = "messages", key = "#content")
    public List<Message> getMessagesByContent(String content) {
        if (content.isEmpty()) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Message " + content + " not found.");
        }
        return messageRepository.findByContent(content);
    }

    /**
     * Retrieves messages sent by a specific user and sorts them.
     *
     * @param senderId The ID of the sender.
     * @param order    The order to sort the messages in.
     * @return The retrieved and sorted messages.
     */
    @Cacheable(value = "messages", key = "#root.methodName + #senderId")
    public List<Message> getSortedMessagesBySenderId(Long senderId, String order) {
        userRepository.findById(senderId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Sender with id " + senderId + " not found.")
        );
        List<Message> messages = messageRepository.findAll();
        Stream<Message> sortedStream = messages.stream()
                .filter(message -> message.getSender().getId().equals(senderId));
        if (order.equalsIgnoreCase("desc")) {
            sortedStream = sortedStream.sorted(Comparator.comparing(Message::getId).reversed());
        } else {
            sortedStream = sortedStream.sorted(Comparator.comparing(Message::getId));
        }

        return sortedStream.collect(Collectors.toList());
    }

    /**
     * Retrieves messages received by a specific user and sorts them.
     *
     * @param receiverId The ID of the receiver.
     * @param order      The order to sort the messages in.
     * @return The retrieved and sorted messages.
     */
    @Cacheable(value = "messages", key = "#root.methodName + #receiverId")
    public List<Message> getSortedMessagesByReceiverId(Long receiverId, String order) {
        userRepository.findById(receiverId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Receiver with id " + receiverId + " not found.")
        );
        List<Message> messages = messageRepository.findAll();
        Stream<Message> sortedStream = messages.stream()
                .filter(message -> message.getReceiver().getId().equals(receiverId));
        if (order.equalsIgnoreCase("desc")) {
            sortedStream = sortedStream.sorted(Comparator.comparing(Message::getId).reversed());
        } else {
            sortedStream = sortedStream.sorted(Comparator.comparing(Message::getId));
        }
        return sortedStream.collect(Collectors.toList());
    }

    /**
     * Sends a message from one user to another.
     *
     * @param senderId   The ID of the sender.
     * @param receiverId The ID of the receiver.
     * @param content    The content of the message.
     * @return The sent message.
     */
    @CacheEvict(value = "messages", allEntries = true)
    public Message sendMessage(Long senderId, Long receiverId, String content) {
        Message message = new Message();
        User sender = userRepository.findById(senderId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Sender with id " + senderId + " not found.")
        );
        User receiver = userRepository.findById(receiverId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Receiver with id " + receiverId + " not found.")
        );
        if (content.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Message must contain a text.");
        }
        if (!isValidContent(content)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Message must be between 1 and 100 characters.");
        }
        message.setContent(HtmlUtils.htmlEscape(content));
        if (isValidUser(sender) || isValidUser(receiver)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "User must have a name and surname.");
        }
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setTimestamp(LocalDateTime.now());
        return messageRepository.save(message);
    }

    /**
     * Checks if the content of a message is valid.
     *
     * @param content The content of the message.
     * @return True if the content is valid, false otherwise.
     */
    private boolean isValidContent(String content) {
        return content != null && !content.isEmpty() && content.length() <= 1000;
    }

    /**
     * Checks if a user is valid.
     *
     * @param user The user to check.
     * @return True if the user is valid, false otherwise.
     */
    private boolean isValidUser(User user) {
        return user == null || user.getName() == null || user.getSurname() == null
                || user.getName().isEmpty() || user.getSurname().isEmpty()
                || user.getName().length() > 100 || user.getSurname().length() > 100;
    }
}
