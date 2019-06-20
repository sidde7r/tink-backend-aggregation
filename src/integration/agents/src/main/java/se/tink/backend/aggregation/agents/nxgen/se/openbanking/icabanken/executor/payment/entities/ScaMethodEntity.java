package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaMethodEntity {
    private String authenticationType;
    private String authenticationVersion;
    private String authenticationMethodId;
    private String name;
    private String explanation;
}
