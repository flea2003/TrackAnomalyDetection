package helperObjects;

import java.time.Duration;
import java.time.LocalDateTime;

public class Timestamp implements Comparable<Timestamp> {

    private int year;
    private int month;
    private int day;
    private int minute;
    private int hour;

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

    /**
     * Gets year.
     *
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * Gets month.
     *
     * @return the month
     */
    public int getMonth() {
        return month;
    }

    /**
     * Gets day.
     *
     * @return the day
     */
    public int getDay() {
        return day;
    }

    /**
     * Gets minute.
     *
     * @return the minute
     */
    public int getMinute() {
        return minute;
    }

    /**
     * Gets hour.
     *
     * @return the hour
     */
    public int getHour() {
        return hour;
    }

    /**
     * Equals method
     *
     * @param o object that is being compared
     * @return whether two objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timestamp timestamp = (Timestamp) o;
        return year == timestamp.year && month == timestamp.month && day == timestamp.day
                && minute == timestamp.minute && hour == timestamp.hour;
    }

    /**
     * Returns object as a string
     *
     * @return object representation in human-readable format
     */
    @Override
    public String toString() {
        return "Timestamp{" +
                "year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", minute=" + minute +
                ", hour=" + hour +
                '}';
    }
}
