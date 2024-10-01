package api.mpba.rastvdmy.service.impl;


import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.entity.Message;
import api.mpba.rastvdmy.entity.User;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.MessageRepository;
import api.mpba.rastvdmy.repository.UserRepository;
import api.mpba.rastvdmy.service.JwtService;
import api.mpba.rastvdmy.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository, UserRepository userRepository, JwtService jwtService) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public List<Message> getMessages() {
        List<Message> messages = messageRepository.findAll();
        return messages.stream().filter(this::decryptMessageData).toList();
    }

    private boolean decryptMessageData(Message message) {
        SecretKey secretKey = EncryptionUtil.getSecretKey();
        try {
            message.setContent(EncryptionUtil.decrypt(message.getContent(), secretKey));
            return true;
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while decrypting account data.");
        }
    }

    public Message sendMessage(HttpServletRequest request, String receiverEmail, String content) throws Exception {
        User sender = BankAccountServiceImpl.getUserData(request, jwtService, userRepository);

        if (sender.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }

        if (content.isEmpty() || content.isBlank()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Message must contain a text.");
        }
        SecretKey secretKey = EncryptionUtil.getSecretKey();

        String encryptedContent = EncryptionUtil.encrypt(content, secretKey);

        User receiver = userRepository.findByEmail(receiverEmail).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Specified receiver not found.")
        );

        Message message = Message.builder()
                .id(UUID.randomUUID())
                .sender(sender)
                .receiver(receiver)
                .timestamp(LocalDateTime.now())
                .content(encryptedContent)
                .build();

        return messageRepository.save(message);
    }
}
