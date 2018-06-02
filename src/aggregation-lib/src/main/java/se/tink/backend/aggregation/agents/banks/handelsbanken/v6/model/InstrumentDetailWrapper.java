package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstrumentDetailWrapper {
    private List<HeaderValueEntity> lines;

    public List<HeaderValueEntity> getLines() {
        return lines;
    }

    public void setLines(List<HeaderValueEntity> lines) {
        this.lines = lines;
    }

    public Map<String, String> asMapKeyValueMap() {
        if (lines == null) {
            return Collections.emptyMap();
        }

        return lines.stream()
                .map(HeaderValueEntity::asKeyValueMap)
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(
                        Collectors.toMap(
                                entry -> entry.getKey().toLowerCase(),
                                Map.Entry::getValue));
    }
}
