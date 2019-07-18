package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class JwtHeader {

    private String alg;
    private String typ;

    public JwtHeader(String alg, String typ) {
        this.alg = alg;
        this.typ = typ;
    }
}
