package sp.dtos;

import org.junit.jupiter.api.Test;

public class TestTimestamp {

    @Test
    void test1(){
        Timestamp timestamp1 = new Timestamp(2024, 12, 30, 4, 50);
        Timestamp timestamp2 = new Timestamp(2024, 12, 30, 4, 55);
        System.out.println(timestamp2.difference(timestamp1));

    }

    @Test
    void test2(){
        Timestamp timestamp1 = new Timestamp(2024, 12, 30, 4, 50);
        Timestamp timestamp2 = new Timestamp(2024, 12, 30, 4, 55);
        System.out.println(timestamp1.difference(timestamp2));

    }
}
