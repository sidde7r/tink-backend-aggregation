package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityKeyResponseEntity {
    private String cardId;
    private String securityKeyIndex;
    private Boolean newCard;

    public String getCardId() {
        return cardId;
    }

    public String getSecurityKeyIndex() {
        return securityKeyIndex;
    }

    public Boolean getNewCard() {
        return newCard;
    }
}
