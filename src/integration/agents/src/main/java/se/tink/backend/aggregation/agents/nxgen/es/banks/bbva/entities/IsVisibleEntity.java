package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IsVisibleEntity {
    private String channelId;
    private boolean granted;

    public String getChannelId() {
        return channelId;
    }

    public boolean isGranted() {
        return granted;
    }
}
