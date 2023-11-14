package com.jdsg.datalogger.processors;

import com.jdsg.datalogger.dto.ChartData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ChartDataTopology {

    @Autowired
    public void process(StreamsBuilder streamsBuilder) {
        KStream<String, ChartData> chartDataKStream = streamsBuilder
                .stream(
                "sensor-data-topic",
                Consumed.with(Serdes.String(), new JsonSerde<>(ChartData.class)))
                .selectKey((key,value) -> value.getTimeStamp());


        chartDataKStream.print(Printed.<String, ChartData>toSysOut().withLabel("chartdata"));


        // Approach 1 to Count using KTable:
        // chartDataCount(chartDataKStream);

        // Approach 1 to Count using KStream:
        chartDataKStream
                .groupByKey(Grouped.with(Serdes.String(), new JsonSerde<>(ChartData.class)))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(1))) // Adjust window size as needed
                .count(Materialized.as("chartdata-count"))
                .toStream()
                .map((key, value) -> KeyValue.pair(key.key(), value))
                .to("chartdata-output", Produced.with(Serdes.String(), Serdes.Long()));

    }

    private void chartDataCount(KStream<String, ChartData> chartDataKStream) {
        KTable<String, Long> chartDataKTable = chartDataKStream
                .map((key, chartData) -> KeyValue.pair(chartData.getTimeStamp(), chartData))
                .groupByKey(Grouped.with(Serdes.String(), new JsonSerde<>(ChartData.class)))
                .count(Named.as("chartdata-count"),
                        Materialized.as("chartdata-count"));
        chartDataKTable
                .toStream()
                .to("chartdata-output", Produced.with(Serdes.String(), Serdes.Long()));

//.print(Printed.<String, Long>toSysOut().withLabel("chartdata-count"));
    }


    // Preparing struc below for average aggregate
//    @Data
//    @NoArgsConstructor
//    class ChartDataAverage {
//        private String timeStamp;
//        private String sensorId;
//        private Long numberOfDataPoints;
//        private Double sumOfDataPoints;
//
//        public ChartDataAverage (String timeStamp, String sensorId, Long numberOfDataPoints, Double sumOfDataPoints) {
//            this.timeStamp = timeStamp;
//            this.sensorId = sensorId;
//            this.numberOfDataPoints = numberOfDataPoints;
//            this.sumOfDataPoints = sumOfDataPoints;
//        }
//
//        public ChartDataAverage update(ChartData chartData) {
//            this.numberOfDataPoints += 1;
//            this.sumOfDataPoints += chartData.getValue();
//            return this;
//        }
//
//        public Double calculateAverage() {
//            return sumOfDataPoints / numberOfDataPoints;
//        }
//    }

}
