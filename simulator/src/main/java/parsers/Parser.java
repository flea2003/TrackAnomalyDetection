package parsers;
import helperObjects.Timestamp;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;


public interface Parser {
    List<SimpleEntry<Timestamp, String>> parse() throws IOException;
}
