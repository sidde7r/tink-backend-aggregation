package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.entity;

import lombok.Builder;
import se.tink.backend.aggregation.annotations.JsonObject;

@Builder
@JsonObject
public class PaymentAttributesEntity {

    private final AccountInfoEntity remoteAccount;
    private final String concept;
    private final AmountEntity amount;
    private final String recipientName;
    private final String operationType;
}
