package sp.dtos;

import lombok.Builder;

public interface StreamRecord {
    String getShipHash();
    String toJson();
}
