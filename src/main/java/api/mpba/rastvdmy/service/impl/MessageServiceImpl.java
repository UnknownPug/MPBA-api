package api.mpba.rastvdmy.service.impl;


import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.entity.Message;
import api.mpba.rastvdmy.entity.UserProfile;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.MessageRepository;
import api.mpba.rastvdmy.repository.UserProfileRepository;
import api.mpba.rastvdmy.service.JwtService;
import api.mpba.rastvdmy.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.text.StringEscapeUtils;
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
    private final UserProfileRepository userProfileRepository;
    private final JwtService jwtService;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository, UserProfileRepository userProfileRepository, JwtService jwtService) {
        this.messageRepository = messageRepository;
        this.userProfileRepository = userProfileRepository;
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
        UserProfile sender = BankAccountServiceImpl.getUserData(request, jwtService, userProfileRepository);

        if (sender.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }

        if (content.isEmpty() || content.isBlank()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Message must contain a text.");
        }
        SecretKey secretKey = EncryptionUtil.getSecretKey();

        String sanitizedContent = StringEscapeUtils.escapeHtml4(content);
        String encryptedContent = EncryptionUtil.encrypt(sanitizedContent, secretKey);

        UserProfile receiver = userProfileRepository.findByEmail(receiverEmail).orElseThrow(
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
