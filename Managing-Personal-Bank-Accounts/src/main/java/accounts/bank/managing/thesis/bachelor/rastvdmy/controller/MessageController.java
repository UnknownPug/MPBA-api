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
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping(path = "/chat")
public class MessageController {

    private final static Logger LOG = LoggerFactory.getLogger(MessageController.class);
    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping(path = "/")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Message>> getMessages() {
        LOG.debug("Getting messages ...");
        return ResponseEntity.ok(messageService.getMessages());
    }

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Message> getMessageById(@PathVariable(value = "id") Long messageId) {
        LOG.debug("Getting message id: {} ...", messageId);
        return ResponseEntity.ok(messageService.getMessageById(messageId));
    }

    @GetMapping(path = "/{id}/")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Message>> getSortedMessages(
            @PathVariable(value = "id") Long userId,
            @RequestParam(value = "sort") String sort) {
        if (sort.equals("sender-id")) {
            LOG.debug("Getting sorted messages by senderId: {} ...", userId);
            return ResponseEntity.ok(messageService.getSortedMessagesBySenderId(userId));
        } else if (sort.equals("receiver-id")) {
            LOG.debug("Getting sorted messages by receiverId: {} ...", userId);
            return ResponseEntity.ok(messageService.getSortedMessagesByReceiverId(userId));
        } else {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST, "Invalid type of sorting. Use 'sender-id' or 'receiver-id'");
        }
    }

    @PostMapping(path = "/")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Message> sendMessage(@RequestBody MessageRequest messageRequest) {
        LOG.debug("Sending message ...");
        return ResponseEntity.ok(messageService.sendMessage(
                messageRequest.senderId(),
                messageRequest.receiverId(),
                messageRequest.content())
        );
    }
}
