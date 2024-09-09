package api.mpba.rastvdmy.controller;

import api.mpba.rastvdmy.controller.mapper.MessageMapper;
import api.mpba.rastvdmy.dto.request.MessageRequest;
import api.mpba.rastvdmy.dto.response.MessageResponse;
import api.mpba.rastvdmy.entity.Message;
import api.mpba.rastvdmy.service.MessageService;
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

import jakarta.validation.Valid;

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
    public ResponseEntity<List<MessageResponse>> getMessages() {
        logInfo("Getting messages ...");
        List<Message> messages = messageService.getMessages();
        List<MessageResponse> messagesResponses = messages.stream()
                .map(message -> messageMapper.toResponse(
                        new MessageRequest(message.getReceiver().getEmail(), message.getContent(), message.getSender().getEmail())
                )).collect(Collectors.toList());
        return ResponseEntity.ok(messagesResponses);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<MessageResponse> sendMessage(
            HttpServletRequest request, @Valid @RequestBody MessageRequest messageRequest) throws Exception {
        logInfo("Sending message ...");

        kafkaTemplate.send("messages", messageRequest.receiverEmail(), messageRequest.content());

        Message message = messageService.sendMessage(request, messageRequest.receiverEmail(),
                messageRequest.content());

        MessageResponse response = messageMapper.toResponse(
                new MessageRequest(message.getReceiver().getEmail(), message.getContent(), message.getSender().getEmail())
        );
        return ResponseEntity.accepted().body(response);
    }

    private void logInfo(String message, Object... args) {
        LOG.info(message, args);
    }
}
