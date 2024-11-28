package api.mpba.rastvdmy.service.impl;


import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.entity.Message;
import api.mpba.rastvdmy.entity.UserProfile;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.MessageRepository;
import api.mpba.rastvdmy.repository.UserProfileRepository;
import api.mpba.rastvdmy.service.MessageService;
import api.mpba.rastvdmy.service.TokenVerifierService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the MessageService interface for managing messages between users.
 * This service provides methods for sending and retrieving messages,
 * including message encryption and decryption functionalities.
 */
@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final TokenVerifierService tokenVerifierService;
    private final UserProfileRepository userProfileRepository;

    /**
     * Constructor for MessageServiceImpl.
     *
     * @param messageRepository     the message repository to be used
     * @param userProfileRepository the user profile repository to be used
     * @param tokenVerifierService the service for extracting user token and getting user data from the request
     *
     */
    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository, UserProfileRepository userProfileRepository,
                              TokenVerifierService tokenVerifierService) {
        this.messageRepository = messageRepository;
        this.userProfileRepository = userProfileRepository;
        this.tokenVerifierService = tokenVerifierService;
    }

    /**
     * Retrieves all messages, decrypting their content before returning.
     *
     * @return a list of decrypted messages
     */
    public List<Message> getMessages() {
        List<Message> messages = messageRepository.findAll();
        return messages.stream().filter(this::decryptMessageData).toList();
    }

    /**
     * Decrypts the content of a given message.
     *
     * @param message the message to decrypt
     * @return true if decryption is successful, false otherwise
     * @throws ApplicationException if an error occurs during decryption
     */
    private boolean decryptMessageData(Message message) {
        SecretKey secretKey = EncryptionUtil.getSecretKey();
        try {
            message.setContent(EncryptionUtil.decrypt(message.getContent(), secretKey));
            return true;
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while decrypting account data.");
        }
    }

    /**
     * Sends a message from the sender to the specified receiver.
     *
     * @param request       the HTTP request containing user data
     * @param receiverEmail the email of the receiver
     * @param content       the content of the message
     * @return the saved Message object
     * @throws Exception if an error occurs during message sending
     */
    public Message sendMessage(HttpServletRequest request, String receiverEmail, String content) throws Exception {
        UserProfile sender = tokenVerifierService.getUserData(request);

        if (sender.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }

        if (content.trim().isBlank()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Message must contain a text.");
        }
        SecretKey secretKey = EncryptionUtil.getSecretKey();

        // Sanitize input content to prevent XSS
        String sanitizedContent = StringEscapeUtils.escapeHtml4(content.trim());

        // Encrypt the sanitized content
        String encryptedContent = EncryptionUtil.encrypt(sanitizedContent, secretKey);

        UserProfile receiver = userProfileRepository.findByEmail(receiverEmail.trim()).orElseThrow(
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
