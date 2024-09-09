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
    List<Message> getMessages();

    Message sendMessage(HttpServletRequest request, String receiverName, String content) throws Exception;
}
