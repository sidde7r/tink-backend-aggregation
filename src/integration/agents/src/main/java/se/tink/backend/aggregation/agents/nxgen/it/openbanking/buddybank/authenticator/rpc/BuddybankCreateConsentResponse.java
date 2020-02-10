package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.rpc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BuddybankCreateConsentResponse {

    private String consentStatus;
    private String consentId;
    private String psuMessage;

    public String getConsentId() {
        return Preconditions.checkNotNull(Strings.emptyToNull(consentId));
    }

    public String getConsentStatus() {
        return consentStatus;
    }

    public String getPsuMessage() {
        return psuMessage;
    }
}
