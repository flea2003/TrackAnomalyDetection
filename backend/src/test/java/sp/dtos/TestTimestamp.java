package sp.dtos;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class TestTimestamp {

    @Test
    void testDifference(){
        Timestamp timestamp1 = new Timestamp(2024, 12, 30, 4, 50);
        Timestamp timestamp2 = new Timestamp(2024, 12, 30, 4, 55);
        assertThat(timestamp2.difference(timestamp1)).isEqualTo(5);
    }

    @Test
    void testDifference2(){
        Timestamp timestamp1 = new Timestamp(2024, 12, 30, 4, 50);
        Timestamp timestamp2 = new Timestamp(2024, 12, 30, 4, 55);
        assertThat(timestamp1.difference(timestamp2)).isEqualTo(-5);
    }

    @Test
    void testConstructors(){
        Timestamp timestamp1 = new Timestamp(2024, 12, 30, 4, 50);
        Timestamp timestamp2 = new Timestamp("30/12/2024 04:50");
        assertThat(timestamp1).isEqualTo(timestamp2);
    }

    @Test
    void testComparator1(){
        Timestamp timestamp1 = new Timestamp(2024, 12, 30, 4, 50);
        Timestamp timestamp2 = new Timestamp("30/12/2024 04:52");
        assertThat(timestamp1.compareTo(timestamp2)).isEqualTo(-1);
    }

    @Test
    void testComparator2(){
        Timestamp timestamp1 = new Timestamp(2024, 12, 30, 4, 50);
        Timestamp timestamp2 = new Timestamp("30/12/2024 04:50");
        assertThat(timestamp1.compareTo(timestamp2)).isEqualTo(0);
    }

    @Test
    void testGetterTimestamp(){
        Timestamp timestamp1 = new Timestamp(2024, 12, 30, 4, 50);
        assertThat(timestamp1.getTimestamp()).isEqualTo("30/12/2024 04:50");
    }
}
