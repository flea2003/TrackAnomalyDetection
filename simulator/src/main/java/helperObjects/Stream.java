package helperObjects;
import parsers.Parser;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Stream {

    private List<SimpleEntry<Timestamp, String>> data;
    private Timestamp streamStart;
    private Timestamp streamEnd;

    /**
     * Constructor for the Stream object
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
                .filter(entry -> entry.getKey().compareTo(this.streamStart) >= 0 && entry.getKey().compareTo(this.streamEnd) <= 0)
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

    /**
     * Returns the object as a string
     *
     * @return a string format of the object
     */
    @Override
    public String toString() {
        return "Stream{" +
                "data=" + data +
                ", streamStart=" + streamStart +
                ", streamEnd=" + streamEnd +
                '}';
    }

    /**
     *  Equals method
     *
     * @param o object that is being compared
     * @return whether or not two objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stream stream = (Stream) o;
        return Objects.equals(data, stream.data) && Objects.equals(streamStart, stream.streamStart) && Objects.equals(streamEnd, stream.streamEnd);
    }

    /**
     * Returns the start timestamp of the stream
     *
     * @return the starting timestamp of the stream
     */
    public Timestamp getStreamStart() {
        return streamStart;
    }

    /**
     * Returns the ending timestamp of the stream
     *
     * @return the ending timestamp of the stream
     */
    public Timestamp getStreamEnd() {
        return streamEnd;
    }
}
