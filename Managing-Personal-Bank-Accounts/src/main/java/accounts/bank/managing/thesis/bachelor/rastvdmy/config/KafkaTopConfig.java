package accounts.bank.managing.thesis.bachelor.rastvdmy.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopConfig {

    @Bean
    public NewTopic createTopic() {
        return TopicBuilder.name("messages").build();
    }
}


