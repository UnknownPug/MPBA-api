package api.mpba.rastvdmy.service;

import api.mpba.rastvdmy.entity.Message;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * Service interface for managing messages within the application.
 */
public interface MessageService {

    /**
     * Retrieves all messages.
     *
     * @return a list of messages
     */
    List<Message> getMessages();

    /**
     * Sends a message to a specified receiver.
     *
     * @param request      the HTTP request containing context information
     * @param receiverName the name of the message receiver
     * @param content      the content of the message to be sent
     * @return the sent Message object
     * @throws Exception if there is an error while sending the message
     */
    Message sendMessage(HttpServletRequest request, String receiverName, String content) throws Exception;
}
