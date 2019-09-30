package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RequestedScaMethodEntity {
    private String requestedApprovalMethod;

    private RequestedScaMethodEntity(String scaMethod) {
        this.requestedApprovalMethod = scaMethod;
    }

    public static RequestedScaMethodEntity create(String scaMethod) {
        return new RequestedScaMethodEntity(scaMethod);
    }
}
