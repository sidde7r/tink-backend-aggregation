package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.utils;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class JWTHeader {

    private String alg;
    private String typ;

    public JWTHeader(String alg, String typ) {
        this.alg = alg;
        this.typ = typ;
    }
}
