package api.mpba.rastvdmy.config.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for listening to Kafka messages.
 * It uses the @KafkaListener annotation to subscribe to "messages" topics.
 * The listener method logs the received message and any potential processing errors.
 */
@Slf4j
@Component
public class KafkaListeners {

    /**
     * This method is a Kafka listener that gets triggered when a message arrives in the "messages" topic.
     * It logs the received message and any potential processing errors.
     *
     * @param data The message data.
     * @param key  The key of the message.
     */
    @KafkaListener(
            topics = "messages",
            groupId = "messagesId"
    )
    public void listener(String data, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        try {
            log.info("Listener received: {}", data);
        } catch (Exception e) {
            log.error("Error processing message with key {}: {}", key, e.getMessage());
        }
    }
}
