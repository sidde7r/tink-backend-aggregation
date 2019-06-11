package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    private String transactionStatus;

    @JsonProperty("_links")
    private ConsentLinksEntity links;

    private String consentId;
    private String psuMessage;

    public String getConsentId() {
        return consentId;
    }

    public ConsentLinksEntity getLinks() {
        return links;
    }
}
