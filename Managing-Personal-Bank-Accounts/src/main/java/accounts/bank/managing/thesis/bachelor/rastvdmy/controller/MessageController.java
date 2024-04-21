package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.MessageRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Message;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * This class is responsible for handling message related requests.
 * It provides endpoints for getting all messages, getting a message by id, getting messages by content,
 * getting sorted messages, and sending a message.
 */
@Slf4j
@RestController
@RequestMapping(path = "/chat")
public class MessageController {

    private final static Logger LOG = LoggerFactory.getLogger(MessageController.class);
    private final MessageService messageService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Constructor for the MessageController.
     *
     * @param messageService The service to handle message operations.
     * @param kafkaTemplate  The Kafka template for sending messages.
     */
    @Autowired
    public MessageController(MessageService messageService, KafkaTemplate<String, String> kafkaTemplate) {
        this.messageService = messageService;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * This method is used to get all messages.
     *
     * @return A list of all messages.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public ResponseEntity<List<Message>> getMessages() {
        LOG.info("Getting messages ...");
        return ResponseEntity.ok(messageService.getMessages());
    }

    /**
     * This method is used to get a message by id.
     *
     * @param messageId The id of the message.
     * @return The message with the given id.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public ResponseEntity<Message> getMessageById(@PathVariable(value = "id") Long messageId) {
        LOG.info("Getting message id: {} ...", messageId);
        return ResponseEntity.ok(messageService.getMessageById(messageId));
    }

    /**
     * This method is used to get messages by content.
     *
     * @param content The content of the messages.
     * @return A list of messages with the given content.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/search/{content}")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public ResponseEntity<List<Message>> getMessagesByContent(@PathVariable(value = "content") String content) {
        LOG.info("Getting messages by content: {} ...", content);
        return ResponseEntity.ok(messageService.getMessagesByContent(content));
    }

    /**
     * This method is used to get sorted messages.
     *
     * @param userId The id of the user.
     * @param sort   The sort option.
     * @param order  The order option.
     * @return A list of sorted messages.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{id}/")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public ResponseEntity<List<Message>> getSortedMessages(
            @PathVariable(value = "id") Long userId,
            @RequestParam(value = "sort") String sort,
            @RequestParam(value = "order", defaultValue = "asc") String order) {
        return switch (sort.toLowerCase() + "-" + order.toLowerCase()) {
            case "sender-asc" -> {
                LOG.info("Getting sorted messages by sender id in ascending order: {} ...", userId);
                yield ResponseEntity.ok(messageService.getSortedMessagesBySenderId(userId, "asc"));
            }
            case "sender-desc" -> {
                LOG.info("Getting sorted messages by sender id in descending order: {} ...", userId);
                yield ResponseEntity.ok(messageService.getSortedMessagesBySenderId(userId, "desc"));
            }
            case "receiver-asc" -> {
                LOG.info("Getting sorted messages by receiver id in ascending order: {} ...", userId);
                yield ResponseEntity.ok(messageService.getSortedMessagesByReceiverId(userId, "asc"));
            }
            case "receiver-desc" -> {
                LOG.info("Getting sorted messages by receiver id in descending order: {} ...", userId);
                yield ResponseEntity.ok(messageService.getSortedMessagesByReceiverId(userId, "desc"));
            }
            default -> throw new ApplicationException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid type of sorting or order. Use 'sender' or 'receiver' for sorting" +
                            " and 'asc' or 'desc' for order.");
        };
    }

    /**
     * This method is used to send a message.
     *
     * @param messageRequest The request containing the sender id, receiver id, and content of the message.
     * @return The sent message.
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public ResponseEntity<Message> sendMessage(@RequestBody MessageRequest messageRequest) {
        LOG.info("Sending message ...");
        kafkaTemplate.send("messages", messageRequest.receiverId().toString(), messageRequest.content());
        LOG.info("Message has been successfully sent.");
        return ResponseEntity.ok(messageService.sendMessage(
                messageRequest.senderId(),
                messageRequest.receiverId(),
                messageRequest.content())
        );
    }
}
