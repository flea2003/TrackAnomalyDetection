package helperobjects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parsers.DEBSParser;
import parsers.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TestStream {
/*
    Parser parser;
    BufferedReader reader;
    Timestamp startTime;
    Timestamp endTime;
    List<SimpleEntry<Timestamp, String>> resultingData;
    Stream stream;
    String startSignal = "{\"shipHash\":\"first\",\"speed\":1.9,\"longitude\":14.54255,\"latitude\":35.8167,\"course\":25.0,\"heading\":1.0,\"timestamp\":\"27/01/2024 10:10\",\"departurePort\":\"VALLETTA\"}";
    String endSignal = "{\"shipHash\":\"second\",\"speed\":0.6,\"longitude\":-5.3482,\"latitude\":35.92638,\"course\":8.0,\"heading\":284.0,\"timestamp\":\"26/02/2024 10:10\",\"departurePort\":\"CEUTA\"}";
    String thirdSignal = "{\"shipHash\":\"third\",\"speed\":0.6,\"longitude\":-5.3482,\"latitude\":35.92638,\"course\":8.0,\"heading\":284.0,\"timestamp\":\"26/02/2024 10:09\",\"departurePort\":\"CEUTA\"}";

    @BeforeEach
    void setUp() throws IOException {
        startTime = new Timestamp(2024, 1, 27, 10, 10);
        endTime = new Timestamp(2024, 2, 26, 10, 10);
        resultingData = new ArrayList<>(List.of(
                new SimpleEntry<>(startTime, startSignal),
                new SimpleEntry<>(endTime, endSignal),
                new SimpleEntry<>(new Timestamp(2024, 2, 26, 10, 9), thirdSignal)
        ));
        reader = mock(BufferedReader.class);
        when(reader.readLine())
                .thenReturn("VESSEL_HASH,speed,LON,LAT,COURSE,HEADING,TIMESTAMP,departurePortName\n")
                .thenReturn("first,1.9,14.54255,35.8167,25,1,27/01/2024 10:10,VALLETTA\n")
                .thenReturn("second,0.6,-5.3482,35.92638,8,284,26/02/2024 10:10,CEUTA\n")
                .thenReturn("third,0.6,-5.3482,35.92638,8,284,26/02/2024 10:09,CEUTA\n")
                .thenReturn(null);

        parser = new DEBSParser(reader);
        stream = new Stream(startTime, endTime);
    }

    @Test
    void testParseData() throws IOException {
        assertThat(stream.parseData(parser)).isEqualTo(resultingData);
        verify(reader, times(5)).readLine();
    }

    @Test
    void testGetDataWithoutParsing() throws IOException {
        assertThat(stream.getData()).isEqualTo(new ArrayList<>());
        verify(reader, times(0)).readLine();
    }

    @Test
    void testGetDataWithParsing() throws IOException {
        stream.parseData(parser);
        verify(reader, times(5)).readLine();
        assertThat(stream.getData()).isEqualTo(resultingData);
    }

    @Test
    void testGetNoData1() throws IOException {
        stream.setStreamStart(new Timestamp(20400, 1, 27, 10, 10));
        stream.setStreamEnd(new Timestamp(20400, 1, 27, 10, 10));
        stream.parseData(parser);
        verify(reader, times(5)).readLine();
        assertThat(stream.getData()).isEqualTo(new ArrayList<>());
    }

    @Test
    void testGetNoData2() throws IOException {
        stream.setStreamStart(new Timestamp(1, 1, 27, 10, 10));
        stream.setStreamEnd(new Timestamp(1, 1, 27, 10, 10));
        stream.parseData(parser);
        verify(reader, times(5)).readLine();
        assertThat(stream.getData()).isEqualTo(new ArrayList<>());
    }

    @Test
    void testSortData() throws IOException {
        stream.parseData(parser);
        verify(reader, times(5)).readLine();
        assertThat(stream.getData()).isEqualTo(resultingData);
        stream.sortStream();
        assertThat(stream.getData()).isEqualTo(new ArrayList<>(List.of(
                new SimpleEntry<>(startTime, startSignal),
                new SimpleEntry<>(new Timestamp(2024, 2, 26, 10, 9), thirdSignal),
                new SimpleEntry<>(endTime, endSignal)
        )));
    }

    @Test
    void testGetDataExcluded() throws IOException {
        stream.setStreamEnd(startTime);
        stream.parseData(parser);
        verify(reader, times(5)).readLine();
        assertThat(stream.getData()).isEqualTo(new ArrayList<>(List.of(
                new SimpleEntry<>(startTime, startSignal)
        )));
    }

    @Test
    void testToString() {
        assertThat(stream.toString()).isEqualTo("Stream(data=[], streamStart=Timestamp[year=2024, month=1, day=27, hour=10, minute=10], streamEnd=Timestamp[year=2024, month=2, day=26, hour=10, minute=10])");
    }

    @Test
    void testSetStreamEnd() {
        stream.setStreamEnd(new Timestamp(0,0,0,0,0));
        assertThat(stream.getStreamEnd()).isEqualTo(new Timestamp(0,0,0,0,0));
    }

    @Test
    void testSetStreamStart() {
        stream.setStreamStart(new Timestamp(0,0,0,0,0));
        assertThat(stream.getStreamStart()).isEqualTo(new Timestamp(0,0,0,0,0));
    }

    @Test
    void testEqualsNull() {
        assertThat(stream.equals(null)).isFalse();
    }

    @Test
    void testEqualsFalse() {
        assertThat(stream.equals(new Date(0,0,0,0,0))).isFalse();
    }

    @Test
    void testSortEmptyData1(){
        stream.setData(null);
        stream.sortStream();
        assertThat(stream.getData()).isEqualTo(new ArrayList<>());
    }

    @Test
    void testSortEmptyData2(){
        stream.setData(new ArrayList<>());
        stream.sortStream();
        assertThat(stream.getData()).isEqualTo(new ArrayList<>());
    }

    @Test
    void testGetEmptyData(){
        stream.setData(null);
        assertThat(stream.getData()).isEqualTo(new ArrayList<>());
    }

    @Test
    void testGetSimpleData(){
        stream.setData(resultingData);
        assertThat(stream.getData()).isEqualTo(resultingData);
    }

 */
}
