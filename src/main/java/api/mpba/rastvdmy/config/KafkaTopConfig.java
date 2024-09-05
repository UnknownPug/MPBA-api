package api.mpba.rastvdmy.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * This class is responsible for the configuration of Kafka topics.
 * It provides a bean for creating a new Kafka topic named "messages".
 */
@Configuration
public class KafkaTopConfig {

    /**
     * This method provides a NewTopic bean.
     * The NewTopic represents a new topic to be created in Kafka.
     * The name of the topic is "messages".
     *
     * @return A NewTopic instance representing a topic named "messages".
     */
    @Bean
    public NewTopic createTopic() {
        return TopicBuilder.name("messages").build();
    }
}


