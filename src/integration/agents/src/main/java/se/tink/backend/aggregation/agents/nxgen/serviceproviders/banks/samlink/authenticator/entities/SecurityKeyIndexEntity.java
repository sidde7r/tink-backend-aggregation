package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityKeyIndexEntity {
    private String index;
    private CardEntity card;

    public String getIndex() {
        return index;
    }

    public CardEntity getCard() {
        return card;
    }
}
