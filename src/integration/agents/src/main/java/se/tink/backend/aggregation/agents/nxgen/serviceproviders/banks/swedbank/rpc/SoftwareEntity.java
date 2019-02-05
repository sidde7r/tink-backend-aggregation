package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SoftwareEntity {
    private String name;
    private String version;
    private String family;
}
