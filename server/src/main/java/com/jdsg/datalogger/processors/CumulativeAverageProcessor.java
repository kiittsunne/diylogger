package com.jdsg.datalogger.processors;
import com.jdsg.datalogger.dto.ChartData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Duration;

@EnableKafkaStreams
public class CumulativeAverageProcessor {
    @Bean
    public KStream<String, ChartData> kStream(StreamsBuilder builder) {
        JsonSerde<ChartData> valueSerde = new JsonSerde<>(ChartData.class);

        // Approach 1 - following this tutorial: https://mail-narayank.medium.com/stream-aggregation-in-kafka-e57aff20d8ad
        builder
                .table("sensor-data-topic", Consumed.with(Serdes.String(), valueSerde))
                .groupBy((key,value) -> KeyValue.pair(value.getTimeStamp(), value), Grouped.with(Serdes.String(), valueSerde))
                .aggregate(
                        () -> new CumulativeAverageData(0,0, 0), // this line gets the following error: no instance(s) of type variable(s) VR exist so that Materialized<String, CumulativeAverageProcessor.CumulativeAverageData, KeyValueStore<Bytes, byte[]>> conforms to Aggregator<? super String, ? super ChartData, VR>
                        (key,value,aggV) -> new CumulativeAverageData(
                                aggV.getSum() + value.getValue(),
                                aggV.getCount() + 1,
                                (aggV.getSum() + value.getValue()) / (aggV.getCount() + 1)
                        ),
                        Materialized
                                .<String, CumulativeAverageData,KeyValueStore<Bytes, byte[]>>as("average-store")
                                .withValueSerde(CumulativeAverageData()) // this line gets error: Method call expected
                )
        ;

        // Approach 2 - asked chatgpt how to implement this, this was the suggestion but it's referencing methods that don't even exist. NOTE: this approach assumed that the CumulativeAverageData storage class had only sum & count, no "avg".
        TimeWindowedKStream<String, ChartData> windowedStream =
        builder.stream(
                "sensor-data-topic",
                        Consumed.with(Serdes.String(), valueSerde)
                )
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofSeconds(1)))
                .aggregate(
                        () -> new CumulativeAverageData(0, 0),
                        (key, value, aggregate) -> new CumulativeAverageData(aggregate.getSum() + value.getValue(), aggregate.getCount() + 1),
                        Materialized.<String, CumulativeAverageData, WindowStore<Bytes, byte[]>>as("average-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new JsonSerde<>(CumulativeAverageData.class))
                );
        windowedStream.toStream()
                .map((key, value) -> new KeyValue<>(key.key(), new ChartData(key.key(), value.getSum() / value.getCount(), key.window().end())))
                .to("average-result-topic", Produced.with(Serdes.String(), valueSerde));

        return null;
    }

    @Data
    @AllArgsConstructor
    public class CumulativeAverageData {
        private double sum;
        private long count;
        private double avg;
    }
}

