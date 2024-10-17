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
 * Controller for handling message-related requests.
 * <p>
 * This class provides endpoints for sending and retrieving messages. It utilizes the
 * {@link MessageService} for business logic and {@link MessageMapper} for mapping
 * between request and response objects. Messages are also sent to a Kafka topic for
 * further processing.
 * </p>
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
     * @param messageMapper  The mapper for converting between request and response objects.
     */
    @Autowired
    public MessageController(MessageService messageService,
                             KafkaTemplate<String, String> kafkaTemplate,
                             MessageMapper messageMapper) {
        this.messageService = messageService;
        this.kafkaTemplate = kafkaTemplate;
        this.messageMapper = messageMapper;
    }

    /**
     * Retrieves a list of messages.
     *
     * @return A {@link ResponseEntity} containing a list of {@link MessageResponse}.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<MessageResponse>> getMessages() {
        logInfo("Getting messages ...");
        List<Message> messages = messageService.getMessages();
        List<MessageResponse> messagesResponses = messages.stream()
                .map(message -> messageMapper.toResponse(
                        new MessageRequest(
                                message.getReceiver().getEmail(),
                                message.getContent(),
                                message.getSender().getEmail(),
                                message.getTimestamp()
                        )
                )).collect(Collectors.toList());
        return ResponseEntity.ok(messagesResponses);
    }

    /**
     * Sends a message to a recipient and publishes it to Kafka.
     *
     * @param request        The HTTP servlet request containing user information.
     * @param messageRequest The request body containing message details.
     * @return A {@link ResponseEntity} containing the sent {@link MessageResponse}.
     * @throws Exception if there is an error sending the message.
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<MessageResponse> sendMessage(
            HttpServletRequest request, @Valid @RequestBody MessageRequest messageRequest) throws Exception {
        logInfo("Sending message ...");

        Message message = messageService.sendMessage(request, messageRequest.receiverEmail(),
                messageRequest.content());

        kafkaTemplate.send("messages", messageRequest.receiverEmail(), messageRequest.content());

        MessageResponse response = messageMapper.toResponse(
                new MessageRequest(
                        message.getReceiver().getEmail(),
                        message.getContent(),
                        message.getSender().getEmail(),
                        message.getTimestamp()
                ));
        return ResponseEntity.accepted().body(response);
    }

    /**
     * Logs informational messages to the console.
     *
     * @param message The message to log.
     * @param args    Optional arguments to format the message.
     */
    private void logInfo(String message, Object... args) {
        LOG.info(message, args);
    }
}
