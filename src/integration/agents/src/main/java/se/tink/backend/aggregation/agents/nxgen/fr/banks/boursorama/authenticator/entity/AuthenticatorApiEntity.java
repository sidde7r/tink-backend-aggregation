package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticatorApiEntity {
    private String href;
    private String method;
    private AuthenticatorParamsEntity params;
}
