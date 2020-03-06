package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FloatingKeyboardEntity {
    private String enabled;
    private String key;

    public String getEnabled() {
        return enabled;
    }

    public String getKey() {
        return key;
    }

    public static FloatingKeyboardEntity of(String enabled, String key) {
        final FloatingKeyboardEntity entity = new FloatingKeyboardEntity();
        entity.enabled = enabled;
        entity.key = key;
        return entity;
    }
}
