package commons;

import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class PipelineObject {
    /**
     *
     * @return hashmap
     */
    public HashMap<Integer, Integer> returnHashMap() {
        return new HashMap<Integer, Integer>();
    };
}
