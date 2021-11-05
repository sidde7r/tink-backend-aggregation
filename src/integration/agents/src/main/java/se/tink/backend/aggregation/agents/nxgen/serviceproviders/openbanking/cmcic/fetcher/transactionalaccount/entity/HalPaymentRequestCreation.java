package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@Setter
public class HalPaymentRequestCreation {
    @JsonProperty("appliedAuthenticationApproach")
    private String appliedAuthenticationApproach = null;

    @JsonProperty("_links")
    private PaymentRequestResourceCreationLinks links = null;
}
