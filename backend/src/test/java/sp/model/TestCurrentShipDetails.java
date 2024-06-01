package sp.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.giladam.kafka.jacksonserde.Jackson2Serde;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import sp.dtos.ExternalAISSignal;
import sp.pipeline.utils.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCurrentShipDetails {

    @Test
    void testToJsonNullAISSignal() throws JsonProcessingException {
        OffsetDateTime dateTime = OffsetDateTime.of(2004, 1, 27, 1,1,0,0, ZoneOffset.ofHours(0));
        AnomalyInformation anomalyInformation = new AnomalyInformation(0.5F, "explanation", null, 1L);
        AISSignal ais =  new AISSignal(123L, 22.5f, 130.0f, 45.0f, 180.0f, 90.0f, dateTime, "New York");
        MaxAnomalyScoreDetails maxAnomalyScoreDetails = new MaxAnomalyScoreDetails(0.5F, null);

        CurrentShipDetails currentShipDetails = new CurrentShipDetails(anomalyInformation, ais, maxAnomalyScoreDetails);

        String json = JsonMapper.toJson(currentShipDetails);
        System.out.println(json);
        System.out.println(JsonMapper.fromJson(json, CurrentShipDetails.class));
        System.out.println(JsonMapper.fromJson("{\"currentAnomalyInformation\":{\"score\":0.5,\"explanation\":\"explanation\",\"correspondingTimestamp\":null,\"id\":1},\"currentAISSignal\":{\"id\":123,\"speed\":22.5,\"longitude\":130.0,\"latitude\":45.0,\"course\":180.0,\"heading\":90.0,\"timestamp\":\"2004-01-27T01:01:00Z\",\"departurePort\":\"New York\",\"receivedTime\":null},\"maxAnomalyScoreInfo\":{\"maxAnomalyScore\":0.5,\"correspondingTimestamp\":null}}\n", CurrentShipDetails.class));
//        "{\"currentAnomalyInformation\":{\"score\":0.5,\"explanation\":\"explanation\",\"correspondingTimestamp\":null,\"id\":1},\"currentAISSignal\":{\"id\":123,\"speed\":22.5,\"longitude\":130.0,\"latitude\":45.0,\"course\":180.0,\"heading\":90.0,\"timestamp\":\"2004-01-27T01:01:00Z\",\"departurePort\":\"New York\",\"receivedTime\":null},\"maxAnomalyScoreInfo\":{\"maxAnomalyScore\":0.5,\"correspondingTimestamp\":null}}\n"
        System.out.println(JsonMapper.fromJson("{\"maxAnomalyScoreInfo\":\"{\\\"maxAnomalyScore\\\":25.0,\\\"correspondingTimestamp\\\":\\\"2015-04-10T23:21:00Z\\\"}\",\"currentAnomalyInformation\":\"{\\\"score\\\":25.0,\\\"explanation\\\":\\\"Heading was not given (value 511).\\\\n\\\",\\\"correspondingTimestamp\\\":\\\"2015-04-10T23:21:00Z\\\",\\\"id\\\":616279638}\",\"currentAISSignal\":\"{\\\"id\\\":616279638,\\\"speed\\\":0.0,\\\"longitude\\\":14.51687,\\\"latitude\\\":35.89673,\\\"course\\\":70.0,\\\"heading\\\":511.0,\\\"timestamp\\\":\\\"2015-04-10T23:21:00Z\\\",\\\"departurePort\\\":\\\"MARSAXLOKK\\\",\\\"receivedTime\\\":\\\"2024-05-30T11:58:07.0292017Z\\\"}\"}\n", CurrentShipDetails.class));
        /*
        {"maxAnomalyScoreInfo":"{\"maxAnomalyScore\":25.0,\"correspondingTimestamp\":\"2015-04-10T23:21:00Z\"}","currentAnomalyInformation":"{\"score\":25.0,\"explanation\":\"Heading was not given (value 511).\\n\",\"correspondingTimestamp\":\"2015-04-10T23:21:00Z\",\"id\":616279638}","currentAISSignal":"{\"id\":616279638,\"speed\":0.0,\"longitude\":14.51687,\"latitude\":35.89673,\"course\":70.0,\"heading\":511.0,\"timestamp\":\"2015-04-10T23:21:00Z\",\"departurePort\":\"MARSAXLOKK\",\"receivedTime\":\"
         */
        // check if conversion to both sides resulted in the same object
//        assertEquals(shipInformation, JsonMapper.fromJson(json, ShipInformation.class));
    }

    @Test
    void test() throws JsonProcessingException{
        OffsetDateTime dateTime = OffsetDateTime.of(2004, 1, 27, 1,1,0,0, ZoneOffset.ofHours(0));
        MaxAnomalyScoreDetails maxAnomalyScoreDetails = new MaxAnomalyScoreDetails(0.5F, dateTime);
        String json = JsonMapper.toJson(maxAnomalyScoreDetails);
//        ExternalAISSignal deserialized = ExternalAISSignal.fromJson(json);
//        assertThat(externalAISSignal).isEqualTo(deserialized);
        System.out.println(JsonMapper.fromJson(json, MaxAnomalyScoreDetails.class));
    }

    @Test
    void testGetSerdeIsSerde() {
        assertThat(CurrentShipDetails.getSerde()).isNotNull();
        assertThat(CurrentShipDetails.getSerde()).isExactlyInstanceOf(Jackson2Serde.class);
    }
}
