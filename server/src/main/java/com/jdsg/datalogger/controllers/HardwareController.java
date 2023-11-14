package com.jdsg.datalogger.controllers;

import com.jdsg.datalogger.dto.ChartData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HardwareController {
    @Autowired
    private KafkaTemplate<String, ChartData> kafkaTemplate;

    private static final String TOPIC = "sensor-data-topic";
    @PostMapping("/in")
    public void receivePhotoresistorValue(@RequestBody ChartData data) {
        kafkaTemplate.send(TOPIC, data);

        // TODO: replace with logging
//        System.out.println(data.getSensorId() + ": " + data.getValue());
    }
}