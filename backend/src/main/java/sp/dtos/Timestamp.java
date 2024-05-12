package sp.dtos;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDateTime;

@JsonSerialize
public class Timestamp implements Comparable<Timestamp> {

    private int year;
    private int month;
    private int day;
    private int minute;
    private int hour;
    private String timestamp;

    /**
     * Method used for creating an object when deparsing a JSON object
     * @param time - the string in the JSON object
     */
    public Timestamp(String time) {
        String[] values = time.split(" ");
        String[] yearsMonthsDays = values[0].split("/");
        String[] minutesHours = values[1].split(":");

        this.year = Integer.parseInt(yearsMonthsDays[2]);
        this.month = Integer.parseInt(yearsMonthsDays[1]);
        this.day = Integer.parseInt(yearsMonthsDays[0]);
        this.hour = Integer.parseInt(minutesHours[0]);
        this.minute = Integer.parseInt(minutesHours[1]);
        this.timestamp = time;
    }

    /**
     * Constructor for a helper object, that corresponds to the timestamp of an AIS signal
     *
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     */
    public Timestamp(int year, int month, int day, int hour, int minute) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.minute = minute;
        this.hour = hour;
    }

    /**
     * CompareTo method for comparing two timestamps
     *
     * @param timestamp the object to be compared.
     * @return integer that corresponds to weather the comparable timestamp corresponds to an earlier date
     */
    @Override
    public int compareTo(Timestamp timestamp) {
        LocalDateTime dateTime1 = LocalDateTime.of(year, month, day, hour, minute);
        LocalDateTime dateTime2 = LocalDateTime.of(timestamp.year, timestamp.month, timestamp.day,
                timestamp.hour, timestamp.minute);

        if (dateTime1.isBefore(dateTime2)) return -1;
        if (dateTime1.isEqual(dateTime2)) return 0;
        else return 1;
    }

    /**
     * Returns the difference of two timestamps in minutes
     *
     * @param timestamp timestamp to be compared
     * @return  the difference of two timestamps in minutes
     */
    public long difference(Timestamp timestamp) {
        LocalDateTime dateTime1 = LocalDateTime.of(year, month, day, hour, minute);
        LocalDateTime dateTime2 = LocalDateTime.of(timestamp.year, timestamp.month, timestamp.day,
                timestamp.hour, timestamp.minute);
        return Duration.between(dateTime2, dateTime1).toMinutes();
    }

    @JsonValue
    public String getTimestamp(){
        return this.timestamp;
    }

}
