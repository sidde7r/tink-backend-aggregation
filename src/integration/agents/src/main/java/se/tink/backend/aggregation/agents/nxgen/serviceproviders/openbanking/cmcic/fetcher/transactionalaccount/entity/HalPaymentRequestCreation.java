package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HalPaymentRequestCreation {
    @JsonProperty("appliedAuthenticationApproach")
    private AppliedAuthenticationApproachEntity appliedAuthenticationApproach = null;

    @JsonProperty("_links")
    private PaymentRequestResourceCreationLinks links = null;

    public AppliedAuthenticationApproachEntity getAppliedAuthenticationApproach() {
        return appliedAuthenticationApproach;
    }

    public void setAppliedAuthenticationApproach(
            AppliedAuthenticationApproachEntity appliedAuthenticationApproach) {
        this.appliedAuthenticationApproach = appliedAuthenticationApproach;
    }

    public PaymentRequestResourceCreationLinks getLinks() {
        return links;
    }

    public void setLinks(PaymentRequestResourceCreationLinks links) {
        this.links = links;
    }
}
