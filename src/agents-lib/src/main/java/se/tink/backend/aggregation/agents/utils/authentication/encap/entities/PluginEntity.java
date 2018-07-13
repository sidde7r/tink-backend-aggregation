package se.tink.backend.aggregation.agents.utils.authentication.encap.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PluginEntity {
    private SigningEntity signing;

    public SigningEntity getSigning() {
        return signing;
    }
}
