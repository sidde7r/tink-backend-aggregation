package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class PaymentCoverageRequestResourceEntity {
    @JsonProperty("paymentCoverageRequestId")
    private String paymentCoverageRequestId = null;

    @JsonProperty("payee")
    private String payee = null;

    @JsonProperty("instructedAmount")
    private AmountTypeEntity instructedAmount = null;

    @JsonProperty("accountId")
    private AccountIdentificationEntity accountId = null;
}
