package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class RemittanceAmountEntity {
    @JsonProperty("duePayableAmount")
    private AmountTypeEntity duePayableAmount = null;

    @JsonProperty("discountAppliedAmount")
    private TypedAmountEntity discountAppliedAmount = null;

    @JsonProperty("creditNoteAmount")
    private AmountTypeEntity creditNoteAmount = null;

    @JsonProperty("taxAmount")
    private TypedAmountEntity taxAmount = null;

    @JsonProperty("adjustmentAmountAndReason")
    private DocumentAdjustmentEntity adjustmentAmountAndReason = null;

    @JsonProperty("remittedAmount")
    private AmountTypeEntity remittedAmount = null;
}
