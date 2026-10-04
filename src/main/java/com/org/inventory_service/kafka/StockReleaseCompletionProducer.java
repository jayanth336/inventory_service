package com.org.inventory_service.kafka;

import com.org.inventory_service.event.StockReleaseCompletedEvent;
import com.org.inventory_service.event.StockReservationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class StockReleaseCompletionProducer {
    private final String TOPIC = "stock-release-completed-events";
    private final KafkaTemplate<String, StockReleaseCompletedEvent> kafkaTemplate;

    public StockReleaseCompletionProducer(KafkaTemplate<String, StockReleaseCompletedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishStockReleaseCompletedEvent(StockReleaseCompletedEvent completedEvent) {
        // KEY IS ORDER ID - IT TELLS THAT MESSAGES BELONGING TO SAME ORDER ID GO TO THE SAME KAFKA PARTITION
        kafkaTemplate.send(
                TOPIC, // TOPIC
                completedEvent.orderId().toString(), // KEY
                completedEvent // VALUE
        );
    }
}
