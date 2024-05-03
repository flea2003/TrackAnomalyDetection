import helperObjects.Stream;
import helperObjects.Timestamp;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import parsers.DEBSParser;
import parsers.Parser;

import java.io.BufferedReader;
import java.io.IOException;
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
    String startSignal = "{\"shipHash\":\"first\",\"speed\":1.9,\"longitude\":14.54255,\"latitude\":35.8167,\"course\":25.0,\"heading\":1.0,\"timestamp\":\"27/01/2024 10:10\",\"departurePort\":\"VALLETTA\"}";
    String endSignal = "{\"shipHash\":\"second\",\"speed\":0.6,\"longitude\":-5.3482,\"latitude\":35.92638,\"course\":8.0,\"heading\":284.0,\"timestamp\":\"27/01/2024 10:11\",\"departurePort\":\"CEUTA\"}";
    String thirdSignal = "{\"shipHash\":\"third\",\"speed\":0.6,\"longitude\":-5.3482,\"latitude\":35.92638,\"course\":8.0,\"heading\":284.0,\"timestamp\":\"27/01/2024 10:10\",\"departurePort\":\"CEUTA\"}";


    @BeforeEach
    public void setup() throws IOException {
        this.reader = mock(BufferedReader.class);
        this.producer = mock(KafkaProducer.class);
        this.topicName = "testTopic";
        startTime = new Timestamp(2024, 1, 27, 10, 10);
        endTime = new Timestamp(2024, 1, 27, 10, 11);
        resultingData = new ArrayList<>(List.of(
                new SimpleEntry<>(startTime, startSignal),
                new SimpleEntry<>(new Timestamp(2024, 1, 27, 10, 10), thirdSignal),
                new SimpleEntry<>(endTime, endSignal)
        ));
        reader = mock(BufferedReader.class);
        when(reader.readLine())
                .thenReturn("VESSEL_HASH,speed,LON,LAT,COURSE,HEADING,TIMESTAMP,departurePortName\n")
                .thenReturn("first,1.9,14.54255,35.8167,25,1,27/01/2024 10:10,VALLETTA\n")
                .thenReturn("second,0.6,-5.3482,35.92638,8,284,27/01/2024 10:11,CEUTA\n")
                .thenReturn("third,0.6,-5.3482,35.92638,8,284,27/01/2024 10:10,CEUTA\n")
                .thenReturn(null);

        this.parser = new DEBSParser(reader);
        this.simulator = new Simulator(parser, startTime, endTime, topicName, producer);
        this.simulator.setSpeed(60);
    }


    @Test
    void testStartStream() throws IOException, InterruptedException {
        InOrder inOrder = inOrder(producer);
        simulator.startStream();

        verify(reader, times(5)).readLine();
        assertThat(simulator.getStream().getData()).isEqualTo(resultingData);
        inOrder.verify(producer).send(
                eq(new ProducerRecord<>(topicName, startSignal)),
                any());
        inOrder.verify(producer).flush();
        inOrder.verify(producer).send(
                eq(new ProducerRecord<>(topicName, thirdSignal)),
                any());
        inOrder.verify(producer).flush();
        inOrder.verify(producer).send(
                eq(new ProducerRecord<>(topicName, endSignal)),
                any());

        inOrder.verify(producer).flush();
        inOrder.verify(producer).close();
        inOrder.verifyNoMoreInteractions();
    }


    @Test
    void testGetStream() throws IOException {
        Stream stream = new Stream(startTime, endTime);
        stream.setData(resultingData);
        assertThat(simulator.getStream()).isEqualTo(stream);
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
        assertThat(simulator.getStream().getData()).isEqualTo(resultingData);
        inOrder.verify(producer).send(
                eq(new ProducerRecord<>(topicName, startSignal)),
                any());
        inOrder.verify(producer).flush();
        inOrder.verify(producer).send(
                eq(new ProducerRecord<>(topicName, thirdSignal)),
                any());
        inOrder.verify(producer).flush();
        inOrder.verify(producer).send(
                eq(new ProducerRecord<>(topicName, endSignal)),
                any());

        inOrder.verify(producer).flush();
        inOrder.verify(producer).close();
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
        assertThat(simulator.getStream().getData()).isEqualTo(new ArrayList<>());
        verifyNoMoreInteractions(reader);
    }
}
