package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class JwtHeader {
    String typ;
    String alg;

    public JwtHeader() {
        typ = "JWT";
        alg = "RS256";
    }
}
