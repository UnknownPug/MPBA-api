package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Message;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public List<Message> getMessages() {
        return messageRepository.findAll();
    }

    public Message getMessageById(Long messageId) {
        return messageRepository.findById(messageId).orElseThrow(
                () -> new IllegalStateException("Message with id " + messageId + " does not exist")
        );
    }

    public List<Message> getSortedMessagesBySenderId(Long senderId) {
        return messageRepository.getSortedMessagesBySenderId(senderId);
    }

    public List<Message> getSortedMessagesByReceiverId(Long receiverID) {
        return messageRepository.getSortedMessagesByReceiverId(receiverID);
    }

    public Message addMessage(String content) {
        Message message = new Message();
        if (content.isEmpty()) {
            throw new IllegalStateException("Message content cannot be empty");
        }
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        return messageRepository.save(message);
    }

    public void updateMessage(Long messageId, String content) {
        Message message = messageRepository.findById(messageId).orElseThrow(
                () -> new IllegalStateException("Message with id " + messageId + " does not exist")
        );
        if (content.isEmpty() || Objects.equals(message.getContent(), content)) {
            throw new IllegalStateException("Message content cannot be empty");
        }
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        messageRepository.save(message);
    }

    public void deleteMessage(Long messageId) {
        messageRepository.findById(messageId).orElseThrow(
                () -> new IllegalStateException("Message with id " + messageId + " does not exist")
        );
        messageRepository.deleteById(messageId);
    }
}
