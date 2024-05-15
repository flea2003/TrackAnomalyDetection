package parsers;

import helperobjects.AISSignal;
import helperobjects.Timestamp;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

public record DEBSParser(BufferedReader reader) implements Parser {

    /**
     * Constructor for the data parser of DEBS public dataset.
     *
     * @param reader the reader for the datafile
     */
    public DEBSParser {
    }

    /**
     * Parses the datafile and returns a list of pairs, where a pair has a timestamp and the
     * string representation of the signal.
     *
     * @return a list of pairs, where a pair has a timestamp and the string representation of the
     *     signal, extracted from the data file
     */

    @Override
    public List<SimpleEntry<Timestamp, String>> parse() throws IOException {
        List<SimpleEntry<Timestamp, String>> result = new ArrayList<>();

        this.reader.readLine(); // skip first line
        String line = this.reader.readLine();

        while (line != null) {
            String[] values = line.split(",");
            result.add(new SimpleEntry<>(parseDate(values[6]), parseAISSignal(values).toJson()));

            line = this.reader.readLine();
        }

        return result;
    }

    /**
     * Parse a line to an AIS signal object.
     *
     * @param values array of strings corresponding to one line
     * @return AISSignal signal object
     */
    public AISSignal parseAISSignal(String[] values) {
        String shipHash = values[0];
        float speed = Float.parseFloat(values[1]);
        float lon = Float.parseFloat(values[2]);
        float lat = Float.parseFloat(values[3]);
        float course = Float.parseFloat(values[4]);
        float heading = Float.parseFloat(values[5]);
        OffsetDateTime date = parseToISO8601(values[6]);

        String departurePort = values[7];
        if (departurePort.endsWith("\n")) departurePort = departurePort.substring(0, departurePort.length() - 1);

        return new AISSignal(shipHash, speed, lon, lat, course, heading, date, departurePort);
    }

    /**
     * Extracts the timestamp from the timestamp string.
     *
     * @param line a string that corresponds to a time stamp in the data file
     * @return the Date object extracted from the line
     */
    public Timestamp parseDate(String line) {
        String[] values = line.split(" ");
        String[] yearsMonthsDays = values[0].split("/");
        String[] minutesHours = values[1].split(":");

        int year = Integer.parseInt(yearsMonthsDays[2]);
        int month = Integer.parseInt(yearsMonthsDays[1]);
        int day = Integer.parseInt(yearsMonthsDays[0]);
        int hour = Integer.parseInt(minutesHours[0]);
        int minute = Integer.parseInt(minutesHours[1]);

        return new Timestamp(year, month, day, hour, minute);
    }

    private OffsetDateTime parseToISO8601(String date) {

        Timestamp t = parseDate(date);
        return OffsetDateTime.of(t.year(), t.month(), t.day(), t.hour(), t.minute(), 0, 0, ZoneOffset.ofHours(0));
    }
}
