package helperobjects;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import parsers.DEBSParser;
import parsers.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TestSimulator {

    Simulator simulator;
    Timestamp startTime;
    Timestamp endTime;
    Parser parser;
    String topicName;
    KafkaProducer<String, String> producer;
    BufferedReader reader;
    List<SimpleEntry<Timestamp, String>> resultingData;
    OffsetDateTime dateTime1 = OffsetDateTime.of(2004, 01, 27, 1,1,0,0, ZoneOffset.ofHours(0));
    OffsetDateTime dateTime2 = OffsetDateTime.of(2004, 01, 27, 1,2,0,0, ZoneOffset.ofHours(0));
    String date1 = "2004-01-27T01:01:00Z";
    String date2 = "2004-01-27T01:02:00Z";

    String startSignal = "{\"producerID\":\"simulator\",\"shipHash\":\"first\",\"speed\":1.9,\"longitude\":14.54255,\"latitude\":35.8167,\"course\":25.0,\"heading\":1.0,\"timestamp\":\"2004-01-27T01:01:00Z\",\"departurePort\":\"VALLETTA\"}";
    String secondSignal = "{\"producerID\":\"simulator\",\"shipHash\":\"second\",\"speed\":0.6,\"longitude\":-5.3482,\"latitude\":35.92638,\"course\":8.0,\"heading\":284.0,\"timestamp\":\"2004-01-27T01:01:00Z\",\"departurePort\":\"CEUTA\"}";
    String endSignal = "{\"producerID\":\"simulator\",\"shipHash\":\"third\",\"speed\":0.6,\"longitude\":-5.3482,\"latitude\":35.92638,\"course\":8.0,\"heading\":284.0,\"timestamp\":\"2004-01-27T01:02:00Z\",\"departurePort\":\"CEUTA\"}";


    @BeforeEach
    public void setup() throws IOException {
        this.reader = mock(BufferedReader.class);
        this.producer = mock(KafkaProducer.class);
        this.topicName = "testTopic";
        startTime = new Timestamp(2004, 1, 27, 01, 01);
        endTime = new Timestamp(2004, 1, 27, 01, 02);
        resultingData = new ArrayList<>(List.of(
                new SimpleEntry<>(startTime, startSignal),
                new SimpleEntry<>(new Timestamp(2004, 1, 27, 01, 01), secondSignal),
                new SimpleEntry<>(endTime, endSignal)
        ));
        reader = mock(BufferedReader.class);
        when(reader.readLine())
                .thenReturn("VESSEL_HASH,speed,LON,LAT,COURSE,HEADING,TIMESTAMP,departurePortName\n")
                .thenReturn("first,1.9,14.54255,35.8167,25,1,27/01/2004 01:01,VALLETTA\n")
                .thenReturn("second,0.6,-5.3482,35.92638,8,284,27/01/2004 01:01,CEUTA\n")
                .thenReturn("third,0.6,-5.3482,35.92638,8,284,27/01/2004 01:02,CEUTA\n")
                .thenReturn(null);

        this.parser = new DEBSParser(reader);
        this.simulator = new Simulator(parser, startTime, endTime, topicName, producer);
        this.simulator.setSpeed(60);
    }

    @Test
    void testSpeed() throws IOException, InterruptedException {
        Stream stream = new Stream(startTime, endTime);
        stream.setData(resultingData);
        simulator = new Simulator(stream, topicName, producer);
        simulator.setSpeed(60);

        long expectedSleepMillis = 1000; // Calculate expected sleep time

        long startSleepTime = System.currentTimeMillis(); // Record start time of sleep
        simulator.startStream();
        long endSleepTime = System.currentTimeMillis(); // Record end time of sleep
        long actualSleepTime = endSleepTime - startSleepTime; // Calculate actual sleep time

        // Assert that the actual sleep time is within an acceptable range of the expected sleep time
        assertThat(actualSleepTime).isBetween(expectedSleepMillis * 95 / 100, expectedSleepMillis * 105 / 100); // Allow a 5% margin of error
    }


    @Test
    void testStartStream() throws IOException, InterruptedException {
        InOrder inOrder = inOrder(producer);
        simulator.startStream();

        verify(reader, times(5)).readLine();
        AssertionsForClassTypes.assertThat(simulator.getStream().getData()).isEqualTo(resultingData);
        inOrder.verify(producer).send(
                eq(new ProducerRecord<>(topicName, startSignal)),
                argThat((ArgumentMatcher<Callback>) (callback) -> {
                    if (callback instanceof Callback) {
                        callback.onCompletion(null, null);
                        return true;
                    } else {
                        return false;
                    }
                })
                );
        inOrder.verify(producer).flush();
        inOrder.verify(producer).send(
                eq(new ProducerRecord<>(topicName, secondSignal)),
                argThat((ArgumentMatcher<Callback>) (callback) -> {
                    if (callback instanceof Callback) {
                        callback.onCompletion(null, null);
                        return true;
                    } else {
                        return false;
                    }
                }));
        inOrder.verify(producer).flush();
        inOrder.verify(producer).send(
                eq(new ProducerRecord<>(topicName, endSignal)),
                argThat((ArgumentMatcher<Callback>) (callback) -> {
                    if (callback instanceof Callback) {
                        callback.onCompletion(null, null);
                        return true;
                    } else {
                        return false;
                    }
                }));

        inOrder.verify(producer).flush();
        inOrder.verifyNoMoreInteractions();
    }


    @Test
    void testGetStream() throws IOException {
        Stream stream = new Stream(startTime, endTime);
        stream.setData(resultingData);
        AssertionsForClassTypes.assertThat(simulator.getStream()).isEqualTo(stream);
        verify(reader, times(5)).readLine();
    }

    @Test
    void testSecondConstructor() throws IOException, InterruptedException {
        Stream stream = new Stream(startTime, endTime);
        stream.setData(resultingData);
        simulator = new Simulator(stream, topicName, producer);
        simulator.setSpeed(60);

        InOrder inOrder = inOrder(producer);
        simulator.startStream();

        verify(reader, times(5)).readLine();
        AssertionsForClassTypes.assertThat(simulator.getStream().getData()).isEqualTo(resultingData);
        inOrder.verify(producer).send(
                eq(new ProducerRecord<>(topicName, startSignal)),
                any());
        inOrder.verify(producer).flush();
        inOrder.verify(producer).send(
                eq(new ProducerRecord<>(topicName, secondSignal)),
                any());
        inOrder.verify(producer).flush();
        inOrder.verify(producer).send(
                eq(new ProducerRecord<>(topicName, endSignal)),
                any());

        inOrder.verify(producer).flush();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void testEmptyStream() throws IOException, InterruptedException {
        Stream stream = new Stream(startTime, endTime);
        stream.setData(new ArrayList<>());
        simulator = new Simulator(stream, topicName, producer);
        simulator.setSpeed(60);
        simulator.startStream();

        verify(reader, times(5)).readLine();
        AssertionsForClassTypes.assertThat(simulator.getStream().getData()).isEqualTo(new ArrayList<>());
        verifyNoMoreInteractions(reader);
    }

}
