package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CodeAppScaEntity {
    private boolean bindSca = true;
    private String scaInstanceId = "";
    private String token;

    public CodeAppScaEntity(String token) {
        this.token = token;
    }
}
