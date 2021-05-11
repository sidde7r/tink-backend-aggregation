package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.entity.BuddybankConsentLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BuddybankConsentResponse implements ConsentResponse {

    private String consentStatus;
    private String consentId;
    private String psuMessage;

    @JsonProperty("_links")
    private BuddybankConsentLinksEntity links;

    private BuddybankConsentLinksEntity getLinks() {
        return Optional.ofNullable(links)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_SCA_URL));
    }

    public String getConsentId() {
        return Preconditions.checkNotNull(Strings.emptyToNull(consentId));
    }

    public String getScaRedirect() {
        return getLinks().getScaRedirect().getHref();
    }

    public String getPsuMessage() {
        return psuMessage;
    }
}
