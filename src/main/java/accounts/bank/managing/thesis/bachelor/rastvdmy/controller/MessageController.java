package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

import accounts.bank.managing.thesis.bachelor.rastvdmy.controller.mapper.MessageMapper;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.MessageRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.response.MessageResponse;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Message;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is responsible for handling message related requests.
 */
@Slf4j
@RestController
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEFAULT')")
@RequestMapping(path = "/api/v1/messages")
public class MessageController {
    private final static Logger LOG = LoggerFactory.getLogger(MessageController.class);
    private final MessageService messageService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MessageMapper messageMapper;

    /**
     * Constructor for the MessageController.
     *
     * @param messageService The service to handle message operations.
     * @param kafkaTemplate  The template to send messages to Kafka.
     */
    @Autowired
    public MessageController(MessageService messageService,
                             KafkaTemplate<String, String> kafkaTemplate,
                             MessageMapper messageMapper) {
        this.messageService = messageService;
        this.kafkaTemplate = kafkaTemplate;
        this.messageMapper = messageMapper;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<MessageResponse>> getMessages(HttpServletRequest request, @RequestBody String username) {
        logInfo("Getting messages ...");
        List<Message> messages = messageService.getMessages(request, username);
        List<MessageResponse> messagesResponses = messages.stream()
                .map(message -> messageMapper.toResponse(
                        new MessageRequest(message.getReceiver().getName(), message.getContent()))
                ).collect(Collectors.toList());
        return ResponseEntity.ok(messagesResponses);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/content", produces = "application/json")
    public ResponseEntity<MessageRequest> getMessage(HttpServletRequest request,
                                                     @Valid @RequestBody MessageRequest messageRequest) {
        logInfo("Getting message by text ...");
        Message message = messageService.getMessageByContent(request, messageRequest.content());
        MessageResponse messageResponse = messageMapper.toResponse(
                new MessageRequest(message.getReceiver().getName(), message.getContent())
        );
        return ResponseEntity.ok(messageMapper.toRequest(messageResponse));
    }


    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<MessageResponse> sendMessage(
            HttpServletRequest request, @Valid @RequestBody MessageRequest messageRequest) throws Exception {
        logInfo("Sending message ...");

        kafkaTemplate.send("messages", messageRequest.receiverName(), messageRequest.content());

        Message message = messageService.sendMessageById(request, messageRequest.receiverName(),
                messageRequest.content());

        MessageResponse response = messageMapper.toResponse(
                new MessageRequest(message.getReceiver().getName(), message.getContent())
        );
        return ResponseEntity.accepted().body(response);
    }

    private void logInfo(String message, Object... args) {
        LOG.info(message, args);
    }
}
