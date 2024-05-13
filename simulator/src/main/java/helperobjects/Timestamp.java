package helperobjects;

import java.time.Duration;
import java.time.LocalDateTime;

public record Timestamp(int year, int month, int day, int hour, int minute) implements Comparable<Timestamp> {

    /**
     * Constructor for a helper object, that corresponds to the timestamp of an AIS signal.
     *
     * @param year   integer representing year
     * @param month  integer representing month
     * @param day    integer representing day
     * @param hour   integer representing hour
     * @param minute integer representing minute
     */
    public Timestamp {
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
     * @return the difference of two timestamps in minutes
     */
    public long difference(Timestamp timestamp) {
        LocalDateTime dateTime1 = LocalDateTime.of(year, month, day, hour, minute);
        LocalDateTime dateTime2 = LocalDateTime.of(timestamp.year, timestamp.month, timestamp.day,
                timestamp.hour, timestamp.minute);
        return Duration.between(dateTime2, dateTime1).toMinutes();
    }
}
