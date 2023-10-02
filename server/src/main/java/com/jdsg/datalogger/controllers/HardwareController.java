package com.jdsg.datalogger.controllers;

import com.jdsg.datalogger.dto.ChartData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HardwareController {
    @Autowired
    SimpMessagingTemplate messagingTemplate;

    // Bridges REST hardware-server layer to WS server-frontend client layer
    @PostMapping("/in")
    public void receivePhotoresistorValue(@RequestBody ChartData data) {

        // TODO: Sensor connection sanity check. Remove in final ver.
        System.out.println("Received photoresistor value: " + data.getValue());
        messagingTemplate.convertAndSend("/topic/data", data);
    }
}
