package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityKeyRequestEntity {
    private String securityKeyIndex;
    private String cardId;
    private String securityCode;

    public SecurityKeyRequestEntity setSecurityKeyIndex(String securityKeyIndex) {
        this.securityKeyIndex = securityKeyIndex;
        return this;
    }

    public SecurityKeyRequestEntity setCardId(String cardId) {
        this.cardId = cardId;
        return this;
    }

    public SecurityKeyRequestEntity setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
        return this;
    }
}
