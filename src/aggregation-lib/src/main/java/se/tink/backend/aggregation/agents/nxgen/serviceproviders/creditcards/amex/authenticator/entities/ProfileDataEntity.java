package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProfileDataEntity {
    private String maskedUserId;
    private String data;
}
