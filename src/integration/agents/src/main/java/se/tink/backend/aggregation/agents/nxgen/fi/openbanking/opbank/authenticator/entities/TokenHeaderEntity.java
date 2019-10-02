package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenHeaderEntity {
    private String alg = "RS256";
    private String typ = "JWT";
    private String kid = "PSDSE-FINA-44059-RSA";
}
