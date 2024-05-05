package helperObjects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import parsers.Parser;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode
@ToString
public class Stream {

    private List<SimpleEntry<Timestamp, String>> data;
    private Timestamp streamStart;
    private Timestamp streamEnd;

    /**
     * Constructor for the Stream object
     *
     * @param streamStart starting timestamp for the stream
     * @param streamEnd ending timestamp for the stream
     *
     */
    public Stream(Timestamp streamStart, Timestamp streamEnd) {
        this.data = new ArrayList<>();
        this.streamStart = streamStart;
        this.streamEnd = streamEnd;
    }

    /**
     * Parses the data for the data file
     *
     * @param parser parser for a needed data file
     * @return parsed data from the data file
     */
    public List<SimpleEntry<Timestamp, String>> parseData(Parser parser) throws IOException {
        this.data = parser.parse();
        return this.data;
    }

    /**
     * Sorts the parsed data according to it's timestamp
     */
    public void sortStream() {
        if (data == null || data.isEmpty()) return;
        data.sort(java.util.Map.Entry.comparingByKey());
    }

    /**
     * Sets the timestamp for the first signal that should be streamed.
     *
     * @param streamStart timestamp for the start of the stream.
     */
    public void setStreamStart(Timestamp streamStart) {
        this.streamStart = streamStart;
    }

    /**
     * Sets the timestamp for the last signal that should be streamed.
     *
     * @param streamEnd timestamp for the end of the stream.
     */
    public void setStreamEnd(Timestamp streamEnd) {
        this.streamEnd = streamEnd;
    }

    /**
     * Returns the actual data that will be streamed, that corresponds to the signals that should be streamed in between
     * the specified starting timestamp and ending timestamp
     *
     * @return the actual data that will be streamed
     */
    public List<SimpleEntry<Timestamp, String>> getData() {
        if (this.data == null || this.data.isEmpty()) return new ArrayList<>();
        else return this.data.stream()
                .filter(entry -> entry.getKey().compareTo(this.streamStart) >= 0
                        && entry.getKey().compareTo(this.streamEnd) <= 0)
                .collect(Collectors.toList());
    }

    /**
     * Sets the data of the stream
     *
     * @param data data of the stream object
     */
    public void setData(List<SimpleEntry<Timestamp, String>> data) {
        this.data = data;
    }
}
