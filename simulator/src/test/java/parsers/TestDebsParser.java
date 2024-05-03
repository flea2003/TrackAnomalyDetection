package parsers;

import helperObjects.AIS;
import helperObjects.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
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

    @BeforeEach
    public void setUp() throws IOException {
        reader = mock(BufferedReader.class);
        when(reader.readLine())
                .thenReturn("VESSEL_HASH,speed,LON,LAT,COURSE,HEADING,TIMESTAMP,departurePortName\n")
                .thenReturn("0x97df717d828ac6df388396b8e48ec1299e837917,1.9,14.54255,35.8167,25,1,01/04/2015 20:19,VALLETTA\n")
                .thenReturn("0xd7aeaeb3986186e3550aa68bd1561f8df9672d17,0.6,-5.3482,35.92638,8,284,25/04/2015 05:12,CEUTA\n")
                .thenReturn(null);  // It could be common to return null t
        debsParser = new DEBSParser(reader);
        resultObject = new ArrayList<>();
        resultObject.add(new AbstractMap.SimpleEntry<>(new Timestamp(2015, 4, 1, 20, 19), "{\"shipHash\":\"0x97df717d828ac6df388396b8e48ec1299e837917\",\"speed\":1.9,\"longitude\":14.54255,\"latitude\":35.8167,\"course\":25.0,\"heading\":1.0,\"timestamp\":\"01/04/2015 20:19\",\"departurePort\":\"VALLETTA\"}"));
        resultObject.add(new AbstractMap.SimpleEntry<>(new Timestamp(2015, 4, 25, 5, 12), "{\"shipHash\":\"0xd7aeaeb3986186e3550aa68bd1561f8df9672d17\",\"speed\":0.6,\"longitude\":-5.3482,\"latitude\":35.92638,\"course\":8.0,\"heading\":284.0,\"timestamp\":\"25/04/2015 05:12\",\"departurePort\":\"CEUTA\"}"));}

    @Test
    void testParseAIS() throws IOException {
        assertThat(debsParser.parseAIS(new String[]{"a", "1","2.0","3","4","5.5","b", "c"})).isEqualTo(new AIS("a", 1F,2,3,4,5.5F,"b", "c"));
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
        assertThat(debsParser.getReader()).isEqualTo(reader);
    }
}
