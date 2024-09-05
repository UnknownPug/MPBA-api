package api.mpba.rastvdmy.service;

import api.mpba.rastvdmy.entity.Message;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * Service for managing messages
 */
public interface MessageService {

    /**
     * Get all messages
     * @return list of messages
     */
    List<Message> getMessages(HttpServletRequest request, String username);

    /**
     * Get a message by content
     * @param content content of the message
     * @return message
     */
    Message getMessageByContent(HttpServletRequest request, String content);

    Message sendMessageById(HttpServletRequest request, String receiverName, String content) throws Exception;
}
