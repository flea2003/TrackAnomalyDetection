package parsers;

import helperobjects.AISSignal;
import helperobjects.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.Mockito.mock;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

public class TestDebsParser {

    DEBSParser debsParser;
    BufferedReader reader;
    List<AbstractMap.SimpleEntry<Timestamp, String>> resultObject;
    OffsetDateTime dateTime = OffsetDateTime.of(2024, 01, 27, 10,10,0,0, ZoneOffset.ofHours(0));

    @BeforeEach
    public void setUp() throws IOException {
        reader = mock(BufferedReader.class);
        when(reader.readLine())
                .thenReturn("VESSEL_HASH,speed,LON,LAT,COURSE,HEADING,TIMESTAMP,departurePortName\n")
                .thenReturn("0x97df717d828ac6df388396b8e48ec1299e837917,1.9,14.54255,35.8167,25,1,27/01/2024 10:10,VALLETTA\n")
                .thenReturn("0xd7aeaeb3986186e3550aa68bd1561f8df9672d17,0.6,-5.3482,35.92638,8,284,28/01/2024 10:10,CEUTA\n")
                .thenReturn(null);  // It could be common to return null t
        debsParser = new DEBSParser(reader);
        resultObject = new ArrayList<>();
        resultObject.add(new AbstractMap.SimpleEntry<>(new Timestamp(2024, 1, 27, 10, 10), "{\"producerID\":\"simulator\",\"shipHash\":\"0x97df717d828ac6df388396b8e48ec1299e837917\",\"speed\":1.9,\"longitude\":14.54255,\"latitude\":35.8167,\"course\":25.0,\"heading\":1.0,\"timestamp\":\"2024-01-27T10:10:00Z\",\"departurePort\":\"VALLETTA\"}"));
        resultObject.add(new AbstractMap.SimpleEntry<>(new Timestamp(2024, 1, 28, 10, 10), "{\"producerID\":\"simulator\",\"shipHash\":\"0xd7aeaeb3986186e3550aa68bd1561f8df9672d17\",\"speed\":0.6,\"longitude\":-5.3482,\"latitude\":35.92638,\"course\":8.0,\"heading\":284.0,\"timestamp\":\"2024-01-28T10:10:00Z\",\"departurePort\":\"CEUTA\"}"));}

    @Test
    void testParseAISSignal() {
        assertThat(debsParser.parseAISSignal(new String[]{"a", "1","2.0","3","4","5.5","27/01/2024 10:10", "c"})).isEqualTo(new AISSignal("simulator", "a", 1F,2,3,4,5.5F,dateTime, "c"));
    }

    @Test
    void testParse() throws IOException {
        assertThat(debsParser.parse()).isEqualTo(resultObject);
    }

    @Test
    void testParseDate() {
        assertThat(debsParser.parseDate("28/04/2015 20:26")).isEqualTo(new Timestamp(2015, 4, 28, 20, 26));
    }

    @Test
    void testGetFilename() {
        assertThat(debsParser.reader()).isEqualTo(reader);
    }
}
