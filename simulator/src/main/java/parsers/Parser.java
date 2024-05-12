package parsers;

import helperobjects.Timestamp;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;

public interface Parser {

    /**
     * General method that parsers should implement.
     *
     * @return a list of the parsed AIS data with corresponding timestamp
     * @throws IOException when parsing is failed
     */
    List<SimpleEntry<Timestamp, String>> parse() throws IOException;
}
