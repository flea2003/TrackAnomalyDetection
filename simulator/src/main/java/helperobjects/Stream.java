package helperobjects;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import parsers.Parser;

@Setter
@Getter
@EqualsAndHashCode
@ToString
public class Stream {

    private List<SimpleEntry<Timestamp, String>> data;
    private Timestamp streamStart;
    private Timestamp streamEnd;

    /**
     * Constructor for the Stream object.
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
     * Parses the data for the data file.
     *
     * @param parser parser for a needed data file
     * @return parsed data from the data file
     */
    public List<SimpleEntry<Timestamp, String>> parseData(Parser parser) throws IOException {
        this.data = parser.parse();
        return this.data;
    }

    /**
     * Sorts the parsed data according to its timestamp.
     */
    public void sortStream() {
        if (data == null || data.isEmpty()) return;
        data.sort(java.util.Map.Entry.comparingByKey());
    }

    /**
     * Returns the actual data that will be streamed, that corresponds to the signals that should be streamed in between
     * the specified starting timestamp and ending timestamp.
     *
     * @return the actual data that will be streamed
     */
    public List<SimpleEntry<Timestamp, String>> getData() {
        if (this.data == null) return Collections.emptyList();
        else return this.data.stream()
                .filter(entry -> entry.getKey().compareTo(this.streamStart) >= 0
                        && entry.getKey().compareTo(this.streamEnd) <= 0)
                .collect(Collectors.toList());
    }

}
