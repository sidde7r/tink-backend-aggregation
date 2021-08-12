package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EmailAddressEntity {
    private String address;
    private String lastUpdated;
    private String priority;
}
