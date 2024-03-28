package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Message;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

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
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Message with id " + messageId + " not found")
        );
    }

    public List<Message> getMessagesByContent(String content) {
        if (content.isEmpty()) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Message " + content + " not found");
        }
        return messageRepository.findByContent(content);
    }

    public List<Message> getSortedMessagesBySenderId(Long senderId) {
        List<Message> messages = messageRepository.findAll();
        return messages.stream()
                .sorted()
                .filter(message -> message.getSender().getId().equals(senderId))
                .toList();
    }

    public List<Message> getSortedMessagesByReceiverId(Long receiverID) {
        List<Message> messages = messageRepository.findAll();
        return messages.stream()
                .sorted()
                .filter(message -> message.getReceiver().getId().equals(receiverID))
                .toList();
    }

    public Message sendMessage(Long senderId, Long receiverId, String content) {
        // TODO: complete this method
        return null;
    }
}
