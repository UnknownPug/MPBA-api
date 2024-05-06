package accounts.bank.managing.thesis.bachelor.rastvdmy.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for the configuration of Kafka consumers.
 * It provides the necessary beans for creating Kafka consumers.
 * The configuration properties are fetched from the application's properties file.
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootStrapService;

    /**
     * This method provides the configuration for Kafka consumers.
     * It sets the bootstrap servers, key deserializer and value deserializer.
     *
     * @return A map containing the configuration properties.
     */
    public Map<String, Object> consumerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapService);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }

    /**
     * This method provides a ConsumerFactory bean.
     * The ConsumerFactory is responsible for creating Kafka consumers.
     *
     * @return A new instance of DefaultKafkaConsumerFactory.
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfig());
    }

    /**
     * This method provides a KafkaListenerContainerFactory bean.
     * The KafkaListenerContainerFactory is responsible for creating Kafka listener containers.
     *
     * @param consumerFactory The consumer factory.
     * @return A new instance of ConcurrentKafkaListenerContainerFactory.
     */
    @Bean
    public KafkaListenerContainerFactory<
            ConcurrentMessageListenerContainer<String, String>> factory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}