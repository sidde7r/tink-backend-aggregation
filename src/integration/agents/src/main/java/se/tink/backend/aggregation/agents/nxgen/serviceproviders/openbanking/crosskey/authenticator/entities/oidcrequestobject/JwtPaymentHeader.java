package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class JwtPaymentHeader {

    private String alg;

    public JwtPaymentHeader(String alg) {
        this.alg = alg;
    }
}
