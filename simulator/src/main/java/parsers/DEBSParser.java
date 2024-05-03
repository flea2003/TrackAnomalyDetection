package parsers;

import helperObjects.AIS;
import helperObjects.Timestamp;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;

public class DEBSParser implements Parser {

    private BufferedReader reader;

    /**
     * Constructor for the data parser of DEBS public dataset
     *
     * @param reader the reader for the datafile
     */
    public DEBSParser(BufferedReader reader) {
        this.reader = reader;
    }

    /**
     * Parses the datafile and returns a list of pairs, where a pair has a timestamp and the
     * string representation of the signal
     *
     * @return a list of pairs, where a pair has a timestamp and the string representation of the
     * signal, extracted from the data file
     */

    @Override
    public List<SimpleEntry<Timestamp, String>> parse() throws IOException {
        List<SimpleEntry<Timestamp, String>> result = new ArrayList<>();

        String line = this.reader.readLine();
        while ((line = this.reader.readLine()) != null) {
            String[] values = line.split(",");
            result.add(new SimpleEntry<>(parseDate(values[6]), parseAIS(values).toJson()));
        }

        return result;
    }

    /**
     * Parse a line to an AIS signal object
     *
     * @param values array of strings corresponding to one line
     * @return AIS signal object
     * @throws IOException
     */
    public AIS parseAIS(String[] values) throws IOException {
        String shipHash, date, departurePort;
        float speed, lon, lat, course, heading;

        shipHash = values[0];
        speed = Float.parseFloat(values[1]);
        lon = Float.parseFloat(values[2]);
        lat = Float.parseFloat(values[3]);
        course = Float.parseFloat(values[4]);
        heading = Float.parseFloat(values[5]);
        date = values[6];

        departurePort = values[7];
        if (departurePort.endsWith("\n")) departurePort = departurePort.substring(0, departurePort.length() - 1);

        return new AIS(shipHash, speed, lon, lat, course, heading, date, departurePort);
    }

    /**
     * Extracts the timestamp from the timestamp string
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

    /**
     * Gets the reader.
     *
     * @return the buffered reader
     */
    public BufferedReader getReader() {
        return this.reader;
    }
}
