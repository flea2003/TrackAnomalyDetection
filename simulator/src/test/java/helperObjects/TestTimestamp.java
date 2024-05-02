package helperObjects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TestTimestamp {

    Timestamp timestamp1;
    Timestamp timestamp2;

    @BeforeEach
    public void setUp() {
        timestamp1 = new Timestamp(2004, 01, 27, 10, 0);
        timestamp2 = new Timestamp(2003, 01, 28, 10, 15);
    }

    @Test
    public void testCompareToMore(){
        assertThat(timestamp1.compareTo(timestamp2)).isEqualTo(1);
    }

    @Test
    public void testCompareToEquals(){
        assertThat(timestamp1.compareTo(timestamp1)).isEqualTo(0);
    }
    @Test
    public void testCompareToLess(){
        assertThat(timestamp2.compareTo(timestamp1)).isEqualTo(-1);
    }

    @Test
    void testGetYear() {
        assertThat(timestamp1.getYear()).isEqualTo(2004);
    }

    @Test
    void testGetMonth() {
        assertThat(timestamp1.getMonth()).isEqualTo(1);
    }

    @Test
    void testGetDay() {
        assertThat(timestamp1.getDay()).isEqualTo(27);
    }

    @Test
    void testGetMinute() {
        assertThat(timestamp1.getMinute()).isEqualTo(0);
    }

    @Test
    void testGetHour() {
        assertThat(timestamp1.getHour()).isEqualTo(10);
    }

    @Test
    void testEqualsTrue() {
        assertThat(timestamp1.equals(timestamp1)).isEqualTo(true);
    }

    @Test
    void testEqualsFalse() {
        assertThat(timestamp1.equals(timestamp2)).isEqualTo(false);
    }

    @Test
    void testEqualsFalseNull() {
        assertThat(timestamp1.equals(null)).isEqualTo(false);
    }

    @Test
    void testEqualsTrue2() {
        assertThat(timestamp1.equals(new Timestamp(2004, 01, 27, 10, 0))).isEqualTo(true);
    }

    @Test
    void testToString() {
        assertThat(timestamp1.toString()).isEqualTo("Timestamp{year=2004, month=1, day=27, minute=0, hour=10}");
    }

    @Test
    void testDifference() {
        assertThat(timestamp1.difference(new Timestamp(2004, 01, 27, 10, 1))).isEqualTo(-1);
    }
}
