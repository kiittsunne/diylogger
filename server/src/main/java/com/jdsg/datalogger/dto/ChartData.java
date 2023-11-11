package com.jdsg.datalogger.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@JsonDeserialize
@JsonSerialize
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartData {
    private int value;
    private String sensorId;
    private String timeStamp;
}


