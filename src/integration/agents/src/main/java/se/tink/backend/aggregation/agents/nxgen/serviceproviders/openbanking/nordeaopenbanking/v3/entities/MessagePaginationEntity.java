package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MessagePaginationEntity {
    private String continuationKey;

    public String getContinuationKey() {
        return continuationKey;
    }
}
