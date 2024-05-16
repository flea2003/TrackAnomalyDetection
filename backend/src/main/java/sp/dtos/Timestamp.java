package sp.dtos;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.EqualsAndHashCode;

@JsonSerialize
@EqualsAndHashCode
public class Timestamp implements Comparable<Timestamp> {

    private int year;
    private int month;
    private int day;
    private int minute;
    private int hour;
    private String timestamp;

    /**
     * Method used to construct a Timestamp object when recieving an JSON object.
     *
     * @param time - the string of the timestamp
     */
    public Timestamp(String time) {
        String[] values = time.split(" ");

        String[] yearsMonthsDays = values[0].split("/");
        this.year = Integer.parseInt(yearsMonthsDays[2]);
        this.month = Integer.parseInt(yearsMonthsDays[1]);
        this.day = Integer.parseInt(yearsMonthsDays[0]);

        String[] minutesHours = values[1].split(":");
        this.hour = Integer.parseInt(minutesHours[0]);
        this.minute = Integer.parseInt(minutesHours[1]);
        this.timestamp = time;
    }

    /**
     * Constructor for a helper object, that corresponds to the timestamp of an AIS signal.
     *
     * @param year - the year of the timestamp
     * @param month - the month of the timestamp
     * @param day - the day of the timestamp
     * @param hour - the hour of the timestamp
     * @param minute - the minute of the timestamp
     */
    public Timestamp(int year, int month, int day, int hour, int minute) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.minute = minute;
        this.hour = hour;
        LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        this.timestamp = dateTimeFormatter.format(dateTime);
    }

    /**
     * CompareTo method for comparing two timestamps.
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
     * Returns the difference of two timestamps in minutes.
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
    public String getTimestamp() {
        return this.timestamp;
    }

    @Override
    public String toString() {
        return this.timestamp;
    }
}
