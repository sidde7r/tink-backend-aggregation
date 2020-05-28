package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity;

import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeymapEntity {
    private String contentType;
    private int height;
    private Map<String, String> keys;
    private List<Integer> margin;
    private int width;

    public Map<String, String> getKeys() {
        return keys;
    }
}
