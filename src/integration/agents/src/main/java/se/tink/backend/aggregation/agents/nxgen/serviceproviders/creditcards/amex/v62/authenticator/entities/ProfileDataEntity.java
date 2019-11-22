package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProfileDataEntity {
    private String maskedUserId;
    private String data;

    public String getMaskedUserId() {
        return maskedUserId;
    }

    public String getData() {
        return data;
    }
}
