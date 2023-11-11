package com.jdsg.datalogger.services;

import com.jdsg.datalogger.dto.ChartData;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Service
public class KafkaConsumer {

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "sensor-data-topic", groupId = "my-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, ChartData> record) {
        // Process the received sensor data
        ChartData data = record.value();
        // Send the processed data to the WebSocket topic for real-time updates
        messagingTemplate.convertAndSend("/topic/data", data);
    }

}
