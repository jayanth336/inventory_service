package com.org.inventory_service.kafka;

import com.org.inventory_service.event.StockReservationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class StockReservationEventProducer {
    private final String TOPIC = "stock-reservation-events";
    private final KafkaTemplate<String, StockReservationEvent> kafkaTemplate;

    public StockReservationEventProducer(KafkaTemplate<String, StockReservationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishStockReservationEvent(StockReservationEvent stockReservationEvent) {
        // KEY IS ORDER ID - IT TELLS THAT MESSAGES BELONGING TO SAME ORDER ID GO TO THE SAME KAFKA PARTITION
        kafkaTemplate.send(
                TOPIC, // TOPIC
                stockReservationEvent.orderId().toString(), // KEY
                stockReservationEvent // VALUE
        );
    }
}
