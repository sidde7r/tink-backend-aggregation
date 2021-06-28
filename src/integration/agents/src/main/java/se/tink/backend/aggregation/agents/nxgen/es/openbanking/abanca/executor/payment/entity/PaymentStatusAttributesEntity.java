package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.entity;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class PaymentStatusAttributesEntity {

    private String valueDate;
    private String operationDate;
    private String accountingDate;
    private String concept;
    private AmountEntity amount;
    private AmountEntity subsequentBalance;
}
