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
import java.util.List;

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

    public List<Message> getMessages(HttpServletRequest request, String username) {
        User sender = getSender(request);
        return messageRepository.findAllBySenderIdAndReceiverName(sender.getId(), username);
    }

    public Message getMessageByContent(HttpServletRequest request, String content) {
        User sender = getSender(request);
        return messageRepository.findBySenderIdAndContent(sender.getId(), content);
    }

    public Message sendMessageById(HttpServletRequest request, String receiverName, String content) throws Exception {
        User sender = getSender(request);

        if (content.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Message must contain a text.");
        }
        SecretKey secretKey = EncryptionUtil.getSecretKey();

        String encryptedContent = EncryptionUtil.encrypt(content, secretKey, EncryptionUtil.generateIv());

        Message message = Message.builder()
                        .sender(sender)
                        .receiver(userRepository.findByName(receiverName))
                        .content(encryptedContent)
                        .build();

        return messageRepository.save(message);
    }

    private User getSender(HttpServletRequest request) {
        final String token = jwtService.extractToken(request);
        final String senderId = jwtService.extractSubject(token); //FIXME: Don't forget that this is users' email!

        User sender = userRepository.findById(senderId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Specified sender not found.")
        );

        if (sender.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "User is blocked. Opperation is forbidden.");
        }
        return sender;
    }
}
