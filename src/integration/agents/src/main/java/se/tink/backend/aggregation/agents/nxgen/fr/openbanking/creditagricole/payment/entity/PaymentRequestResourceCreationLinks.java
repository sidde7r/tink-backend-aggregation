package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

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
