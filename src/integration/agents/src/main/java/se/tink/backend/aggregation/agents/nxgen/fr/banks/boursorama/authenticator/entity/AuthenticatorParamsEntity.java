package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticatorParamsEntity {
    private String accountKey;
    private String creditCardKey;
}
