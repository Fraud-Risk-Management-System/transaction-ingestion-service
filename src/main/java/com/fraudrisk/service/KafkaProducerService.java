package com.fraudrisk.service;

import com.fraudrisk.model.Transaction;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

//import javax.annotation.PostConstruct;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class KafkaProducerService implements InitializingBean {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${kafka.topics.transactions}")
    private String transactionTopic;

    private KafkaProducer<String, Transaction> producer;

//    @PostConstruct
//    public void init() {
//
//    }

    public CompletableFuture<Void> sendTransaction(Transaction transaction) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        String key = transaction.getTransactionId().toString();

        try {
            ProducerRecord<String, Transaction> record = new ProducerRecord<>(
                    transactionTopic, key, transaction);

            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("Failed to send transaction {}: {}",
                            key, exception.getMessage(), exception);
                    future.completeExceptionally(exception);
                } else {
                    log.debug("Transaction sent successfully: {}, partition: {}, offset: {}",
                            key, metadata.partition(), metadata.offset());
                    future.complete(null);
                }
            });
        } catch (Exception e) {
            log.error("Error preparing transaction for sending: {}", e.getMessage(), e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");
        props.put(ProducerConfig.RETRIES_CONFIG, "5");
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");

        producer = new KafkaProducer<>(props);
        log.info("Kafka producer initialized with bootstrap servers: {}", bootstrapServers);
    }
}