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

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Cacheable(value = "messages")
    public List<Message> getMessages() {
        return messageRepository.findAll();
    }

    @Cacheable(value = "messages", key = "#messageId")
    public Message getMessageById(Long messageId) {
        return messageRepository.findById(messageId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Message with id " + messageId + " not found.")
        );
    }

    @Cacheable(value = "messages", key = "#content")
    public List<Message> getMessagesByContent(String content) {
        if (content.isEmpty()) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Message " + content + " not found.");
        }
        return messageRepository.findByContent(content);
    }

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

    private boolean isValidContent(String content) {
        return content != null && !content.isEmpty() && content.length() <= 100;
    }

    private boolean isValidUser(User user) {
        return user == null || user.getName() == null || user.getSurname() == null
                || user.getName().isEmpty() || user.getSurname().isEmpty()
                || user.getName().length() > 100 || user.getSurname().length() > 100;
    }
}
