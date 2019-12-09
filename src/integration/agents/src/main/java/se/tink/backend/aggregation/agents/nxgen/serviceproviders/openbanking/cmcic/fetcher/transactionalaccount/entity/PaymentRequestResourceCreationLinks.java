package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentRequestResourceCreationLinks {
    @JsonProperty("consentApproval")
    private PaymentRequestResourceCreationLinksConsentApproval consentApproval = null;

    public PaymentRequestResourceCreationLinksConsentApproval getConsentApproval() {
        return consentApproval;
    }

    public void setConsentApproval(
            PaymentRequestResourceCreationLinksConsentApproval consentApproval) {
        this.consentApproval = consentApproval;
    }
}
