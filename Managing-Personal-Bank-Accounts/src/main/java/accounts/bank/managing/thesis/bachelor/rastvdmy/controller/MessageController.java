package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.MessageRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Message;
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
        LOG.info("Getting messages");
        return ResponseEntity.ok(messageService.getMessages());
    }

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Message> getMessageById(@PathVariable (value = "id") Long messageId) {
        LOG.info("Getting message by id");
        return ResponseEntity.ok(messageService.getMessageById(messageId));
    }
    @GetMapping(path = "/{id}/")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Message>> getSortedMessages(
            @PathVariable(value = "id") Long userId,
            @RequestParam(value = "sort") String sort) {
        LOG.info("Getting sorted messages");
        if (sort.equals("sender_id")) {
            return ResponseEntity.ok(messageService.getSortedMessagesBySenderId(userId));
        } else if (sort.equals("receiver_id")) {
            return ResponseEntity.ok(messageService.getSortedMessagesByReceiverId(userId));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(path = "/")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Message> addMessage(@RequestBody MessageRequest messageRequest) {
        LOG.info("Adding message");
        return ResponseEntity.ok(messageService.addMessage(messageRequest.content()));
    }

    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void updateMessage(
            @PathVariable(value = "id") Long messageId,
            @RequestBody MessageRequest messageRequest) {
        LOG.info("Updating message");
        messageService.updateMessage(messageId, messageRequest.content());
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(@PathVariable(value = "id") Long messageId) {
        LOG.info("Deleting message");
        messageService.deleteMessage(messageId);
    }
}
