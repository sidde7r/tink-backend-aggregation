package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestResourceCreationLinks {
    @JsonProperty("consentApproval")
    private PaymentRequestResourceCreationLinksConsentApproval consentApproval = null;
}
