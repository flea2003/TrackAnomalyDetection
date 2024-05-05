package dev.system.backend.commons;

import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class PipelineObject {
    public HashMap<Integer, Integer> returnHashMap() {
        return new HashMap<Integer, Integer>();
    };
}
