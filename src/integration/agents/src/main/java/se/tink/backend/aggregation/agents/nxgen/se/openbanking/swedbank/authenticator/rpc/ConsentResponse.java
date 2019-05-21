package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entity.consent.ConsentLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    private String transactionStatus;
    private String consentStatus;
    private String consentId;
    private ConsentLinksEntity links;

    public String getConsentId() {
        return consentId;
    }
}
