package accounts.bank.managing.thesis.bachelor.rastvdmy.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for the configuration of Kafka producers.
 * It provides the necessary beans for creating Kafka producers.
 * The configuration properties are fetched from the application's properties file.
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootStrapService;

    /**
     * This method provides the configuration for Kafka producers.
     * It sets the bootstrap servers, key serializer and value serializer.
     *
     * @return A map containing the configuration properties.
     */
    public Map<String, Object> producerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapService);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

    /**
     * This method provides a ProducerFactory bean.
     * The ProducerFactory is responsible for creating Kafka producers.
     *
     * @return A new instance of DefaultKafkaProducerFactory.
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    /**
     * This method provides a KafkaTemplate bean.
     * The KafkaTemplate wraps a Producer instance and provides convenience methods for sending messages to Kafka topics.
     *
     * @param producerFactory The producer factory.
     * @return A new instance of KafkaTemplate.
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}