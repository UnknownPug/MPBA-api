package accounts.bank.managing.thesis.bachelor.rastvdmy.config.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaListeners {

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
