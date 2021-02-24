package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.JWTHeaderValues;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenHeaderEntity {
    private String alg;
    private String typ;
    private String kid;

    public TokenHeaderEntity(String kid) {
        this.alg = JWTHeaderValues.ALG;
        this.typ = JWTHeaderValues.TYPE;
        this.kid = kid;
    }
}
