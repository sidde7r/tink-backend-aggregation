package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class JWTHeader {
    private String alg;
    private String type;

    public JWTHeader(String alg, String type) {
        this.alg = alg;
        this.type = type;
    }
}
